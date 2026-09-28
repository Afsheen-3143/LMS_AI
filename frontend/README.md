# AI Enrollment Assistant — React frontend

A React + Vite + TypeScript showcase frontend for the backend under
`com.afsheen.aiassistant` in this repo. It talks directly to the same REST
API as the plain-JS test console in `tools/ai-assistant-ui` (kept as-is);
this app adds a proper component structure, an admin FAQ/reindex screen,
and a static "How it works" screen that visualizes the RAG-vs-tools
architecture described in the repo's `CLAUDE.md`.

## Run

```bash
# Backend (from the repo root)
docker-compose up -d
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

Opens on `http://localhost:5174` (the old static tool uses 5173, so both can
run at once). The backend's CORS policy (`SecurityConfig`) already allows
any origin for this local demo, so no dev proxy is needed — just point the
"Backend base URL" field at your running backend (default
`http://localhost:8080`).

## Screens

- **Chat** — mint a dev JWT, chat with the assistant, confirm enrollments via
  the action card, load/persist conversation history.
- **Admin** — mint an `ADMIN` dev JWT, manage `assistant_faq` rows (the RAG
  knowledge base), and trigger `/api/admin/assistant/reindex`.
- **How it works** — a static diagram of the request pipeline (RAG retrieval
  vs. tool calling vs. grounding/memory), for demoing the architecture
  without reading the Java source.

## Notes

- Auth is the same dev-only flow as the rest of the repo: `GET
  /api/dev/token/{id}?role=STUDENT|ADMIN` mints a JWT with no real
  login/OTP, purely so this showcase runs standalone. Don't reuse that
  pattern in a real deployment.
- Session state (base URL, token, student id, conversation id) is kept in
  `localStorage` under the `ai-assistant-react-ui` key, per browser.
