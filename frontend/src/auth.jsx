import { createContext, useCallback, useContext, useMemo, useState } from "react";
import { api, isTokenValid, tokenPayload } from "./api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [userId, setUserId] = useState(() => {
    if (!isTokenValid()) {
      localStorage.removeItem("grabseat_token");
      return null;
    }
    return tokenPayload().sub;
  });

  const login = useCallback(async (id) => {
    const res = await api.login(id);
    localStorage.setItem("grabseat_token", res.token);
    setUserId(res.userId);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("grabseat_token");
    setUserId(null);
  }, []);

  const value = useMemo(
    () => ({ userId, authenticated: !!userId && isTokenValid(), login, logout }),
    [userId, login, logout]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
