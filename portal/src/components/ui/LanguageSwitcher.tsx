"use client";

import { useState, useRef, useEffect } from "react";
import {
  changeTranslateLanguage,
  normalizeTranslateLanguage,
  readStoredTranslateLanguage,
} from "@/lib/translate-runtime";

const LANGUAGES = [
  { code: "english", label: "English", flag: "🇬🇧" },
  { code: "chinese_simplified", label: "简体中文", flag: "🇨🇳" },
  { code: "chinese_traditional", label: "繁體中文", flag: "🇹🇼" },
  { code: "japanese", label: "日本語", flag: "🇯🇵" },
  { code: "korean", label: "한국어", flag: "🇰🇷" },
  { code: "french", label: "Français", flag: "🇫🇷" },
  { code: "german", label: "Deutsch", flag: "🇩🇪" },
  { code: "spanish", label: "Español", flag: "🇪🇸" },
  { code: "italian", label: "Italiano", flag: "🇮🇹" },
  { code: "portuguese", label: "Português", flag: "🇵🇹" },
  { code: "russian", label: "Русский", flag: "🇷🇺" },
  { code: "arabic", label: "العربية", flag: "🇸🇦" },
  { code: "thai", label: "ไทย", flag: "🇹🇭" },
  { code: "vietnamese", label: "Tiếng Việt", flag: "🇻🇳" },
  { code: "dutch", label: "Nederlands", flag: "🇳🇱" },
  { code: "polish", label: "Polski", flag: "🇵🇱" },
  { code: "turkish", label: "Türkçe", flag: "🇹🇷" },
];

export default function LanguageSwitcher({
  scrolled,
}: {
  scrolled: boolean;
}) {
  const [open, setOpen] = useState(false);
  const [current, setCurrent] = useState(() => readStoredTranslateLanguage());
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  const handleChange = (code: string) => {
    const normalizedLanguage = normalizeTranslateLanguage(code);

    setCurrent(normalizedLanguage);
    setOpen(false);

    void changeTranslateLanguage(normalizedLanguage).catch((error) => {
      console.warn("切换页面翻译语言失败：", error);
    });
  };

  return (
    <div className="relative flex items-center" ref={ref}>
      <button
        onClick={() => setOpen(!open)}
        className={`transition-colors ${scrolled ? "text-foreground" : "text-white"}`}
        aria-label="Switch language"
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <circle cx="12" cy="12" r="10" />
          <path d="M2 12h20" />
          <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
        </svg>
      </button>

      {open && (
        <div className="absolute right-0 top-full mt-2 w-48 bg-white shadow-lg z-50 max-h-80 overflow-y-auto">
          <div className="py-1">
            {LANGUAGES.map((lang) => (
              <button
                key={lang.code}
                onClick={() => handleChange(lang.code)}
                className={`w-full text-left px-4 py-2 text-sm flex items-center gap-3 hover:bg-surface transition-colors ${
                  current === lang.code
                    ? "bg-surface font-medium text-foreground"
                    : "text-foreground"
                }`}
              >
                <span className="text-base">{lang.flag}</span>
                <span>{lang.label}</span>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
