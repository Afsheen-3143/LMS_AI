# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

An AI enrollment assistant (RAG + tool-calling), originally built inside a larger company LMS backend, now
paired with a real LMS domain of its own. **Everything under `com.afsheen.aiassistant` (not `.lms`) is the
real, unmodified assistant feature** — chat orchestration, RAG retrieval/ingestion, tool-calling, chat
memory, conversation ownership, prompt design. `com.afsheen.aiassistant.lms` is this repo's own LMS domain —
students, courses, programs, batches, schedules, enrollments, fees — with real MySQL-backed persistence
(Liquibase-managed, see `lms/entity` + `db/changelog/changelogs`) and full CRUD REST endpoints under
`/api/admin/lms/**` (see `lms/controller`), plus a matching `CatalogPage` in `frontend/`. It is not a copy of
the original company's proprietary schema/business logic, but it is real, working persistence, not
throwaway demo data — the only intentionally-fake piece left is `DevAuthController`'s token-minting endpoint
(mints a JWT directly instead of a real register/login/OTP flow).

## Commands

```bash
# Start Elasticsearch (vector store) + MySQL — required before running the app
docker-compose up -d

# Run the app — Liquibase creates the schema and seeds demo data on first boot
./mvnw spring-boot:run

# Build / compile
./mvnw clean install
./mvnw compile

# There is no src/test directory in this repo — no test command applies
```

Required env vars to actually talk to a model: `MISTRALAI_API_KEY` (embeddings, `mistral-embed`) and
`GEMINI_API_KEY` (chat, active provider — see `SpringAiConfig`). `ANTHROPIC_API_KEY` is wired but unused
unless you switch providers (see below).

Useful runtime calls while iterating (JWT auth is required on everything except `/api/dev/**` and swagger):

```bash
# Mint a dev JWT (no real login flow in this showcase)
curl http://localhost:8080/api/dev/token/S000001
curl "http://localhost:8080/api/dev/token/ADMIN001?role=ADMIN"

# Reindex the RAG knowledge base — do this once after every fresh startup;
# nothing is retrievable until you do
curl -X POST http://localhost:8080/api/admin/assistant/reindex -H "Authorization: Bearer $TOKEN"

# Chat
curl -X POST http://localhost:8080/api/student-assistant/chat \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"message": "Do you have any Java courses?"}'
```

Test UI (no build step): `cd tools/ai-assistant-ui && node serve.js`, then open `http://localhost:5173`.

Swagger: `http://localhost:8080/swagger-ui/index.html`.

## Architecture

```
Student → JWT auth → EnrollmentAssistantController
                          │
                          ▼
              EnrollmentAssistantServiceImpl
                 │                    │
                 ▼                    ▼
      RagRetrievalService      ChatClient (Spring AI)
      (Elasticsearch vector       │         │
       similarity search +        │         ▼
       keyword rerank)            │   AssistantTools (@Tool methods:
                 │                │    live enrollments, seat counts,
                 ▼                │    payment status, profile...)
         Retrieved context ───────┘
         injected as system prompt
                 │
                 ▼
         Gemini / Claude / Mistral (pluggable, see SpringAiConfig)
```

**The RAG-vs-tools split is the core design decision** and drives where any new capability belongs:

- **RAG** (`rag/`) is for static, shared knowledge that's safe to pre-embed and search semantically: course
  catalog text, fees, batch schedules, FAQs. `DocumentIngestionService` chunks/embeds it into Elasticsearch;
  `RagRetrievalService` over-fetches candidates via vector similarity (`topK * 3`) then reranks with a
  weighted blend of vector similarity (0.8) and plain keyword overlap (0.2) — a heuristic reranker, not a
  trained cross-encoder. Retrieved chunks get injected as system-prompt context per-turn
  (`AssistantSystemPrompt.withContext`), not answered from the model's own training data.
- **Tool-calling** (`tools/AssistantTools.java`) is for anything live or per-student: seat counts (would go
  stale in a vector index the moment someone enrolls), enrollments/payment status/profile (would leak across
  students if pre-embedded). The model decides per-question whether to call a `@Tool` method. Per-student
  scoping works via `AssistantTools.setStudentId(...)` / `.clear()` on a `ThreadLocal`, set in a
  try/finally around the chat call in `EnrollmentAssistantServiceImpl.chat()` — any new tool that touches
  student data must go through `getStudentId()`, never take a studentId parameter from the model.
