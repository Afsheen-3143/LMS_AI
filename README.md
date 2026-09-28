# AI Enrollment Assistant

A RAG + tool-calling AI assistant built with **Spring Boot 3.2 + Spring AI 1.1.8**,
extracted from a larger Learning Management System backend to stand on its own
as a portfolio piece.

It answers two very different kinds of student questions correctly:

- **Static knowledge** (course catalog, programs, fees, batch schedules,
  FAQs) — retrieved via semantic search over an embedded knowledge base, so
  answers are grounded in real data instead of the model guessing.
- **Live, per-student data** (their own enrollments, real-time seat counts,
  payment status) — fetched via tool-calling straight from application
  services, scoped to the authenticated student's JWT so it's
  architecturally impossible for the AI to leak one student's data to
  another.

It also holds persistent conversation memory (a real database table, not an
in-memory map) and can drive real enrollment actions through the same
service layer a normal UI would use — not just talk about them.

## ⚠️ What's real vs. a stand-in

This code was originally built inside a larger company LMS platform. The
**AI assistant itself is the real, unmodified feature** — chat orchestration,
RAG retrieval/ingestion, tool-calling, chat memory, conversation ownership,
prompt design, all of it.

`com.afsheen.aiassistant.lms` is this showcase's own LMS domain — course
catalog, programs, students, batches, schedules, enrollments, and fees —
**real, MySQL-backed persistence with full CRUD REST endpoints** (see
`lms/controller`), not a copy of the original company's proprietary schema
or business logic. The one intentionally-fake piece is
`GET /api/dev/token/{studentId}`, which mints a JWT directly instead of a
real register/login/OTP flow — see the comments on `DevAuthController` for
specifics.

Everything under `com.afsheen.aiassistant` (not `.lms`) is the actual
assistant implementation.

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

- **RAG** (`rag/`): `DocumentIngestionService` chunks and embeds the course
  catalog, fees, batch schedules, and FAQs into Elasticsearch.
  `RagRetrievalService` does the read side — over-fetches candidates via
  vector similarity, then reranks by blending similarity with keyword
  overlap.
- **Tool-calling** (`tools/AssistantTools.java`): live/per-student data
  that would either go stale in a vector index (seat counts) or leak across
  students (enrollments, payment status) if pre-embedded. The model decides
  per-question whether it needs to call one of these.
- **Grounding** (`prompt/AssistantSystemPrompt.java`): the system prompt
  enforces "answer only from retrieved context or tool results, say 'I
  don't know' otherwise" — the primary lever against hallucination.
- **Conversation memory** (`memory/`): backed by a Liquibase-managed table
  (`assistant_chat_message`), not a transient map, so history survives
  restarts. `ConversationIdSupport` encodes the owning student directly in
  the conversation id (`<studentId>::<uuid>`) so ownership can be enforced
  without a second lookup table — and a malformed/placeholder id (e.g.
  Swagger's default `"string"`) is treated as "start a new session" rather
  than rejected.
- **Pluggable LLM provider** (`config/SpringAiConfig.java`): Mistral,
  Anthropic, and Google Gemini are all wired up as interchangeable chat
  models — swapping the active one is a one-line `@Qualifier` change.
  Embeddings run on a separate model (Mistral's `mistral-embed`) from chat,
  since the vector index's dimensionality is fixed once built.

## Running it

**Prerequisites:** Java 21, Docker (for Elasticsearch + MySQL), and a free
API key for at least one embedding + one chat provider.

```bash
# 1. Start Elasticsearch (RAG vector store) and MySQL
docker-compose up -d

# 2. Set API keys (free tiers work fine)
export MISTRALAI_API_KEY=...   # https://console.mistral.ai/api-keys (embeddings)
export GEMINI_API_KEY=...      # https://aistudio.google.com/apikey (chat, active provider)

# 3. Run the app - Liquibase creates the schema and seeds demo data on first boot
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` (override with `SERVER_PORT`).

**Get a test token** (replaces the real host app's OTP login for this demo):

```bash
curl http://localhost:8080/api/dev/token/S000001
```

**Reindex the knowledge base** (do this once after startup — nothing is
searchable via RAG until you do):

```bash
curl -X POST http://localhost:8080/api/admin/assistant/reindex \
  -H "Authorization: Bearer $(curl -s 'http://localhost:8080/api/dev/token/ADMIN001?role=ADMIN' | jq -r .token)"
```

**Chat with it:**

```bash
TOKEN=$(curl -s http://localhost:8080/api/dev/token/S000001 | jq -r .token)

curl -X POST http://localhost:8080/api/student-assistant/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"message": "Do you have any Java courses?"}'
```

**Or use one of the included frontends** — a full React app (`frontend/`,
`npm run dev`) with a Catalog page for managing courses/students/batches/
schedules, or a small no-build-step chat UI:

```bash
cd tools/ai-assistant-ui
node serve.js
# open http://localhost:5173, click "Get dev token", start chatting
```

## API reference

| Endpoint | Role | Purpose |
|---|---|---|
| `POST /api/student-assistant/chat` | STUDENT | Main chat entry point |
| `GET /api/student-assistant/history/{sessionId}` | STUDENT | Replay a saved conversation |
| `POST /api/student-assistant/confirm-enrollment` | STUDENT | Actually enroll (body: `courseId` or `programId`) |
| `POST /api/admin/assistant/reindex` | ADMIN | Rebuild the RAG knowledge base |
| `GET/POST/PUT/DELETE /api/admin/assistant/faqs` | ADMIN | Manage FAQ content |
| `GET/POST/PUT/DELETE /api/admin/lms/courses` | ADMIN | Manage the course catalog |
| `GET/POST/PUT/DELETE /api/admin/lms/students` | ADMIN | Manage students |
| `GET/POST/PUT/DELETE /api/admin/lms/batches` | ADMIN | Manage class batches |
| `GET/POST/PUT/DELETE /api/admin/lms/schedules` | ADMIN | Manage the class schedule |
| `GET /api/admin/lms/programs` | ADMIN | List programs (read-only) |
| `GET /api/dev/token/{studentId}?role=STUDENT\|ADMIN` | public | **Demo-only** — mint a test JWT |

Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

## Demo data

Three courses (Advanced Java, Data Science with Python, Full-Stack Web
Development), one program bundling two of them, batches with schedules, and
two demo students — one (`S000002`) pre-seeded with an active enrollment so
"what am I enrolled in?" has something to show. All seeded via Liquibase
(`db/changelog/changelogs/202609211520-seed-demo-data.xml`) on first boot —
edit freely, or manage it live through the Catalog page / `/api/admin/lms/**`.

## Tech stack

Spring Boot 3.2.2 (Java 21) · Spring AI 1.1.8 · Elasticsearch (vector
store) · MySQL (all persistence via Liquibase) · Google Gemini / Anthropic
Claude / Mistral (pluggable chat providers) · Spring Security (JWT) ·
MapStruct · springdoc-openapi · React + TypeScript (`frontend/`)
