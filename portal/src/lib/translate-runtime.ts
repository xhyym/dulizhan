"use client";

export const DEFAULT_TRANSLATE_LANGUAGE = "english";
export const TRANSLATE_LANGUAGE_STORAGE_KEY = "portal_translate_language";

const TRANSLATE_SCRIPT_ID = "portal-translate-script";
const TRANSLATE_SCRIPT_SRC =
  "https://cdn.staticfile.net/translate.js/4.0.0/translate.min.js";
const LOCAL_HOSTS = new Set(["localhost", "127.0.0.1", "::1"]);

export interface TranslateApi {
  language: {
    setLocal: (language: string) => void;
  };
  selectLanguageTag: {
    show: boolean;
  };
  service: {
    use: (service: string) => void;
  };
  listener: {
    start: () => void;
  };
  changeLanguage: (language: string) => void;
}

export interface TranslateWindow extends Window {
  translate?: TranslateApi;
  translateConfigured?: boolean;
  translateRuntimeEnabled?: boolean;
  translateScriptLoaded?: boolean;
  translateBootPromise?: Promise<TranslateApi | null>;
}

declare global {
  interface Window {
    translate?: TranslateApi;
    translateConfigured?: boolean;
    translateRuntimeEnabled?: boolean;
    translateScriptLoaded?: boolean;
    translateBootPromise?: Promise<TranslateApi | null>;
  }
}

interface ResolveTranslateRuntimeEnabledOptions {
  hostname?: string;
  nodeEnv?: string;
  enableInDev?: boolean;
}

function parseBooleanEnvFlag(flag?: string): boolean {
  return flag === "true" || flag === "1";
}

function getBrowserWindow(): TranslateWindow | null {
  if (typeof window === "undefined") {
    return null;
  }
  return window as TranslateWindow;
}

function normalizeHostname(hostname?: string): string {
  return hostname?.trim().toLowerCase() ?? "";
}

function isLocalHostname(hostname?: string): boolean {
  const normalizedHostname = normalizeHostname(hostname);
  return (
    LOCAL_HOSTS.has(normalizedHostname) ||
    normalizedHostname.endsWith(".localhost") ||
    normalizedHostname.endsWith(".local")
  );
}

function configureTranslateInstance(targetWindow: TranslateWindow): TranslateApi | null {
  if (!targetWindow.translate) {
    return null;
  }

  if (!targetWindow.translateConfigured) {
    // 固定以英文内容作为站点原文，避免插件首次加载时自行推断源语种。
    targetWindow.translate.language.setLocal(DEFAULT_TRANSLATE_LANGUAGE);
    targetWindow.translate.selectLanguageTag.show = false;
    targetWindow.translate.service.use("client.edge");
    targetWindow.translate.listener.start();
    targetWindow.translateConfigured = true;
  }

  targetWindow.translateScriptLoaded = true;
  return targetWindow.translate;
}

function getTranslateScriptElement(document: Document): HTMLScriptElement | null {
  const scriptElement = document.getElementById(TRANSLATE_SCRIPT_ID);
  if (!scriptElement) {
    return null;
  }
  return scriptElement as HTMLScriptElement;
}

export function normalizeTranslateLanguage(language?: string | null): string {
  const normalizedLanguage = language?.trim().toLowerCase();
  if (!normalizedLanguage) {
    return DEFAULT_TRANSLATE_LANGUAGE;
  }
  return normalizedLanguage;
}

export function resolveTranslateRuntimeEnabled(
  options: ResolveTranslateRuntimeEnabledOptions
): boolean {
  const { hostname, nodeEnv, enableInDev = false } = options;

  if (isLocalHostname(hostname)) {
    return enableInDev;
  }

  if (normalizeHostname(hostname)) {
    return true;
  }

  return nodeEnv === "production" || enableInDev;
}

export function shouldBootTranslateOnLoad(language?: string | null): boolean {
  return normalizeTranslateLanguage(language) !== DEFAULT_TRANSLATE_LANGUAGE;
}

export function readStoredTranslateLanguage(): string {
  const targetWindow = getBrowserWindow();
  if (!targetWindow) {
    return DEFAULT_TRANSLATE_LANGUAGE;
  }

  try {
    return normalizeTranslateLanguage(
      targetWindow.localStorage.getItem(TRANSLATE_LANGUAGE_STORAGE_KEY)
    );
  } catch {
    return DEFAULT_TRANSLATE_LANGUAGE;
  }
}

