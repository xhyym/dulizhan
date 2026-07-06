import type { Metadata, MetadataRoute } from "next";
import type { ProductDetail } from "@/lib/api";
import {
  buildAbsoluteUrl,
  getSiteBaseUrl,
  getSiteDisplayName,
  parseSeoConfig,
  splitKeywords,
} from "@/lib/site-config";

interface PageMetadataOptions {
  title: string;
  description: string;
  keywords?: string[];
  siteName: string;
  canonicalPath?: string;
  imageUrl?: string;
}

const DEFAULT_HOME_DESCRIPTION =
  "OSEN FURNITURE provides modern sofas, tables, chairs and custom furniture solutions for wholesale, retail and project orders worldwide.";

function normalizeText(value: string): string {
  return value.replace(/\s+/g, " ").trim();
}

function uniqueKeywords(keywords: Array<string | undefined>): string[] | undefined {
  const deduped = Array.from(
    new Set(
      keywords
        .map((item) => item?.trim())
        .filter((item): item is string => Boolean(item))
    )
  );

  return deduped.length > 0 ? deduped : undefined;
}

function buildMetadataBase(): URL {
  return new URL(`${getSiteBaseUrl()}/`);
}

/**
 * 生成通用页面元信息，统一 canonical、Open Graph 与 Twitter 基础字段。
 */
export function buildPageMetadata({
  title,
  description,
  keywords,
  siteName,
  canonicalPath,
  imageUrl,
}: PageMetadataOptions): Metadata {
  const metadata: Metadata = {
    metadataBase: buildMetadataBase(),
    title: normalizeText(title),
    description: normalizeText(description),
    keywords: uniqueKeywords(keywords || []),
    openGraph: {
      title: normalizeText(title),
      description: normalizeText(description),
      siteName,
      type: "website",
    },
    twitter: {
      card: imageUrl ? "summary_large_image" : "summary",
      title: normalizeText(title),
      description: normalizeText(description),
    },
  };

  if (canonicalPath) {
    const canonicalUrl = buildAbsoluteUrl(canonicalPath);
    metadata.alternates = { canonical: canonicalUrl };
    metadata.openGraph = {
      ...metadata.openGraph,
      url: canonicalUrl,
    };
  }

  if (imageUrl) {
    metadata.openGraph = {
      ...metadata.openGraph,
      images: [{ url: imageUrl }],
    };
    metadata.twitter = {
      ...metadata.twitter,
      images: [imageUrl],
    };
  }

  return metadata;
}

export function buildRootMetadata(siteConfig: Record<string, string>): Metadata {
  const siteName = getSiteDisplayName(siteConfig);
  const seoConfig = parseSeoConfig(siteConfig.seo_config);
  const keywords = splitKeywords(seoConfig.home_keywords);
  const favicon = siteConfig.site_favicon?.trim();

  return {
    metadataBase: buildMetadataBase(),
    title: siteName,
    description: normalizeText(seoConfig.home_description || DEFAULT_HOME_DESCRIPTION),
    keywords: uniqueKeywords(keywords),
    icons: favicon
      ? {
          icon: favicon,
          shortcut: favicon,
          apple: favicon,
        }
      : undefined,
    openGraph: {
      title: siteName,
      description: normalizeText(seoConfig.home_description || DEFAULT_HOME_DESCRIPTION),
      siteName,
      type: "website",
    },
    twitter: {
      card: "summary",
      title: siteName,
      description: normalizeText(seoConfig.home_description || DEFAULT_HOME_DESCRIPTION),
    },
  };
}

export function buildHomeMetadata(siteConfig: Record<string, string>): Metadata {
  const siteName = getSiteDisplayName(siteConfig);
  const seoConfig = parseSeoConfig(siteConfig.seo_config);

  return buildPageMetadata({
    title: seoConfig.home_title || `${siteName} | Modern Furniture Manufacturer`,
    description: seoConfig.home_description || DEFAULT_HOME_DESCRIPTION,
    keywords: splitKeywords(seoConfig.home_keywords),
    siteName,
    canonicalPath: "/",
  });
}

