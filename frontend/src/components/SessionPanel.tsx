import { useState } from 'react';
import { api, ApiError } from '../api/client';
import { useSession } from '../context/SessionContext';

export default function SessionPanel({
  extra,
  showRole = false,
}: {
  extra?: React.ReactNode;
  showRole?: boolean;
}) {
  const s = useSession();
  const [status, setStatus] = useState<{ text: string; kind?: 'ok' | 'err' }>({
    text: 'Not connected yet.',
  });

  async function ping() {
    setStatus({ text: 'Pinging…' });
    try {
      const res = await api.ping(s.baseUrl);
      setStatus({
        text: res.ok ? `Backend reachable (HTTP ${res.status}).` : `Backend responded HTTP ${res.status}`,
        kind: res.ok ? 'ok' : 'err',
      });
    } catch (e) {
      setStatus({ text: `Could not reach backend: ${(e as Error).message}`, kind: 'err' });
    }
  }

  async function getToken() {
    if (!s.studentId.trim()) {
      setStatus({ text: 'Enter a student/admin ID first.', kind: 'err' });
      return;
    }
    setStatus({ text: 'Requesting dev token…' });
    try {
      const result = await api.getDevToken(s.baseUrl, s.studentId.trim(), s.role);
      s.setToken(result.token);
      setStatus({ text: `Token minted for ${s.studentId} (${s.role}).`, kind: 'ok' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : (e as Error).message;
      setStatus({ text: `Failed to get token: ${msg}`, kind: 'err' });
    }
  }

  return (
    <aside
      style={{
        width: 300,
        flexShrink: 0,
        borderRight: '1px solid var(--border)',
        padding: 16,
        overflowY: 'auto',
        background: 'var(--panel)',
      }}
    >
      <label>Backend base URL</label>
      <input value={s.baseUrl} onChange={(e) => s.setBaseUrl(e.target.value)} />

      {showRole && (
        <>
          <label>Role</label>
          <select value={s.role} onChange={(e) => s.setRole(e.target.value as 'STUDENT' | 'ADMIN')}>
            <option value="STUDENT">STUDENT</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </>
      )}

      <label>{showRole ? 'Demo user ID' : 'Demo student ID'}</label>
      <input value={s.studentId} onChange={(e) => s.setStudentId(e.target.value)} />
      <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
        <button onClick={getToken}>Get dev token</button>
        <button className="secondary" onClick={ping}>
          Ping API
        </button>
      </div>
      <div style={{ fontSize: 11, color: 'var(--text-dim)', marginTop: 8 }}>
        Mints a token via <code>GET /api/dev/token/&#123;id&#125;</code> — a demo-only endpoint
        that replaces the real host app's register/login/OTP flow. Try{' '}
        <code>S000002</code> too — it already has a demo enrollment seeded.
      </div>

      <label>JWT (Authorization: Bearer …)</label>
      <textarea
        style={{ minHeight: 90, resize: 'vertical' }}
        value={s.token}
        onChange={(e) => s.setToken(e.target.value)}
        placeholder="Click 'Get dev token', or paste one"
      />

      {extra}

      <div
        style={{
          fontSize: 11,
          marginTop: 12,
          color: status.kind === 'err' ? 'var(--danger)' : status.kind === 'ok' ? 'var(--success)' : 'var(--text-dim)',
          wordBreak: 'break-all',
        }}
      >
        {status.text}
      </div>
    </aside>
  );
}
