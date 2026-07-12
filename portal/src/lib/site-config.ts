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

export interface AboutPhilosophyItem {
  icon: string;
  title: string;
  desc: string;
}

export interface AboutStatsItem {
  number: string;
  label: string;
}

export interface AboutUsConfig {
  bannerImage: string;
  storyImage: string;
  storyTitle: string;
  storyContent: string;
  philosophy: AboutPhilosophyItem[];
  craftImage: string;
  craftTitle: string;
  craftContent: string;
  stats: AboutStatsItem[];
  ctaText: string;
  ctaButtonText: string;
}

export interface FooterInfo {
  description: string;
  copyright: string;
  links: Array<{ title: string; url: string }>;
}

interface FooterLinkInput {
  title?: string;
  name?: string;
  url?: string;
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

const DEFAULT_ABOUT_US_CONFIG: AboutUsConfig = {
  bannerImage: "",
  storyImage: "",
  storyTitle: "Our Story",
  storyContent:
    "OSEN FURNITURE creates thoughtful furniture for modern homes, commercial spaces, and projects around the world.",
  philosophy: [
    {
      icon: "quality",
      title: "Quality",
      desc: "Reliable materials and careful craftsmanship in every detail.",
    },
    {
      icon: "design",
      title: "Design",
      desc: "Timeless forms made for comfortable, everyday living.",
    },
    {
      icon: "service",
      title: "Service",
      desc: "Practical support from product selection through delivery.",
    },
  ],
  craftImage: "",
  craftTitle: "Crafted With Care",
  craftContent:
    "Each piece is developed with attention to proportion, material, and lasting usability.",
  stats: [],
  ctaText: "Find Furniture For Your Space",
  ctaButtonText: "Explore Our Collection",
};

/**
 * 门户展示使用的兜底配置。后台尚未保存相应字段时，页面应保持可访问而不是中断服务端渲染。
 */
export const DEFAULT_SITE_CONFIG: Readonly<Record<string, string>> = {
  site_title: "OSEN FURNITURE",
  hero_tagline: "OSEN FURNITURE",
  hero_title: "Furniture For Modern Living",
  hero_subtitle:
    "Thoughtfully designed furniture for homes, projects, and commercial spaces.",
  banner_images: "[]",
  about_us: JSON.stringify(DEFAULT_ABOUT_US_CONFIG),
  footer_info: JSON.stringify({
    description:
      "Handcrafted furniture for modern living. Simple, functional, and beautiful.",
    copyright: "© OSEN FURNITURE. All rights reserved.",
    links: [],
  }),
  social_links: "{}",
  seo_config: "{}",
  analytics_config: "{}",
};

/**
 * 合并后台站点配置与门户默认值；空字符串同样按未配置处理，避免旧数据导致页面异常。
 */
export function normalizeSiteConfig(
  siteConfig?: Record<string, string> | null
): Record<string, string> {
  const normalizedConfig = { ...DEFAULT_SITE_CONFIG };

  for (const [key, value] of Object.entries(siteConfig || {})) {
    if (typeof value !== "string") {
      continue;
    }

    if (value.trim() || !(key in DEFAULT_SITE_CONFIG)) {
      normalizedConfig[key] = value;
    }
  }

  return normalizedConfig;
}

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

export function resolvePageBannerImage(
  siteConfig: Record<string, string>,
  imageKey?: string
): string {
  const configuredImage = imageKey ? siteConfig[imageKey]?.trim() : "";
  if (configuredImage) {
    return configuredImage;
  }

  const images = parseBannerImages(siteConfig.banner_images);
  if (!images.length) {
    return "";
  }

  return images.length > 1 ? images[1] : images[0];
}

export function parseSeoConfig(value: string | undefined): SeoConfig {
  const parsedValue = parseConfigJson<Partial<SeoConfig> | null>(value, null);
  const parsed =
    parsedValue && typeof parsedValue === "object" && !Array.isArray(parsedValue)
      ? parsedValue
      : {};

  return {
    ...DEFAULT_SEO_CONFIG,
    ...parsed,
    sitemap_enabled: parsed.sitemap_enabled !== false,
  };
}

/**
 * 解析关于我们配置，并为后台未保存的内容提供可展示的默认文案。
 */
export function parseAboutUsConfig(value: string | undefined): AboutUsConfig {
  const parsedValue = parseConfigJson<Partial<AboutUsConfig> | null>(value, null);
  const parsed =
    parsedValue && typeof parsedValue === "object" && !Array.isArray(parsedValue)
      ? parsedValue
      : {};
  const philosophy = Array.isArray(parsed.philosophy)
    ? parsed.philosophy
        .filter((item) => item && typeof item.icon === "string")
        .map((item) => ({
          icon: item.icon.trim() || "quality",
          title: item.title?.trim() || "Quality",
          desc: item.desc?.trim() || "",
        }))
    : DEFAULT_ABOUT_US_CONFIG.philosophy;
  const stats = Array.isArray(parsed.stats)
    ? parsed.stats
        .filter((item) => item && (item.number?.trim() || item.label?.trim()))
        .map((item) => ({
          number: item.number?.trim() || "",
          label: item.label?.trim() || "",
        }))
    : DEFAULT_ABOUT_US_CONFIG.stats;

  return {
    ...DEFAULT_ABOUT_US_CONFIG,
    ...parsed,
    bannerImage: parsed.bannerImage?.trim() || "",
    storyImage: parsed.storyImage?.trim() || "",
    storyTitle: parsed.storyTitle?.trim() || DEFAULT_ABOUT_US_CONFIG.storyTitle,
    storyContent:
      parsed.storyContent?.trim() || DEFAULT_ABOUT_US_CONFIG.storyContent,
    philosophy: philosophy.length
      ? philosophy
      : DEFAULT_ABOUT_US_CONFIG.philosophy,
    craftImage: parsed.craftImage?.trim() || "",
    craftTitle: parsed.craftTitle?.trim() || DEFAULT_ABOUT_US_CONFIG.craftTitle,
    craftContent:
      parsed.craftContent?.trim() || DEFAULT_ABOUT_US_CONFIG.craftContent,
    stats,
    ctaText: parsed.ctaText?.trim() || DEFAULT_ABOUT_US_CONFIG.ctaText,
    ctaButtonText:
      parsed.ctaButtonText?.trim() || DEFAULT_ABOUT_US_CONFIG.ctaButtonText,
  };
}

/**
 * Footer 配置解析失败时保持站点底部可用，避免单个 JSON 字段影响整个门户页面。
 */
export function parseFooterInfo(value: string | undefined): FooterInfo {
  const parsedValue = parseConfigJson<
    Partial<Omit<FooterInfo, "links">> & { links?: FooterLinkInput[] } | null
  >(value, null);
  const parsed =
    parsedValue && typeof parsedValue === "object" && !Array.isArray(parsedValue)
      ? parsedValue
      : {};
  const links = Array.isArray(parsed.links)
    ? parsed.links
        .map((link) => ({
          title: link.title?.trim() || link.name?.trim() || "",
          url: link.url?.trim() || "",
        }))
        .filter((link) => Boolean(link.title && link.url))
    : [];

  return {
    description:
      parsed.description?.trim() ||
      "Handcrafted furniture for modern living. Simple, functional, and beautiful.",
    copyright:
      parsed.copyright?.trim() || "© OSEN FURNITURE. All rights reserved.",
    links,
  };
}

/**
 * 社交媒体配置只保留非空字符串地址，防止异常 JSON 或空字段生成无效跳转。
 */
export function parseSocialLinks(value: string | undefined): Record<string, string> {
  const parsedValue = parseConfigJson<Record<string, string> | null>(value, null);
  const parsed =
    parsedValue && typeof parsedValue === "object" && !Array.isArray(parsedValue)
      ? parsedValue
      : {};

  return Object.fromEntries(
    Object.entries(parsed).filter(([, url]) => typeof url === "string" && Boolean(url.trim()))
  );
}

export function getSiteDisplayName(siteConfig: Record<string, string>): string {
  return siteConfig.site_title?.trim() || "OSEN FURNITURE";
}

export function getSiteBaseUrl(): string {
  return (
    process.env.NEXT_PUBLIC_SITE_URL ||
    process.env.NEXT_PUBLIC_WEB_URL ||
    process.env.NEXT_PUBLIC_APP_URL ||
    "https://osensino.com"
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
