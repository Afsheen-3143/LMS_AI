import { useState } from 'react';
import { api, ApiError } from '../api/client';
import { useSession } from '../context/SessionContext';
import type { AssistantFaqResponse } from '../api/types';
import SessionPanel from '../components/SessionPanel';

const emptyForm = { category: '', question: '', answer: '', active: true };

export default function AdminPage({ onBack }: { onBack?: () => void }) {
  const s = useSession();
  const [faqs, setFaqs] = useState<AssistantFaqResponse[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [status, setStatus] = useState<{ text: string; kind?: 'ok' | 'err' }>({ text: '' });

  function requireToken(): boolean {
    if (!s.token.trim()) {
      setStatus({ text: 'Get an ADMIN dev token first (role dropdown in the sidebar).', kind: 'err' });
      return false;
    }
    return true;
  }

  async function loadFaqs() {
    if (!requireToken()) return;
    setStatus({ text: 'Loading FAQs…' });
    try {
      const list = await api.listFaqs(s.baseUrl, s.token);
      setFaqs(list);
      setStatus({ text: `Loaded ${list.length} FAQ(s).`, kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function submitForm() {
    if (!requireToken()) return;
    if (!form.category.trim() || !form.question.trim() || !form.answer.trim()) {
      setStatus({ text: 'Category, question, and answer are required.', kind: 'err' });
      return;
    }
    setStatus({ text: editingId ? 'Updating FAQ…' : 'Creating FAQ…' });
    try {
      if (editingId) {
        await api.updateFaq(s.baseUrl, s.token, editingId, form);
      } else {
        await api.createFaq(s.baseUrl, s.token, form);
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadFaqs();
      setStatus({ text: 'Saved. Remember to reindex for it to affect chat answers.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function removeFaq(id: number) {
    if (!requireToken()) return;
    setStatus({ text: 'Deleting…' });
    try {
      await api.deleteFaq(s.baseUrl, s.token, id);
      await loadFaqs();
      setStatus({ text: 'Deleted. Remember to reindex.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function reindex() {
    if (!requireToken()) return;
    setStatus({ text: 'Reindexing knowledge base…' });
    try {
      const result = await api.reindex(s.baseUrl, s.token);
      setStatus({ text: 'Reindex complete: ' + JSON.stringify(result), kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      <SessionPanel showRole />

      <main style={{ flex: 1, padding: 20, overflowY: 'auto' }}>
        {onBack && (
          <button className="back" type="button" onClick={onBack} style={{ marginBottom: 16 }}>
            ← Back
          </button>
        )}
        <h2 style={{ fontSize: 15, marginTop: 0 }}>Knowledge base (assistant_faq)</h2>
        <p style={{ color: 'var(--text-dim)', fontSize: 13, maxWidth: 700 }}>
          FAQs, plus course/fee/batch text elsewhere in the demo store, are what{' '}
          <code>DocumentIngestionService</code> chunks and embeds into Elasticsearch. Add or edit a
          FAQ below, then hit <b>Reindex</b> — nothing you change here is retrievable by the assistant
          until you do.
        </p>

        <div style={{ display: 'flex', gap: 8, margin: '12px 0' }}>
          <button onClick={loadFaqs}>Load FAQs</button>
          <button className="secondary" onClick={reindex}>
            Reindex knowledge base
          </button>
        </div>

        {status.text && (
          <div
            style={{
              fontSize: 12,
              marginBottom: 12,
              color: status.kind === 'err' ? 'var(--danger)' : status.kind === 'ok' ? 'var(--success)' : 'var(--text-dim)',
            }}
          >
            {status.text}
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
          <div>
            <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>{editingId ? `Editing FAQ #${editingId}` : 'New FAQ'}</h3>
            <label>Category</label>
            <input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
            <label>Question</label>
            <input value={form.question} onChange={(e) => setForm({ ...form, question: e.target.value })} />
            <label>Answer</label>
            <textarea
              style={{ minHeight: 90 }}
              value={form.answer}
              onChange={(e) => setForm({ ...form, answer: e.target.value })}
            />
            <label style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <input
                type="checkbox"
                style={{ width: 'auto' }}
                checked={form.active}
                onChange={(e) => setForm({ ...form, active: e.target.checked })}
              />
              Active
            </label>
            <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
              <button onClick={submitForm}>{editingId ? 'Update' : 'Create'}</button>
              {editingId && (
                <button
                  className="secondary"
                  onClick={() => {
                    setEditingId(null);
                    setForm(emptyForm);
                  }}
                >
                  Cancel
                </button>
              )}
            </div>
          </div>

          <div>
            <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>Existing FAQs ({faqs.length})</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {faqs.map((f) => (
                <div
                  key={f.id}
                  style={{
                    border: '1px solid var(--border)',
                    borderRadius: 8,
                    padding: 10,
                    background: 'var(--panel-2)',
                  }}
                >
                  <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>
                    #{f.id} · {f.category} {f.active ? '' : '· inactive'}
                  </div>
                  <div style={{ fontSize: 13, fontWeight: 600, marginTop: 4 }}>{f.question}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-dim)', marginTop: 4 }}>{f.answer}</div>
                  <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
                    <button
                      className="secondary"
                      onClick={() => {
                        setEditingId(f.id);
                        setForm({
                          category: f.category,
                          question: f.question,
                          answer: f.answer,
                          active: f.active,
                        });
                      }}
                    >
                      Edit
                    </button>
                    <button className="secondary" onClick={() => removeFaq(f.id)}>
                      Delete
                    </button>
                  </div>
                </div>
              ))}
              {faqs.length === 0 && (
                <div style={{ color: 'var(--text-dim)', fontSize: 12 }}>
                  No FAQs loaded yet — click "Load FAQs".
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

function describe(e: unknown): string {
  return e instanceof ApiError ? e.message : (e as Error).message;
}