- **Grounding** (`prompt/AssistantSystemPrompt.java`) is the single source of truth for the system prompt,
  shared by the default `ChatClient` bean and the per-turn RAG call. Its numbered rules (answer only from
  context/tools, say "I don't know" otherwise, never leak cross-student data, always confirm before
  enrolling/cancelling) are the primary defense against hallucination — when changing assistant behavior,
  this file is usually the first place to look, not the controller/service.
- **Conversation memory** (`memory/`) is backed by a Liquibase-managed table (`assistant_chat_message`), not
  an in-memory map — history survives restarts and backs the `GET /history` endpoint.
  `ConversationIdSupport` encodes the owning student directly into the conversation id as
  `<studentId>::<uuid>` (see its javadoc) so ownership can be checked from the id alone wherever Spring AI's
  `ChatMemoryRepository` contract only hands back an id — no second lookup table. A conversationId that's
  blank or doesn't parse (e.g. Swagger's default `"string"`) is treated as "start a new session," not
  rejected — see `resolveConversationId` in `EnrollmentAssistantServiceImpl`.
- **Pluggable LLM provider** (`config/SpringAiConfig.java`): Mistral, Anthropic, and Google Gemini chat
  models are all wired as beans; the active one is selected by the `@Qualifier` on `studentAssistantChatClient`
  (currently `googleGenAiChatModel`). Embeddings always run on Mistral (`mistral-embed`) regardless of chat
  provider, since the vector index's dimensionality is fixed once built — don't change the embedding model
  without a reindex. Note the `anthropicTemperatureDefaultRemover` `BeanPostProcessor`: Claude models reject
  a `temperature` param outright, and Spring AI hard-codes one into `AnthropicChatProperties` that no
  property/per-request override can unset, so it's stripped at the properties-bean level before
  `AnthropicChatModel` reads it. Only relevant if the `@Qualifier` is switched to Anthropic.

RAG retrieval failure (Elasticsearch down/unreachable) is treated as best-effort, not fatal — the chat call
still proceeds on tools + chat memory alone (`safeRetrieveContext` in `EnrollmentAssistantServiceImpl`)
rather than failing the whole request.

## Persistence

Everything is real, Liquibase-managed MySQL persistence (changelogs under `resources/db/changelog/changelogs/`):
`assistant_faq` and `assistant_chat_message` back the AI assistant; `student`, `course`, `program`,
`program_course`, `course_fee_setting`, `program_fee_setting`, `class_batch`, `class_schedule`, `enrollment`,
`enrollment_batch`, and `class_student` back the LMS domain under `com.afsheen.aiassistant.lms`. All
repositories in `.lms.repository` are plain Spring Data JPA interfaces (`extends JpaRepository<...>`) — there
are no hand-written in-memory implementations or a `DemoDataStore` anymore. `Student`/`Course`/`Program` use
human-readable ids (`S000001`, `CRS001`, `PRG001`); since MySQL has no native `SEQUENCE`, these are minted via
`IdSequenceGenerator` against the `id_sequence` table rather than `AUTO_INCREMENT`. `202609211520-seed-demo-data.xml`
seeds the same demo catalog `DemoDataStore` used to hand-seed at startup, so the documented `S000001`/`CRS001`
curl examples above keep working unchanged on a fresh database.

## Security

JWT auth (`JwtFilter`/`JwtUtil`), stateless sessions, role-gated routes in `SecurityConfig`:
`/api/student-assistant/**` requires `STUDENT`, `/api/admin/assistant/**` and `/api/admin/lms/**` require
`ADMIN`, `/api/dev/**` and swagger are open. CORS is wide open (`allowedOriginPatterns: *`) — this is fine for the local demo UI in
`tools/ai-assistant-ui` but should never be treated as a template for a real deployment. The JWT secret in
`application.yml` has a hardcoded demo-only default; a real deployment must set `JWT_SECRET` explicitly.
