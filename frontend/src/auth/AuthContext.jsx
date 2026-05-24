import { createContext, useCallback, useEffect, useState } from 'react';
import { api } from '../api/client.js';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    api.me()
      .then((u) => { if (!cancelled) setUser(u); })
      .catch(() => { if (!cancelled) setUser(null); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const login = useCallback(async (username, password) => {
    const u = await api.login(username, password);
    setUser(u);
    return u;
  }, []);

  const signup = useCallback(async (username, password, nickname) => {
    const u = await api.signup(username, password, nickname);
    setUser(u);
    return u;
  }, []);

  const logout = useCallback(async () => {
    try { await api.logout(); } catch {}
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
