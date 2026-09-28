import { useSession } from '../context/SessionContext';
import Icon from './Icon';

const TIPS = [
  'What courses do you offer?',
  'What am I currently enrolled in?',
  'What are the fees for Advanced Java?',
];

export default function RightPanel({ onNewChat, onLogout }: { onNewChat: () => void; onLogout: () => void }) {
  const s = useSession();

  return (
    <aside className="side" aria-label="Session">
      <div className="session-card">
        <small>Signed in as</small>
        <h3>{s.studentId}</h3>
        <small>{s.sessionId ? `Session ${s.sessionId.split('::')[1]?.slice(0, 8) ?? s.sessionId}` : 'No active session yet'}</small>
        <div className="row">
          <button className="btn" type="button" onClick={onNewChat}>
            New chat
          </button>
          <button className="btn ghost" type="button" onClick={onLogout}>
            Log out
          </button>
        </div>
      </div>

      <div className="box">
        <h3>How answers are made</h3>
        <p>Two different paths, picked automatically per question.</p>
        <div className="legend">
          <div className="row">
            <span className="dot" style={{ background: 'var(--mint)' }} />
            <span>
              <b>Catalog &amp; fees</b> — retrieved from the knowledge base
            </span>
          </div>
          <div className="row">
            <span className="dot" style={{ background: 'var(--orange)' }} />
            <span>
              <b>Enrollments &amp; seats</b> — fetched live, scoped to you
            </span>
          </div>
        </div>
      </div>

      <div className="tip">
        <b>Try asking</b>
        {TIPS.map((t) => (
          <div key={t} style={{ marginTop: 6, fontSize: 13.5, display: 'flex', gap: 6, alignItems: 'center' }}>
            <Icon name="chat" className="i" style={{ width: 14, height: 14, flex: 'none' }} />
            <span>{t}</span>
          </div>
        ))}
      </div>
    </aside>
  );
}
