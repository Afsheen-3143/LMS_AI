import { useState, type FormEvent } from 'react';
import { api, ApiError } from '../api/client';
import { useSession } from '../context/SessionContext';
import Icon from '../components/Icon';

type Mode = 'login' | 'register';

export default function LoginPage({
  onLoggedIn,
  onOpenAdmin,
  onOpenArchitecture,
}: {
  onLoggedIn: () => void;
  onOpenAdmin: () => void;
  onOpenArchitecture: () => void;
}) {
  const s = useSession();
  const [mode, setMode] = useState<Mode>('login');
  const [studentId, setStudentId] = useState(s.studentId || 'S000001');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [baseUrlDraft, setBaseUrlDraft] = useState(s.baseUrl);

  async function signIn(id: string) {
    const result = await api.getDevToken(baseUrlDraft, id, 'STUDENT');
    s.setBaseUrl(baseUrlDraft);
    s.setStudentId(result.studentId);
    s.setRole('STUDENT');
    s.setToken(result.token);
    onLoggedIn();
  }

  async function handleLogin(e: FormEvent) {
    e.preventDefault();
    const id = studentId.trim();
    if (!id) {
      setError('Enter your student ID to continue.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      await signIn(id);
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : (e as Error).message;
      setError(`Couldn't sign you in: ${msg}`);
    } finally {
      setBusy(false);
    }
  }

  async function handleRegister(e: FormEvent) {
    e.preventDefault();
    if (!firstName.trim() || !lastName.trim() || !email.trim()) {
      setError('First name, last name, and email are required.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const student = await api.registerStudent(baseUrlDraft, {
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        phone: phone.trim(),
      });
      await signIn(student.studentId);
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : (e as Error).message;
      setError(`Couldn't create your account: ${msg}`);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-screen">
      <form className="login-card" onSubmit={mode === 'login' ? handleLogin : handleRegister}>
        <div className="brand">
          <div className="logo" aria-hidden="true" />
          <div>
            <b>AI Enrollment Assistant</b>
          </div>
        </div>

        {mode === 'login' ? (
          <>
            <h1>Sign in to continue</h1>
            <p className="sub">Enter your student ID to open your assistant.</p>

            <div className="login-field">
              <label htmlFor="studentId">Student ID</label>
              <input
                id="studentId"
                autoFocus
                value={studentId}
                onChange={(e) => setStudentId(e.target.value)}
                placeholder="e.g. S000001"
              />
            </div>
          </>
        ) : (
          <>
            <h1>Create a demo account</h1>
            <p className="sub">This mints a new student id in the demo store, then signs you in.</p>

            <div className="login-field">
              <label htmlFor="firstName">First name</label>
              <input id="firstName" autoFocus value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="Priya" />
            </div>
            <div className="login-field" style={{ marginTop: 10 }}>
              <label htmlFor="lastName">Last name</label>
              <input id="lastName" value={lastName} onChange={(e) => setLastName(e.target.value)} placeholder="Sharma" />
            </div>
            <div className="login-field" style={{ marginTop: 10 }}>
              <label htmlFor="email">Email</label>
              <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="priya.sharma@example.com" />
            </div>
            <div className="login-field" style={{ marginTop: 10 }}>
              <label htmlFor="phone">Phone (optional)</label>
              <input id="phone" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="9990000003" />
            </div>
          </>
        )}

        {error && <div className="login-error">{error}</div>}

        <button className="login-submit" type="submit" disabled={busy}>
          <Icon name="lock" className="i" />
          {busy ? (mode === 'login' ? 'Signing in…' : 'Creating account…') : mode === 'login' ? 'Log in' : 'Create account & sign in'}
        </button>

        {mode === 'login' ? (
          <p className="login-hint">
            Demo login — no password needed. Try <b>S000001</b> or <b>S000002</b>, or{' '}
            <button
              type="button"
              onClick={() => {
                setMode('register');
                setError('');
              }}
              style={{ background: 'none', border: 0, padding: 0, color: 'var(--violet)', fontWeight: 700, textDecoration: 'underline' }}
            >
              create a new demo account
            </button>
            .
          </p>
        ) : (
          <p className="login-hint">
            Already have an id?{' '}
            <button
              type="button"
              onClick={() => {
                setMode('login');
                setError('');
              }}
              style={{ background: 'none', border: 0, padding: 0, color: 'var(--violet)', fontWeight: 700, textDecoration: 'underline' }}
            >
              Sign in instead
            </button>
            .
          </p>
        )}

        <details
          className="login-advanced"
          open={showAdvanced}
          onToggle={(e) => setShowAdvanced((e.target as HTMLDetailsElement).open)}
        >
          <summary>Advanced: backend URL</summary>
          <div className="login-field" style={{ marginTop: 10 }}>
            <input value={baseUrlDraft} onChange={(e) => setBaseUrlDraft(e.target.value)} />
          </div>
        </details>

        <div className="login-footer-links">
          <button type="button" onClick={onOpenArchitecture}>
            How it works
          </button>
          <button type="button" onClick={onOpenAdmin}>
            Admin console
          </button>
        </div>
      </form>
    </div>
  );
}
