"use client";

import { useEffect } from "react";
import {
  bootStoredTranslateLanguage,
  resolveBrowserTranslateRuntimeEnabled,
} from "@/lib/translate-runtime";

export default function TranslateProvider() {
  useEffect(() => {
    resolveBrowserTranslateRuntimeEnabled();

    void bootStoredTranslateLanguage().catch((error) => {
      console.warn("初始化页面翻译能力失败：", error);
    });
  }, []);

  return null;
}
