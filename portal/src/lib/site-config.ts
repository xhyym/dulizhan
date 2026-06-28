import type { Category } from "@/lib/api";

export interface BannerConfigItem {
  image: string;
  title?: string;
  link?: string;
}

export interface SeoConfig {
  home_title: string;
  home_description: string;
  home_keywords: string;
  product_title_template: string;
  product_description_template: string;
  robots: string;
  sitemap_enabled: boolean;
}

const DEFAULT_SEO_CONFIG: SeoConfig = {
  home_title: "",
  home_description: "",
  home_keywords: "",
  product_title_template: "",
  product_description_template: "",
  robots: "",
  sitemap_enabled: true,
};

/**
 * 统一解析站点配置中的 JSON 字段，避免门户多个页面重复处理异常分支。
 */
export function parseConfigJson<T>(value: string | undefined, fallback: T): T {
  if (!value?.trim()) {
    return fallback;
  }

  try {
    return JSON.parse(value) as T;
  } catch {
    return fallback;
  }
}

/**
 * 兼容轮播图旧结构（字符串数组）和新结构（对象数组）。
 */
export function parseBannerImages(value: string | undefined): string[] {
  const parsed = parseConfigJson<unknown>(value, []);

  if (!Array.isArray(parsed)) {
    return [];
  }

  return parsed
    .map((item) => {
      if (typeof item === "string") {
        return item.trim();
      }

      if (
        item &&
        typeof item === "object" &&
        "image" in item &&
        typeof item.image === "string"
      ) {
        return item.image.trim();
      }

      return "";
    })
    .filter(Boolean);
}

export function parseSeoConfig(value: string | undefined): SeoConfig {
  const parsed = parseConfigJson<Partial<SeoConfig>>(value, {});

  return {
    ...DEFAULT_SEO_CONFIG,
    ...parsed,
    sitemap_enabled: parsed.sitemap_enabled !== false,
  };
}

export function getSiteDisplayName(siteConfig: Record<string, string>): string {
  return siteConfig.site_title?.trim() || "OSEN FURNITURE";
}

export function getSiteBaseUrl(): string {
  return (
    process.env.NEXT_PUBLIC_SITE_URL ||
    process.env.NEXT_PUBLIC_WEB_URL ||
    process.env.NEXT_PUBLIC_APP_URL ||
    "http://localhost:3000"
  ).replace(/\/+$/, "");
}

export function buildAbsoluteUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return new URL(normalizedPath, `${getSiteBaseUrl()}/`).toString();
}

export function splitKeywords(rawKeywords: string | undefined): string[] {
  return (rawKeywords || "")
    .split(/[,\n，]+/)
    .map((item) => item.trim())
    .filter(Boolean);
}

export function findCategoryName(
  categories: Category[],
  categoryId: number | undefined
): string {
  if (!categoryId) {
    return "";
  }

  for (const category of categories) {
    if (category.id === categoryId) {
      return category.name;
    }

    if (category.children?.length) {
      const childCategoryName = findCategoryName(category.children, categoryId);
      if (childCategoryName) {
        return childCategoryName;
      }
    }
  }

  return "";
}
