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
  sendLoginCode: (email: string, whatsapp: string) => Promise<string>;
  login: (email: string, whatsapp: string, code: string) => Promise<void>;
  logout: () => Promise<void>;
  showLogin: boolean;
  setShowLogin: (show: boolean) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
const PORTAL_TOKEN_STORAGE_KEY = "portal_token";
const AUTHORIZATION_HEADER_NAME = "Authorization";

interface ApiResponse<T> {
  code: number;
  msg: string;
  data: T;
}

function buildAuthorizationHeaders(token?: string): Record<string, string> {
  if (!token) {
    return {};
  }
  return {
    [AUTHORIZATION_HEADER_NAME]: token,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(() => {
    if (typeof window === "undefined") {
      return null;
    }
    return localStorage.getItem(PORTAL_TOKEN_STORAGE_KEY);
  });
  const [loading, setLoading] = useState(() => token !== null);
  const [showLogin, setShowLogin] = useState(false);

  const fetchUserInfo = useCallback(async (authToken: string) => {
    try {
      const res = await fetch(`${API_BASE}/api/portal/auth/me`, {
        headers: buildAuthorizationHeaders(authToken),
      });
      const data = (await res.json()) as ApiResponse<User>;

      if (res.ok && data.code === 200) {
        setUser(data.data);
        return;
      }

      localStorage.removeItem(PORTAL_TOKEN_STORAGE_KEY);
      setToken(null);
      setUser(null);
    } catch {
      localStorage.removeItem(PORTAL_TOKEN_STORAGE_KEY);
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  // 初始化：从 localStorage 恢复登录状态
  useEffect(() => {
    if (token) {
      const timer = window.setTimeout(() => {
        void fetchUserInfo(token);
      }, 0);
      return () => window.clearTimeout(timer);
    }
  }, [fetchUserInfo, token]);

  const sendLoginCode = useCallback(async (email: string, whatsapp: string) => {
    const res = await fetch(`${API_BASE}/api/portal/auth/send-code`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, whatsapp }),
    });

    const data = (await res.json()) as ApiResponse<null>;
    if (!res.ok || data.code !== 200) {
      throw new Error(data.msg || "Send code failed");
    }

    return data.msg || "Verification code sent";
  }, []);

  const login = useCallback(async (email: string, whatsapp: string, code: string) => {
    const res = await fetch(`${API_BASE}/api/portal/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, whatsapp, code }),
    });

    const data = (await res.json()) as ApiResponse<{ token: string; user: User }>;
    if (!res.ok || data.code !== 200) {
      throw new Error(data.msg || "Login failed");
    }

    const { token: newToken, user: newUser } = data.data;

    localStorage.setItem(PORTAL_TOKEN_STORAGE_KEY, newToken);
    setToken(newToken);
    setUser(newUser);
    setShowLogin(false);
  }, []);

  const logout = useCallback(async () => {
    if (token) {
      await fetch(`${API_BASE}/api/portal/auth/logout`, {
        method: "POST",
        headers: buildAuthorizationHeaders(token),
      }).catch(() => {});
    }
    localStorage.removeItem(PORTAL_TOKEN_STORAGE_KEY);
    setToken(null);
    setUser(null);
  }, [token]);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        sendLoginCode,
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
        headers[AUTHORIZATION_HEADER_NAME] = token;
      }
      return fetch(`${API_BASE}${url}`, { ...options, headers });
    },
    [token]
  );
}
