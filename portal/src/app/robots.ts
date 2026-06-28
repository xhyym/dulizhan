import type { MetadataRoute } from "next";
import { portalAPI } from "@/lib/api";
import { buildAbsoluteUrl } from "@/lib/site-config";
import { buildRobotsMetadata } from "@/lib/seo";

export default async function robots(): Promise<MetadataRoute.Robots> {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);

  if (!siteConfig) {
    return {
      rules: {
        userAgent: "*",
        allow: "/",
      },
      sitemap: [buildAbsoluteUrl("/sitemap.xml")],
    };
  }

  return buildRobotsMetadata(siteConfig);
}
