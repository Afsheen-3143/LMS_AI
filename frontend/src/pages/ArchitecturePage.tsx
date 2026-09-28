export default function ArchitecturePage({ onBack }: { onBack?: () => void }) {
  return (
    <div style={{ height: '100%', overflowY: 'auto', padding: '28px 32px', maxWidth: 900, margin: '0 auto' }}>
      {onBack && (
        <button className="back" type="button" onClick={onBack} style={{ marginBottom: 16 }}>
          ← Back
        </button>
      )}
      <h2 style={{ marginTop: 0 }}>How the assistant answers a question</h2>
      <p style={{ color: 'var(--text-dim)', fontSize: 14, lineHeight: 1.6 }}>
        Every chat request goes through the same pipeline. The model decides per-question whether it
        needs retrieved knowledge, a live tool call, both, or neither — this page just shows the shape
        of that pipeline; it isn't a live trace of the request you just sent.
      </p>

      <FlowDiagram />

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginTop: 28 }}>
        <InfoCard
          color="var(--rag-color)"
          title="RAG retrieval"
          subtitle="rag/RagRetrievalService"
          points={[
            'For static, shared knowledge: course catalog text, fees, batch schedules, FAQs.',
            'DocumentIngestionService chunks and embeds this into Elasticsearch (mistral-embed).',
            'Over-fetches candidates via vector similarity (topK × 3), then reranks with a weighted blend: 0.8 vector similarity + 0.2 keyword overlap.',
            'Retrieved chunks are injected as system-prompt context for that turn only — the model answers from them, not from its own training data.',
            'If Elasticsearch is down, retrieval fails soft (safeRetrieveContext) — chat still proceeds on tools + memory alone.',
          ]}
        />
        <InfoCard
          color="var(--tool-color)"
          title="Tool calling"
          subtitle="tools/AssistantTools.java"
          points={[
            'For anything live or per-student: seat counts, enrollments, payment status, profile.',
            'The model decides per-question whether to invoke a @Tool method.',
            'Per-student scoping via a ThreadLocal (setStudentId / clear) set around the chat call — never a studentId parameter supplied by the model, so one student can’t query another’s data.',
            'Enrolling or cancelling always requires an explicit confirmation step (confirm-enrollment) before it takes effect.',
          ]}
        />
      </div>

      <div
        style={{
          marginTop: 24,
          border: '1px solid var(--border)',
          borderRadius: 10,
          padding: 16,
          background: 'var(--panel)',
        }}
      >
        <h3 style={{ fontSize: 13, margin: '0 0 8px', color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
          Grounding &amp; memory
        </h3>
        <p style={{ fontSize: 13, color: 'var(--text-dim)', lineHeight: 1.6, margin: 0 }}>
          <code>prompt/AssistantSystemPrompt.java</code> is the single source of truth for the system
          prompt: answer only from context/tools, say "I don't know" otherwise, never leak cross-student
          data, always confirm before enrolling or cancelling. Conversation history is persisted in the{' '}
          <code>assistant_chat_message</code> table (not an in-memory map), so it survives restarts. The
          conversation id itself encodes the owning student as <code>&lt;studentId&gt;::&lt;uuid&gt;</code>,
          which is how ownership is checked without a second lookup table.
        </p>
      </div>
    </div>
  );
}

function FlowDiagram() {
  const box = (label: string, sub: string, color: string) => (
    <div
      style={{
        border: `1px solid ${color}`,
        borderRadius: 10,
        padding: '10px 14px',
        background: 'var(--panel-2)',
        minWidth: 150,
        textAlign: 'center' as const,
      }}
    >
      <div style={{ fontSize: 13, fontWeight: 600 }}>{label}</div>
      <div style={{ fontSize: 11, color: 'var(--text-dim)', marginTop: 2 }}>{sub}</div>
    </div>
  );
  const arrow = (vertical = false) => (
    <div
      style={{
        color: 'var(--text-dim)',
        fontSize: 20,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: vertical ? '4px 0' : '0 8px',
      }}
    >
      {vertical ? '↓' : '→'}
    </div>
  );

  return (
    <div
      style={{
        border: '1px solid var(--border)',
        borderRadius: 12,
        padding: 24,
        background: 'var(--panel)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 4,
      }}
    >
      {box('Student', 'JWT auth', 'var(--border)')}
      {arrow(true)}
      {box('EnrollmentAssistantController', '/api/student-assistant/chat', 'var(--accent)')}
      {arrow(true)}
      {box('EnrollmentAssistantServiceImpl', 'chat() orchestration', 'var(--accent)')}
      {arrow(true)}
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {box('RagRetrievalService', 'vector search + rerank', 'var(--rag-color)')}
        {arrow()}
        {box('Retrieved context', 'injected into system prompt', 'var(--rag-color)')}
      </div>
      {arrow(true)}
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {box('ChatClient (Spring AI)', 'Gemini / Claude / Mistral', 'var(--accent)')}
        {arrow()}
        {box('AssistantTools', '@Tool methods, per-student', 'var(--tool-color)')}
      </div>
      {arrow(true)}
      {box('Response', 'answer + optional pendingAction', 'var(--border)')}
    </div>
  );
}

function InfoCard({
  color,
  title,
  subtitle,
  points,
}: {
  color: string;
  title: string;
  subtitle: string;
  points: string[];
}) {
  return (
    <div style={{ border: `1px solid ${color}`, borderRadius: 10, padding: 16, background: 'var(--panel)' }}>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
        <h3 style={{ margin: 0, fontSize: 15, color }}>{title}</h3>
        <span style={{ fontSize: 11, color: 'var(--text-dim)' }}>{subtitle}</span>
      </div>
      <ul style={{ fontSize: 13, color: 'var(--text-dim)', lineHeight: 1.6, paddingLeft: 18, marginTop: 10 }}>
        {points.map((p, i) => (
          <li key={i} style={{ marginBottom: 6 }}>
            {p}
          </li>
        ))}
      </ul>
    </div>
  );
}
