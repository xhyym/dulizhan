"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";

interface User {
  id: number;
  username: string;
  email: string;
  whatsapp: string;
  avatar: string | null;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (email: string, whatsapp: string) => Promise<void>;
  logout: () => Promise<void>;
  showLogin: boolean;
  setShowLogin: (show: boolean) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [showLogin, setShowLogin] = useState(false);

  // 初始化：从 localStorage 恢复登录状态
  useEffect(() => {
    const savedToken = localStorage.getItem("portal_token");
    if (savedToken) {
      setToken(savedToken);
      fetchUserInfo(savedToken);
    } else {
      setLoading(false);
    }
  }, []);

  const fetchUserInfo = async (authToken: string) => {
    try {
      const res = await fetch(`${API_BASE}/api/portal/auth/me`, {
        headers: { satoken: authToken },
      });
      if (res.ok) {
        const data = await res.json();
        setUser(data.data);
      } else {
        // token 失效
        localStorage.removeItem("portal_token");
        setToken(null);
        setUser(null);
      }
    } catch {
      localStorage.removeItem("portal_token");
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = useCallback(async (email: string, whatsapp: string) => {
    const res = await fetch(`${API_BASE}/api/portal/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, whatsapp }),
    });

    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || "Login failed");
    }

    const data = await res.json();
    const { token: newToken, user: newUser } = data.data;

    localStorage.setItem("portal_token", newToken);
    setToken(newToken);
    setUser(newUser);
    setShowLogin(false);
  }, []);

  const logout = useCallback(async () => {
    if (token) {
      await fetch(`${API_BASE}/api/portal/auth/logout`, {
        method: "POST",
        headers: { satoken: token },
      }).catch(() => {});
    }
    localStorage.removeItem("portal_token");
    setToken(null);
    setUser(null);
  }, [token]);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        logout,
        showLogin,
        setShowLogin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

// 带鉴权的 fetch 封装
export function useAuthFetch() {
  const { token } = useAuth();

  return useCallback(
    async (url: string, options: RequestInit = {}) => {
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
        ...(options.headers as Record<string, string>),
      };
      if (token) {
        headers.satoken = token;
      }
      return fetch(`${API_BASE}${url}`, { ...options, headers });
    },
    [token]
  );
}
