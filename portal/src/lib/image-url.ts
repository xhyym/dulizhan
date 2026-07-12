export type PortalImageFit = "scale-down" | "contain" | "cover" | "crop" | "pad";

export interface PortalImageTransformOptions {
  width?: number;
  height?: number;
  quality?: number;
  fit?: PortalImageFit;
  dpr?: number;
}

const LOCAL_HOSTNAMES = new Set(["localhost", "127.0.0.1", "0.0.0.0"]);

/**
 * 门户图片场景预设，统一走 Cloudflare 图片压缩参数，避免页面各处散落硬编码。
 */
export const PORTAL_IMAGE_PRESETS = {
  heroBanner: { width: 1920, quality: 86 },
  pageBanner: { width: 1600, quality: 84 },
  categoryCard: { width: 720, quality: 82 },
  sectionImage: { width: 1200, quality: 84 },
  productCard: { width: 720, quality: 82 },
  productMain: { width: 1200, quality: 85 },
  productDetail: { width: 1400, quality: 85 },
  productZoom: { width: 1800, quality: 88 },
  thumbnail: { width: 240, quality: 78 },
  cartThumb: { width: 240, quality: 80 },
  logo: { width: 240, quality: 86 },
} satisfies Record<string, PortalImageTransformOptions>;

function normalizeConfiguredOrigin(value: string | undefined): string {
  const trimmedValue = value?.trim();
  if (!trimmedValue) {
    return "";
  }

  try {
    const parsedUrl = new URL(trimmedValue);
    if (LOCAL_HOSTNAMES.has(parsedUrl.hostname)) {
      return "";
    }
    return parsedUrl.origin;
  } catch {
    return "";
  }
}

function resolveImageProxyOrigin(): string {
  return (
    normalizeConfiguredOrigin(process.env.NEXT_PUBLIC_IMAGE_PROXY_BASE_URL) ||
    normalizeConfiguredOrigin(process.env.NEXT_PUBLIC_API_URL) ||
    normalizeConfiguredOrigin(process.env.NEXT_PUBLIC_SITE_URL) ||
    normalizeConfiguredOrigin(process.env.NEXT_PUBLIC_WEB_URL) ||
    normalizeConfiguredOrigin(process.env.NEXT_PUBLIC_APP_URL)
  );
}

function isLocalOrUnsupportedSource(rawUrl: string): boolean {
  if (
    rawUrl.startsWith("data:") ||
    rawUrl.startsWith("blob:") ||
    rawUrl.includes("/cdn-cgi/image/")
  ) {
    return true;
  }

  if (rawUrl.startsWith("/")) {
    return false;
  }

  try {
    const parsedUrl = new URL(rawUrl);
    if (!["http:", "https:"].includes(parsedUrl.protocol)) {
      return true;
    }
    return LOCAL_HOSTNAMES.has(parsedUrl.hostname);
  } catch {
    return true;
  }
}

function serializeTransformOptions(options: PortalImageTransformOptions): string {
  const segments: string[] = ["format=auto", "metadata=none"];

  if (options.width) {
    segments.push(`width=${Math.round(options.width)}`);
  }

  if (options.height) {
    segments.push(`height=${Math.round(options.height)}`);
  }

  if (options.quality) {
    segments.push(`quality=${Math.round(options.quality)}`);
  }

  if (options.fit) {
    segments.push(`fit=${options.fit}`);
  }

  if (options.dpr) {
    segments.push(`dpr=${options.dpr}`);
  }

  return segments.join(",");
}

function encodeSourceForCloudflareImage(rawUrl: string): string {
  if (rawUrl.startsWith("/")) {
    const normalizedPath = rawUrl.replace(/^\/+/, "");
    if (!normalizedPath.includes("?") && !normalizedPath.includes("#")) {
      return normalizedPath;
    }

    return encodeURIComponent(normalizedPath);
  }

  // 远程图片地址一旦包含查询参数，必须整体编码；否则 `?w=...` 会被当成当前请求的 query，
  // Cloudflare 无法正确解析源图地址，线上会直接返回 404。
  return encodeURIComponent(rawUrl);
}

/**
 * 生产环境下优先拼接 Cloudflare 图片压缩地址；本地开发或未配置站点域名时直接回退原图。
 */
export function buildPortalImageUrl(
  rawUrl: string | null | undefined,
  options: PortalImageTransformOptions = {}
): string {
  const trimmedUrl = rawUrl?.trim() || "";
  if (!trimmedUrl) {
    return "";
  }

  const proxyOrigin = resolveImageProxyOrigin();
  if (!proxyOrigin || isLocalOrUnsupportedSource(trimmedUrl)) {
    return trimmedUrl;
  }

  const encodedSource = encodeSourceForCloudflareImage(trimmedUrl);

  return `${proxyOrigin}/cdn-cgi/image/${serializeTransformOptions(options)}/${encodedSource}`;
}

export function buildPortalImageSrcSet(
  rawUrl: string | null | undefined,
  widths: number[],
  options: Omit<PortalImageTransformOptions, "width"> = {}
): string {
  const trimmedUrl = rawUrl?.trim() || "";
  if (!trimmedUrl) {
    return "";
  }

  const normalizedWidths = Array.from(
    new Set(widths.filter((width) => Number.isFinite(width) && width > 0))
  ).sort((left, right) => left - right);

  return normalizedWidths
    .map((width) => `${buildPortalImageUrl(trimmedUrl, { ...options, width })} ${width}w`)
    .join(", ");
}
