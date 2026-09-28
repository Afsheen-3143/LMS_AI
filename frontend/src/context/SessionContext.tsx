import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';

interface SessionState {
  baseUrl: string;
  studentId: string;
  role: 'STUDENT' | 'ADMIN';
  token: string;
  sessionId: string;
}

interface SessionContextValue extends SessionState {
  setBaseUrl: (v: string) => void;
  setStudentId: (v: string) => void;
  setRole: (v: 'STUDENT' | 'ADMIN') => void;
  setToken: (v: string) => void;
  setSessionId: (v: string) => void;
}

const STORAGE_KEY = 'ai-assistant-react-ui';

const defaults: SessionState = {
  baseUrl: 'http://localhost:8080',
  studentId: 'S000001',
  role: 'STUDENT',
  token: '',
  sessionId: '',
};

function loadInitial(): SessionState {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
    return { ...defaults, ...saved };
  } catch {
    return defaults;
  }
}

const SessionContext = createContext<SessionContextValue | undefined>(undefined);

export function SessionProvider({ children }: { children: ReactNode }) {
  const initial = useMemo(loadInitial, []);
  const [baseUrl, setBaseUrl] = useState(initial.baseUrl);
  const [studentId, setStudentId] = useState(initial.studentId);
  const [role, setRole] = useState<'STUDENT' | 'ADMIN'>(initial.role);
  const [token, setToken] = useState(initial.token);
  const [sessionId, setSessionId] = useState(initial.sessionId);

  useEffect(() => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ baseUrl, studentId, role, token, sessionId }),
    );
  }, [baseUrl, studentId, role, token, sessionId]);

  const value: SessionContextValue = {
    baseUrl,
    studentId,
    role,
    token,
    sessionId,
    setBaseUrl,
    setStudentId,
    setRole,
    setToken,
    setSessionId,
  };

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionContextValue {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error('useSession must be used within SessionProvider');
  return ctx;
}
