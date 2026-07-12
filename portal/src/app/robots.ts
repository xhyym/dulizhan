import type { MetadataRoute } from "next";
import { portalAPI } from "@/lib/api";
import { normalizeSiteConfig } from "@/lib/site-config";
import { buildRobotsMetadata } from "@/lib/seo";

export default async function robots(): Promise<MetadataRoute.Robots> {
  const siteConfigResponse = await portalAPI.getSiteConfig().catch(() => null);
  return buildRobotsMetadata(normalizeSiteConfig(siteConfigResponse));
}
