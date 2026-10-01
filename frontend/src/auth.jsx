import { createContext, useCallback, useContext, useMemo, useState } from "react";
import { api, isTokenValid, tokenPayload } from "./api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [userId, setUserId] = useState(() => {
    if (!isTokenValid()) {
      localStorage.removeItem("grabseat_token");
      return null;
    }
    const payload = tokenPayload();
    return payload.userId ?? payload.sub;
  });

  const login = useCallback(async (id, password) => {
    const res = await api.login(id, password);
    localStorage.setItem("grabseat_token", res.token);
    setUserId(res.userId);
  }, []);

  const register = useCallback(async (id, password) => {
    const res = await api.register(id, password);
    localStorage.setItem("grabseat_token", res.token);
    setUserId(res.userId);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("grabseat_token");
    setUserId(null);
  }, []);

  const value = useMemo(
    () => ({
      userId,
      authenticated: !!userId && isTokenValid(),
      login,
      register,
      logout,
    }),
    [userId, login, register, logout]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
