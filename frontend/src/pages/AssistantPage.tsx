import { useEffect, useRef, useState } from 'react';
import { api, ApiError } from '../api/client';
import { useSession } from '../context/SessionContext';
import type { ChatUiMessage } from '../api/types';
import Icon from '../components/Icon';

let idCounter = 0;
function nextId() {
  idCounter += 1;
  return 'm' + idCounter;
}

const TILES = [
  { cls: 't1', icon: 'search', title: 'Find a course', sub: 'Tell me what you want to learn and I will suggest a fit.', q: 'What courses do you offer?' },
  { cls: 't2', icon: 'book', title: 'Enroll me', sub: 'Join a course, step by step.', q: 'I want to enroll in a course' },
  { cls: 't3', icon: 'chart', title: 'My enrollments', sub: 'See what you are already enrolled in.', q: 'What am I currently enrolled in?' },
  { cls: 't4', icon: 'cal', title: 'Batches & fees', sub: 'Schedules and pricing.', q: 'What are the upcoming batches and fees?' },
  { cls: 't5', icon: 'help', title: 'Payment status', sub: 'Check where a payment stands.', q: "What's my payment status?" },
];

export default function AssistantPage() {
  const s = useSession();
  const [messages, setMessages] = useState<ChatUiMessage[]>([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [online, setOnline] = useState<boolean | null>(null);
  const [errorNote, setErrorNote] = useState('');
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const threadRef = useRef<HTMLDivElement>(null);
  const epochRef = useRef(0);

  useEffect(() => {
    let cancelled = false;
    api
      .ping(s.baseUrl)
      .then((res) => !cancelled && setOnline(res.ok))
      .catch(() => !cancelled && setOnline(false));
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function scrollDown() {
    requestAnimationFrame(() => {
      if (threadRef.current) threadRef.current.scrollTop = threadRef.current.scrollHeight;
    });
  }

  function pushMessage(msg: ChatUiMessage) {
    setMessages((prev) => [...prev, msg]);
    scrollDown();
  }

  function resetChat() {
    epochRef.current++;
    setMessages([]);
    s.setSessionId('');
    setInput('');
    setErrorNote('');
  }

  async function send(text: string) {
    const message = text.trim();
    if (!message || sending) return;

    pushMessage({ id: nextId(), role: 'user', content: message });
    setInput('');
    autoGrow();
    setSending(true);
    setErrorNote('');
    const epoch = epochRef.current;

    try {
      const res = await api.chat(s.baseUrl, s.token, {
        message,
        conversationId: s.sessionId.trim() || null,
      });
      if (epoch !== epochRef.current) return;

      if (res.conversationId && res.conversationId !== s.sessionId.trim()) {
        s.setSessionId(res.conversationId);
      }

      pushMessage({
        id: nextId(),
        role: 'assistant',
        content: res.response || '(empty response)',
        actionRequired: res.actionRequired,
        pendingAction: res.pendingAction ?? undefined,
      });
    } catch (e) {
      if (epoch !== epochRef.current) return;
      const msg = e instanceof ApiError ? e.message : (e as Error).message;
      setErrorNote(msg);
      pushMessage({ id: nextId(), role: 'system', content: 'Something went wrong: ' + msg });
    } finally {
      if (epoch === epochRef.current) setSending(false);
    }
  }

  async function confirmEnrollment(fields: Record<string, unknown>) {
    setSending(true);
    try {
      const res = await api.confirmEnrollment(s.baseUrl, s.token, {
        courseId: (fields.courseId as string) ?? null,
        programId: (fields.programId as string) ?? null,
        batchId: fields.batchId != null ? Number(fields.batchId) : null,
      });
      pushMessage({
        id: nextId(),
        role: 'assistant',
        content: res.message || (res.success ? 'Enrollment confirmed.' : 'Enrollment failed.'),
      });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : (e as Error).message;
      pushMessage({ id: nextId(), role: 'system', content: 'Confirm-enrollment failed: ' + msg });
    } finally {
      setSending(false);
    }
  }

  function autoGrow() {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 130) + 'px';
  }

  const hasStarted = messages.length > 0;

  return (
    <main className="chat">
      <header className="top">
        <button className="back" type="button" hidden={!hasStarted} onClick={resetChat}>
          <Icon name="back" />
          New chat
        </button>
        <div className="orb" aria-hidden="true" />
        <div>
          <h1>Assistant</h1>
          <div className={'status' + (online === false ? ' offline' : '')}>
            {online === null ? 'Connecting…' : online ? 'Online and ready to help' : 'Backend unreachable'}
          </div>
        </div>
      </header>

      <section className="thread" ref={threadRef} aria-live="polite" aria-label="Conversation">
        <div className="inner">
          <div className="hero" hidden={hasStarted}>
            <div className="orb big" aria-hidden="true" />
            <h2>Hi {s.studentId}, what would you like to do?</h2>
            <p>
              I can search the course catalog, check seat counts, look up your enrollments and payment
              status, and start an enrollment for you.
            </p>
            <div className="tiles">
              {TILES.map((t) => (
                <button key={t.title} type="button" className={'tile ' + t.cls} onClick={() => send(t.q)}>
                  <span className="ic">
                    <Icon name={t.icon} />
                  </span>
                  <span>
                    <b>{t.title}</b>
                    <span>{t.sub}</span>
                  </span>
                </button>
              ))}
            </div>
          </div>

          {messages.map((m) => (
            <MessageRow key={m.id} msg={m} onConfirm={confirmEnrollment} />
          ))}

          {sending && (
            <div className="msg ai">
              <div className="orb" aria-hidden="true" />
              <div className="typing" aria-label="Assistant is typing">
                <i /> <i /> <i />
              </div>
            </div>
          )}
        </div>
      </section>

      <div className="composer-wrap">
        <div className="composer">
          <textarea
            ref={textareaRef}
            rows={1}
            placeholder="Ask about courses, fees, batches, or your enrollments…"
            value={input}
            onChange={(e) => {
              setInput(e.target.value);
              autoGrow();
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                send(input);
              }
            }}
          />
          <button className="send" disabled={sending || !input.trim()} onClick={() => send(input)} aria-label="Send message">
            <Icon name="send" />
          </button>
        </div>
        <p className="note">
          {errorNote
            ? errorNote
            : 'This assistant can make mistakes. Enrolling or cancelling always asks you to confirm first.'}
        </p>
      </div>
    </main>
  );
}

function MessageRow({
  msg,
  onConfirm,
}: {
  msg: ChatUiMessage;
  onConfirm: (fields: Record<string, unknown>) => void;
}) {
  if (msg.role === 'system') {
    return (
      <div className="msg system" style={{ justifyContent: 'center' }}>
        <div className="bubble">{msg.content}</div>
      </div>
    );
  }

  if (msg.role === 'user') {
    return (
      <div className="msg user">
        <div className="bubble">{msg.content}</div>
      </div>
    );
  }

  return (
    <div className="msg ai">
      <div className="orb" aria-hidden="true" />
      <div className="col">
        <div className="bubble">{msg.content}</div>
        {msg.actionRequired && msg.pendingAction && (
          <ActionPanel actionRequired={msg.actionRequired} pendingAction={msg.pendingAction} onConfirm={onConfirm} />
        )}
      </div>
    </div>
  );
}

function ActionPanel({
  actionRequired,
  pendingAction,
  onConfirm,
}: {
  actionRequired: string;
  pendingAction: Record<string, unknown>;
  onConfirm: (fields: Record<string, unknown>) => void;
}) {
  const [status, setStatus] = useState<'pending' | 'confirmed' | 'dismissed'>('pending');
  if (status === 'dismissed') return null;
  return (
    <div className="panel">
      <h4>{actionRequired}</h4>
      {Object.entries(pendingAction).map(([key, value]) => (
        <div className="field" key={key}>
          <span>{key}</span>
          <b>{String(value)}</b>
        </div>
      ))}
      <div className="btns">
        <button
          className="btn"
          type="button"
          disabled={status === 'confirmed'}
          onClick={() => {
            setStatus('confirmed');
            onConfirm(pendingAction);
          }}
        >
          {status === 'confirmed' ? 'Confirmed' : 'Confirm enrollment'}
        </button>
        {status === 'pending' && (
          <button className="btn ghost" type="button" onClick={() => setStatus('dismissed')}>
            Dismiss
          </button>
        )}
      </div>
    </div>
  );
}
