import { useState } from 'react';
import { useSession } from './context/SessionContext';
import LoginPage from './pages/LoginPage';
import AssistantPage from './pages/AssistantPage';
import AdminPage from './pages/AdminPage';
import ArchitecturePage from './pages/ArchitecturePage';
import CatalogPage from './pages/CatalogPage';
import Sidebar, { type View } from './components/Sidebar';
import RightPanel from './components/RightPanel';

type Screen = View | 'login' | 'admin';

export default function App() {
  const s = useSession();
  const [screen, setScreen] = useState<Screen>(s.token ? 'assistant' : 'login');
  const [chatKey, setChatKey] = useState(0);

  function logout() {
    s.setToken('');
    s.setSessionId('');
    setScreen('login');
  }

  if (screen === 'login') {
    return (
      <LoginPage
        onLoggedIn={() => setScreen('assistant')}
        onOpenAdmin={() => setScreen('admin')}
        onOpenArchitecture={() => setScreen('architecture')}
      />
    );
  }

  if (screen === 'admin') {
    return <AdminPage onBack={() => setScreen(s.token ? 'assistant' : 'login')} />;
  }

  const showSide = screen === 'assistant';

  return (
    <div className={'app' + (showSide ? '' : ' no-side')}>
      <Sidebar
        view={screen === 'architecture' || screen === 'catalog' ? screen : 'assistant'}
        onNavigate={setScreen}
        onLogout={logout}
      />

      {screen === 'assistant' && <AssistantPage key={chatKey} />}
      {screen === 'architecture' && <ArchitecturePage onBack={() => setScreen('assistant')} />}
      {screen === 'catalog' && <CatalogPage onBack={() => setScreen('assistant')} />}

      {showSide && (
        <RightPanel
          onNewChat={() => {
            s.setSessionId('');
            setChatKey((k) => k + 1);
          }}
          onLogout={logout}
        />
      )}
    </div>
  );
}