export function buildProductsPageMetadata(
  siteConfig: Record<string, string>,
  options: {
    categoryName?: string;
    categoryId?: number;
    keyword?: string;
  }
): Metadata {
  const siteName = getSiteDisplayName(siteConfig);
  const seoConfig = parseSeoConfig(siteConfig.seo_config);
  const categoryName = options.categoryName?.trim();
  const categoryId = options.categoryId;
  const keyword = options.keyword?.trim();

  let title = `Products | ${siteName}`;
  let description = `Browse the furniture collection of ${siteName}. ${seoConfig.home_description || DEFAULT_HOME_DESCRIPTION}`;
  const queryParams = new URLSearchParams();

  if (categoryName) {
    title = `${categoryName} | Products | ${siteName}`;
    description = `Explore ${categoryName} at ${siteName}. ${seoConfig.home_description || DEFAULT_HOME_DESCRIPTION}`;
    if (categoryId) {
      queryParams.set("category", String(categoryId));
    }
  }

  if (keyword) {
    title = `${keyword} | Search Results | ${siteName}`;
    description = `Browse search results for ${keyword} at ${siteName}. ${seoConfig.home_description || DEFAULT_HOME_DESCRIPTION}`;
    queryParams.set("keyword", keyword);
  }

  if (categoryName && keyword) {
    title = `${keyword} | ${categoryName} | ${siteName}`;
  }

  const keywords = uniqueKeywords([
    ...splitKeywords(seoConfig.home_keywords),
    categoryName,
    keyword,
    "products",
    siteName,
  ]);

  return buildPageMetadata({
    title,
    description,
    keywords,
    siteName,
    canonicalPath: queryParams.toString()
      ? `/products?${queryParams.toString()}`
      : "/products",
  });
}

function applyTemplate(template: string, variables: Record<string, string>): string {
  return template.replace(/\{(\w+)}/g, (_match, key: string) => variables[key] || "");
}

export function buildProductDetailMetadata(
  siteConfig: Record<string, string>,
  product: ProductDetail,
  categoryName: string
): Metadata {
  const siteName = getSiteDisplayName(siteConfig);
  const seoConfig = parseSeoConfig(siteConfig.seo_config);
  const productTitleTemplate = seoConfig.product_title_template || "{productName} | {siteName}";
  const productDescriptionTemplate =
    seoConfig.product_description_template ||
    "Discover {productName} from {siteName}. Premium {categoryName} furniture with custom options, stable quality and global delivery support.";

  const templateVariables = {
    productName: product.name || "",
    siteName,
    categoryName: categoryName || "furniture",
  };

  const title = normalizeText(applyTemplate(productTitleTemplate, templateVariables));
  const description = normalizeText(
    applyTemplate(productDescriptionTemplate, templateVariables) || product.description || DEFAULT_HOME_DESCRIPTION
  );

  return buildPageMetadata({
    title,
    description,
    keywords: uniqueKeywords([
      ...splitKeywords(seoConfig.home_keywords),
      product.name,
      categoryName,
      siteName,
    ]),
    siteName,
    canonicalPath: `/products/${product.id}`,
    imageUrl: product.mainImage || product.posterImage || undefined,
  });
}

export function buildAboutPageMetadata(
  siteConfig: Record<string, string>,
  options?: {
    storyTitle?: string;
    storyContent?: string;
    imageUrl?: string;
  }
): Metadata {
  const siteName = getSiteDisplayName(siteConfig);
  const seoConfig = parseSeoConfig(siteConfig.seo_config);
  const description =
    options?.storyContent?.trim() ||
    `Learn more about ${siteName}, our furniture philosophy, craftsmanship standards and custom service capabilities.`;

  return buildPageMetadata({
    title: `About Us | ${siteName}`,
    description,
    keywords: uniqueKeywords([
      ...splitKeywords(seoConfig.home_keywords),
      "about us",
      "furniture manufacturer",
      siteName,
      options?.storyTitle,
    ]),
    siteName,
    canonicalPath: "/about",
    imageUrl: options?.imageUrl,
  });
}

export function buildContactPageMetadata(siteConfig: Record<string, string>): Metadata {
  const siteName = getSiteDisplayName(siteConfig);
  const seoConfig = parseSeoConfig(siteConfig.seo_config);

  return buildPageMetadata({
    title: `Contact Us | ${siteName}`,
    description: `Contact ${siteName} for furniture catalogs, quotations, customization support and order consultation.`,
    keywords: uniqueKeywords([
      ...splitKeywords(seoConfig.home_keywords),
      "contact us",
      "whatsapp",
      "email",
      siteName,
    ]),
    siteName,
    canonicalPath: "/contact",
  });
}

