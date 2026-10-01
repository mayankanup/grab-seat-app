import { createContext, useCallback, useContext, useMemo, useState } from "react";
import { api, isTokenValid, tokenPayload } from "./api";

const AuthContext = createContext(null);

function storedProfile() {
  try {
    return JSON.parse(localStorage.getItem("grabseat_user") ?? "null");
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [userId, setUserId] = useState(() => {
    if (!isTokenValid()) {
      localStorage.removeItem("grabseat_token");
      localStorage.removeItem("grabseat_user");
      return null;
    }
    const sub = tokenPayload().sub;
    const id = Number(sub);
    return Number.isNaN(id) ? null : id;
  });
  const [profile, setProfile] = useState(() =>
    isTokenValid() ? storedProfile() : null
  );

  const saveSession = useCallback((res) => {
    localStorage.setItem("grabseat_token", res.token);
    const user = { userId: res.userId, login: res.login, fullName: res.fullName, email: res.email };
    localStorage.setItem("grabseat_user", JSON.stringify(user));
    setUserId(user.userId);
    setProfile(user);
  }, []);

  const login = useCallback(
    async (loginName, password) => {
      saveSession(await api.login(loginName, password));
    },
    [saveSession]
  );

  const register = useCallback(
    async (loginName, password, fullName, email) => {
      saveSession(await api.register(loginName, password, fullName, email));
    },
    [saveSession]
  );

  const logout = useCallback(() => {
    localStorage.removeItem("grabseat_token");
    localStorage.removeItem("grabseat_user");
    setUserId(null);
    setProfile(null);
  }, []);

  const value = useMemo(
    () => ({
      userId,
      profile,
      displayName: profile?.fullName || profile?.login || userId,
      authenticated: !!userId && isTokenValid(),
      login,
      register,
      logout,
    }),
    [userId, profile, login, register, logout]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