export function persistTranslateLanguage(language?: string | null): string {
  const normalizedLanguage = normalizeTranslateLanguage(language);
  const targetWindow = getBrowserWindow();

  if (!targetWindow) {
    return normalizedLanguage;
  }

  try {
    targetWindow.localStorage.setItem(
      TRANSLATE_LANGUAGE_STORAGE_KEY,
      normalizedLanguage
    );
  } catch {
    return normalizedLanguage;
  }

  return normalizedLanguage;
}

export function resolveBrowserTranslateRuntimeEnabled(): boolean {
  const targetWindow = getBrowserWindow();
  if (!targetWindow) {
    return false;
  }

  if (typeof targetWindow.translateRuntimeEnabled === "boolean") {
    return targetWindow.translateRuntimeEnabled;
  }

  const runtimeEnabled = resolveTranslateRuntimeEnabled({
    hostname: targetWindow.location.hostname,
    nodeEnv: process.env.NODE_ENV,
    enableInDev: parseBooleanEnvFlag(
      process.env.NEXT_PUBLIC_ENABLE_TRANSLATE_IN_DEV
    ),
  });

  targetWindow.translateRuntimeEnabled = runtimeEnabled;
  return runtimeEnabled;
}

export async function ensureTranslateRuntime(): Promise<TranslateApi | null> {
  const targetWindow = getBrowserWindow();
  if (!targetWindow) {
    return null;
  }

  if (!resolveBrowserTranslateRuntimeEnabled()) {
    return null;
  }

  const configuredTranslate = configureTranslateInstance(targetWindow);
  if (configuredTranslate) {
    return configuredTranslate;
  }

  if (targetWindow.translateBootPromise) {
    return targetWindow.translateBootPromise;
  }

  const existingScript = getTranslateScriptElement(targetWindow.document);
  const scriptElement = existingScript ?? targetWindow.document.createElement("script");

  if (!existingScript) {
    scriptElement.id = TRANSLATE_SCRIPT_ID;
    scriptElement.async = true;
    scriptElement.src = TRANSLATE_SCRIPT_SRC;
  }

  targetWindow.translateBootPromise = new Promise<TranslateApi | null>(
    (resolve, reject) => {
      const handleLoad = () => {
        try {
          const translateInstance = configureTranslateInstance(targetWindow);
          if (!translateInstance) {
            reject(new Error("translate.js 脚本已加载，但未找到 window.translate 实例"));
            return;
          }
          resolve(translateInstance);
        } catch (error) {
          reject(error);
        }
      };

      const handleError = () => {
        reject(new Error("translate.js 脚本加载失败"));
      };

      scriptElement.addEventListener("load", handleLoad, { once: true });
      scriptElement.addEventListener("error", handleError, { once: true });

      if (!existingScript) {
        targetWindow.document.head.appendChild(scriptElement);
        return;
      }

      if (targetWindow.translate) {
        handleLoad();
      }
    }
  ).finally(() => {
    targetWindow.translateBootPromise = undefined;
  });

  return targetWindow.translateBootPromise;
}

export async function changeTranslateLanguage(
  language?: string | null
): Promise<string> {
  const normalizedLanguage = persistTranslateLanguage(language);
  const targetWindow = getBrowserWindow();

  if (!targetWindow) {
    return normalizedLanguage;
  }

  if (normalizedLanguage === DEFAULT_TRANSLATE_LANGUAGE) {
    if (targetWindow.translate) {
      targetWindow.translate.changeLanguage(DEFAULT_TRANSLATE_LANGUAGE);
    }
    return normalizedLanguage;
  }

  const translateInstance = await ensureTranslateRuntime();
  if (!translateInstance) {
    return normalizedLanguage;
  }

  translateInstance.changeLanguage(normalizedLanguage);
  return normalizedLanguage;
}

export async function bootStoredTranslateLanguage(): Promise<string> {
  const storedLanguage = readStoredTranslateLanguage();
  if (!resolveBrowserTranslateRuntimeEnabled()) {
    return storedLanguage;
  }

  if (!shouldBootTranslateOnLoad(storedLanguage)) {
    return storedLanguage;
  }

  return changeTranslateLanguage(storedLanguage);
}
