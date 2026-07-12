import type { MetadataRoute } from "next";
import { portalAPI, type Product } from "@/lib/api";
import { buildAbsoluteUrl, normalizeSiteConfig } from "@/lib/site-config";
import { getSitemapBaseEntries, isSitemapEnabled } from "@/lib/seo";

async function getAllProducts(): Promise<Product[]> {
  const pageSize = 200;
  let currentPage = 1;
  let total = Number.POSITIVE_INFINITY;
  const allProducts: Product[] = [];

  while ((currentPage - 1) * pageSize < total) {
    const pageData = await portalAPI
      .getProducts({ current: currentPage, size: pageSize })
      .catch(() => null);

    if (!pageData || pageData.records.length === 0) {
      break;
    }

    allProducts.push(...pageData.records);
    total = pageData.total;
    currentPage += 1;
  }

  return allProducts;
}

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const siteConfigResponse = await portalAPI.getSiteConfig().catch(() => null);
  const siteConfig = normalizeSiteConfig(siteConfigResponse);

  if (!isSitemapEnabled(siteConfig)) {
    return [];
  }

  const products = await getAllProducts();

  return [
    ...getSitemapBaseEntries(),
    ...products.map((product) => ({
      url: buildAbsoluteUrl(`/products/${product.id}`),
      lastModified: product.createTime ? new Date(product.createTime) : new Date(),
      changeFrequency: "weekly" as const,
      priority: 0.8,
    })),
  ];
}
