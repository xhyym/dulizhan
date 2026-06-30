import type { Metadata } from "next";
import Link from "next/link";
import { portalAPI } from "@/lib/api";
import { notFound } from "next/navigation";
import AddToCartButton from "./AddToCartButton";
import { buildProductDetailMetadata } from "@/lib/seo";
import { findCategoryName } from "@/lib/site-config";
import ProductGallery from "@/components/product/ProductGallery";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;

  const [product, siteConfig, categories] = await Promise.all([
    portalAPI.getProductDetail(Number(id)).catch(() => null),
    portalAPI.getSiteConfig().catch(() => null),
    portalAPI.getCategories().catch(() => []),
  ]);

  if (!product || !siteConfig) {
    return {
      title: "Product Detail | OSEN FURNITURE",
      description: "Browse premium furniture details at OSEN FURNITURE.",
    };
  }

  const categoryName = findCategoryName(categories, product.categoryId);
  return buildProductDetailMetadata(siteConfig, product, categoryName);
}

export default async function ProductDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const product = await portalAPI
    .getProductDetail(Number(id))
    .catch(() => null);

  if (!product) {
    notFound();
  }

  return (
    <>
      {/* Poster Banner */}
      {product.posterImage && (
        <section className="relative h-[40vh] min-h-[300px] flex items-end">
          <div
            className="absolute inset-0"
            style={{
              background: `
                linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.4)),
                url(${product.posterImage}) center/cover no-repeat
              `,
            }}
          />
          <div className="relative z-10 px-6 md:px-15 pb-8 text-white">
            <p className="text-sm font-light tracking-wider opacity-90">
              <Link href="/" className="hover:opacity-80">
                Home
              </Link>{" "}
              &gt;{" "}
              <Link href="/products" className="hover:opacity-80">
                Shop
              </Link>{" "}
              &gt; {product.name}
            </p>
          </div>
        </section>
      )}

      {/* Product Info */}
      <section className="py-15 px-6 md:px-15">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-10 max-w-[1200px] mx-auto">
          {/* Images */}
          <div>
            <ProductGallery
              productName={product.name}
              mainImage={product.mainImage}
              images={product.images}
              detailImage={product.detailImage}
            />
          </div>

          {/* Details */}
          <div className="py-4">
            <h1 className="text-2xl md:text-3xl font-light tracking-wider uppercase mb-4">
              {product.name}
            </h1>
            <div className="flex items-baseline gap-3 mb-6">
              <span className="text-xl font-medium">
                ${product.price}
              </span>
            </div>
            {product.description && (
              <p className="text-sm font-light leading-relaxed text-muted mb-8">
                {product.description}
              </p>
            )}

            <AddToCartButton productId={product.id} skus={product.skus} />
          </div>
        </div>
      </section>

      {/* Detail Image */}
      {product.detailImage && (
        <section className="py-15 px-6 md:px-15">
          <div className="max-w-[1200px] mx-auto">
            <h2 className="text-2xl font-light tracking-[6px] uppercase text-center mb-10">
              Product Details
            </h2>
            <div className="max-w-[800px] mx-auto">
              <div className="overflow-hidden bg-surface">
                <img
                  src={product.detailImage}
                  alt={`${product.name} details`}
                  className="w-full h-auto"
                />
              </div>
            </div>
          </div>
        </section>
      )}
    </>
  );
}