export function buildInquirySuccessMetadata(siteConfig: Record<string, string>): Metadata {
  const siteName = getSiteDisplayName(siteConfig);
  const seoConfig = parseSeoConfig(siteConfig.seo_config);

  return buildPageMetadata({
    title: `Inquiry Submitted | ${siteName}`,
    description: `Your inquiry has been submitted successfully. ${siteName} will review your request and contact you soon.`,
    keywords: uniqueKeywords([
      ...splitKeywords(seoConfig.home_keywords),
      "inquiry submitted",
      "quotation request",
      siteName,
    ]),
    siteName,
    canonicalPath: "/inquiry/success",
  });
}

/**
 * 将后台可编辑的 robots 文本尽量转换成 Next 可识别的结构。
 */
export function buildRobotsMetadata(siteConfig: Record<string, string>): MetadataRoute.Robots {
  const seoConfig = parseSeoConfig(siteConfig.seo_config);
  const defaultSitemap = buildAbsoluteUrl("/sitemap.xml");
  const robotsText = seoConfig.robots?.trim();

  if (!robotsText) {
    return {
      rules: {
        userAgent: "*",
        allow: "/",
      },
      sitemap: [defaultSitemap],
    };
  }

  const lines = robotsText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  const rules: Array<{ userAgent: string; allow: string[]; disallow: string[] }> = [];
  const sitemaps: string[] = [];
  let currentRule: { userAgent: string; allow: string[]; disallow: string[] } | null = null;

  const pushCurrentRule = () => {
    if (currentRule?.userAgent) {
      rules.push(currentRule);
    }
  };

  for (const line of lines) {
    const separatorIndex = line.indexOf(":");
    if (separatorIndex <= 0) {
      continue;
    }

    const key = line.slice(0, separatorIndex).trim().toLowerCase();
    const value = line.slice(separatorIndex + 1).trim();
    if (!value) {
      continue;
    }

    if (key === "user-agent") {
      pushCurrentRule();
      currentRule = {
        userAgent: value,
        allow: [],
        disallow: [],
      };
      continue;
    }

    if (key === "allow") {
      if (!currentRule) {
        currentRule = { userAgent: "*", allow: [], disallow: [] };
      }
      currentRule.allow.push(value);
      continue;
    }

    if (key === "disallow") {
      if (!currentRule) {
        currentRule = { userAgent: "*", allow: [], disallow: [] };
      }
      currentRule.disallow.push(value);
      continue;
    }

    if (key === "sitemap") {
      sitemaps.push(value.startsWith("http") ? value : buildAbsoluteUrl(value));
    }
  }

  pushCurrentRule();

  if (rules.length === 0) {
    return {
      rules: {
        userAgent: "*",
        allow: "/",
      },
      sitemap: sitemaps.length > 0 ? sitemaps : [defaultSitemap],
    };
  }

  return {
    rules: rules.map((rule) => ({
      userAgent: rule.userAgent,
      allow: rule.allow.length > 0 ? rule.allow : undefined,
      disallow: rule.disallow.length > 0 ? rule.disallow : undefined,
    })),
    sitemap: sitemaps.length > 0 ? sitemaps : [defaultSitemap],
  };
}

export function isSitemapEnabled(siteConfig: Record<string, string>): boolean {
  return parseSeoConfig(siteConfig.seo_config).sitemap_enabled !== false;
}

export function getSitemapBaseEntries(): MetadataRoute.Sitemap {
  return [
    {
      url: buildAbsoluteUrl("/"),
      lastModified: new Date(),
      changeFrequency: "weekly",
      priority: 1,
    },
    {
      url: buildAbsoluteUrl("/products"),
      lastModified: new Date(),
      changeFrequency: "daily",
      priority: 0.9,
    },
    {
      url: buildAbsoluteUrl("/about"),
      lastModified: new Date(),
      changeFrequency: "monthly",
      priority: 0.7,
    },
    {
      url: buildAbsoluteUrl("/contact"),
      lastModified: new Date(),
      changeFrequency: "monthly",
      priority: 0.7,
    },
  ];
}
