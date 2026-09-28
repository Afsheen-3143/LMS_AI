import Icon from './Icon';
import { useSession } from '../context/SessionContext';

export type View = 'assistant' | 'architecture' | 'catalog';

const NAV: { view: View; label: string; icon: string; color: string }[] = [
  { view: 'assistant', label: 'Assistant', icon: 'chat', color: 'var(--violet)' },
  { view: 'catalog', label: 'Catalog', icon: 'book', color: 'var(--success)' },
  { view: 'architecture', label: 'How it works', icon: 'grid', color: 'var(--sky)' },
];

export default function Sidebar({
  view,
  onNavigate,
  onLogout,
}: {
  view: View;
  onNavigate: (v: View) => void;
  onLogout: () => void;
}) {
  const s = useSession();

  function toggleTheme() {
    const root = document.documentElement;
    const cur = root.getAttribute('data-theme');
    const dark = cur ? cur === 'dark' : matchMedia('(prefers-color-scheme: dark)').matches;
    root.setAttribute('data-theme', dark ? 'light' : 'dark');
  }

  const initial = (s.studentId || '?').replace(/[^A-Za-z0-9]/g, '').slice(-1).toUpperCase() || 'S';

  return (
    <aside className="nav" aria-label="Main">
      <div className="brand">
        <div className="logo" aria-hidden="true" />
        <div>
          <b>AI Assistant</b>
          <span className="tag">Enrollment help</span>
        </div>
      </div>

      <nav className="nav-list">
        {NAV.map((n) => (
          <button
            key={n.view}
            type="button"
            className="nav-item"
            aria-current={view === n.view ? 'page' : undefined}
            onClick={() => onNavigate(n.view)}
          >
            <span className="chip-i" style={{ background: n.color }}>
              <Icon name={n.icon} />
            </span>
            {n.label}
          </button>
        ))}
      </nav>

      <div className="nav-foot">
        <div className="me" aria-hidden="true">
          {initial}
        </div>
        <div className="who">
          <b>{s.studentId || 'Student'}</b>
          <span>Learner</span>
        </div>
        <button className="theme" type="button" aria-label="Switch colour theme" onClick={toggleTheme}>
          <Icon name="theme" />
        </button>
        <button className="logout-btn" type="button" aria-label="Log out" onClick={onLogout}>
          <Icon name="logout" />
        </button>
      </div>
    </aside>
  );
}
