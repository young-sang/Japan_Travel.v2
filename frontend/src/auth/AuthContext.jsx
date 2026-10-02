import { createContext, useCallback, useEffect, useState } from 'react';
import { api, tokenStore } from '../api/client.js';

export const AuthContext = createContext(null);

// 로그인 상태 = localStorage 의 토큰 (D-054). 서버는 토큰을 무효화하지 않으므로
// 로그아웃은 토큰을 지우는 것뿐이다 (D-033).
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setLoading] = useState(true);

  useEffect(() => {
    if (!tokenStore.get()) { setLoading(false); return; }
    let cancelled = false;
    api.me()
      .then((u) => { if (!cancelled) setUser(u); })
      .catch(() => { tokenStore.clear(); if (!cancelled) setUser(null); })   // 만료 · 사용자 삭제
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const accept = useCallback(({ token, user: u }) => {
    tokenStore.set(token);
    setUser(u);
    return u;
  }, []);

  const login = useCallback(async (username, password) => accept(await api.login(username, password)), [accept]);

  const signup = useCallback(
    async (username, password, nickname) => accept(await api.signup(username, password, nickname)),
    [accept],
  );

  const logout = useCallback(async () => {
    tokenStore.clear();
    setUser(null);
  }, []);

  // 계정 관리 (D-056). 닉네임은 응답의 user 로 바꿔 헤더에 바로 반영한다.
  const updateNickname = useCallback(async (nickname) => {
    const u = await api.updateProfile(nickname);
    setUser(u);
    return u;
  }, []);

  const withdraw = useCallback(async (password) => {
    await api.withdraw(password);
    tokenStore.clear();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, signup, logout, updateNickname, withdraw }}>
      {children}
    </AuthContext.Provider>
  );
}
