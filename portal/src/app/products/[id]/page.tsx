import Link from "next/link";
import { portalAPI } from "@/lib/api";
import { notFound } from "next/navigation";
import AddToCartButton from "./AddToCartButton";

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
            <div className="aspect-[3/4] overflow-hidden bg-surface mb-4">
              <img
                src={product.mainImage}
                alt={product.name}
                className="w-full h-full object-cover"
              />
            </div>
            {product.images.length > 0 && (
              <div className="grid grid-cols-4 gap-2">
                {product.images.map((img, index) => (
                  <div
                    key={index}
                    className="aspect-square overflow-hidden bg-surface"
                  >
                    <img
                      src={img}
                      alt={`${product.name} ${index + 1}`}
                      className="w-full h-full object-cover"
                    />
                  </div>
                ))}
              </div>
            )}
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
              <img
                src={product.detailImage}
                alt={`${product.name} details`}
                className="w-full h-auto"
              />
            </div>
          </div>
        </section>
      )}
    </>
  );
}
