"use client";

import { useState, useRef, useEffect } from "react";
import { useAuth } from "@/lib/auth";

const COUNTRY_CODES = [
  { code: "+86", country: "China", flag: "🇨🇳" },
  { code: "+1", country: "United States", flag: "🇺🇸" },
  { code: "+44", country: "United Kingdom", flag: "🇬🇧" },
  { code: "+49", country: "Germany", flag: "🇩🇪" },
  { code: "+33", country: "France", flag: "🇫🇷" },
  { code: "+81", country: "Japan", flag: "🇯🇵" },
  { code: "+82", country: "South Korea", flag: "🇰🇷" },
  { code: "+91", country: "India", flag: "🇮🇳" },
  { code: "+55", country: "Brazil", flag: "🇧🇷" },
  { code: "+61", country: "Australia", flag: "🇦🇺" },
  { code: "+1", country: "Canada", flag: "🇨🇦" },
  { code: "+65", country: "Singapore", flag: "🇸🇬" },
  { code: "+66", country: "Thailand", flag: "🇹🇭" },
  { code: "+84", country: "Vietnam", flag: "🇻🇳" },
  { code: "+62", country: "Indonesia", flag: "🇮🇩" },
  { code: "+60", country: "Malaysia", flag: "🇲🇾" },
  { code: "+63", country: "Philippines", flag: "🇵🇭" },
  { code: "+7", country: "Russia", flag: "🇷🇺" },
  { code: "+90", country: "Turkey", flag: "🇹🇷" },
  { code: "+52", country: "Mexico", flag: "🇲🇽" },
  { code: "+971", country: "UAE", flag: "🇦🇪" },
  { code: "+966", country: "Saudi Arabia", flag: "🇸🇦" },
];

export default function LoginModal() {
  const { showLogin, setShowLogin, sendLoginCode, login } = useAuth();
  const [email, setEmail] = useState("");
  const [countryCode, setCountryCode] = useState("+86");
  const [phone, setPhone] = useState("");
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [countdown, setCountdown] = useState(0);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // 点击外部关闭下拉
  useEffect(() => {
    if (!dropdownOpen) return;
    const handleClick = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [dropdownOpen]);

  useEffect(() => {
    if (countdown <= 0) return;
    const timer = window.setTimeout(() => {
      setCountdown((prev) => prev - 1);
    }, 1000);
    return () => window.clearTimeout(timer);
  }, [countdown]);

  if (!showLogin) return null;

  const whatsapp = countryCode + phone;

  const handleSendCode = async () => {
    setError("");
    setSuccessMessage("");

    if (!email) {
      setError("Email is required");
      return;
    }

    if (!phone) {
      setError("WhatsApp number is required");
      return;
    }

    setSendingCode(true);
    try {
      const message = await sendLoginCode(email, whatsapp);
      setSuccessMessage(message);
      setCountdown(60);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Failed to send code";
      setError(message);
    } finally {
      setSendingCode(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccessMessage("");

    if (!email) {
      setError("Email is required");
      return;
    }
    if (!phone) {
      setError("WhatsApp number is required");
      return;
    }
    if (!code) {
      setError("Verification code is required");
      return;
    }

    setLoading(true);
    try {
      await login(email, whatsapp, code);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Login failed";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center"
      onClick={() => setShowLogin(false)}
    >
      <div className="absolute inset-0 bg-black/50" />
      <div
        className="relative bg-white w-full max-w-md mx-4 p-8"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          onClick={() => setShowLogin(false)}
          className="absolute top-4 right-4 text-muted hover:text-foreground transition-colors"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>

        <h2 className="text-xl font-medium tracking-wider uppercase text-center mb-8">
          Login / Register
        </h2>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="mb-4 p-3 bg-red-50 text-red-600 text-sm">
              {error}
            </div>
          )}
          {successMessage && (
            <div className="mb-4 p-3 bg-green-50 text-green-700 text-sm">
              {successMessage}
            </div>
          )}

          <div className="mb-5">
            <label className="block text-sm font-medium mb-2">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="your@email.com"
              className="w-full px-4 py-3 text-sm border border-border focus:outline-none focus:border-foreground transition-colors"
              required
            />
          </div>

          <div className="mb-6">
            <label className="block text-sm font-medium mb-2">WhatsApp</label>
            <div className="flex">
              {/* 自定义国家区号下拉 */}
              <div ref={dropdownRef} className="relative">
                <button
                  type="button"
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                  className="flex items-center gap-1.5 px-3 py-3 text-sm border border-border border-r-0 bg-white hover:border-foreground transition-colors cursor-pointer h-full"
                >
                  <span>{COUNTRY_CODES.find((c) => c.code === countryCode)?.flag}</span>
                  <span className="text-foreground">{countryCode}</span>
                  <svg
                    className={`w-3 h-3 text-muted transition-transform ${dropdownOpen ? "rotate-180" : ""}`}
                    viewBox="0 0 12 12"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path d="M3 4.5L6 7.5L9 4.5" />
                  </svg>
                </button>
                {dropdownOpen && (
                  <div className="absolute top-full left-0 mt-1 w-[200px] max-h-[240px] overflow-y-auto bg-white border border-border shadow-lg z-50">
                    {COUNTRY_CODES.map((item, index) => (
                      <button
                        type="button"
                        key={index}
                        onClick={() => {
                          setCountryCode(item.code);
                          setDropdownOpen(false);
                        }}
                        className={`w-full flex items-center gap-2 px-3 py-2 text-sm text-left hover:bg-surface transition-colors ${
                          item.code === countryCode ? "bg-surface text-foreground" : "text-muted"
                        }`}
                      >
                        <span>{item.flag}</span>
                        <span>{item.country}</span>
                        <span className="ml-auto text-muted-light">{item.code}</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value.replace(/\D/g, ""))}
                placeholder="13800138000"
                className="flex-1 px-4 py-3 text-sm border border-border focus:outline-none focus:border-foreground transition-colors"
                required
              />
            </div>
          </div>

          <div className="mb-6">
            <label className="block text-sm font-medium mb-2">Email Verification Code</label>
            <div className="flex gap-3">
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, "").slice(0, 6))}
                placeholder="Enter 6-digit code"
                className="flex-1 px-4 py-3 text-sm border border-border focus:outline-none focus:border-foreground transition-colors"
                maxLength={6}
                required
              />
              <button
                type="button"
                onClick={handleSendCode}
                disabled={sendingCode || countdown > 0}
                className="min-w-[138px] px-4 py-3 text-[12px] font-medium tracking-[2px] uppercase border border-[#1a1a1a] text-[#1a1a1a] hover:bg-[#1a1a1a] hover:text-white transition-all duration-300 disabled:opacity-50 disabled:hover:bg-transparent disabled:hover:text-[#1a1a1a]"
              >
                {sendingCode
                  ? "Sending..."
                  : countdown > 0
                    ? `${countdown}s`
                    : "Send Code"}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300 disabled:opacity-50"
          >
            {loading ? "Logging in..." : "Login with Code"}
          </button>

          <p className="text-center text-sm text-muted mt-4">
            Enter your email and WhatsApp to receive a login code. A new account will be created automatically on first login.
          </p>
        </form>
      </div>
    </div>
  );
}
