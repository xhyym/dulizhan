import type { Metadata } from "next";
import Image from "next/image";
import Link from "next/link";
import { portalAPI, Product } from "@/lib/api";
import PageBanner from "@/components/ui/PageBanner";
import ProductsCategoryFilter from "@/components/product/ProductsCategoryFilter";
import ProductsSearchForm from "@/components/product/ProductsSearchForm";
import ProductsFilterMobile from "@/components/product/ProductsFilterMobile";
import { buildProductsPageMetadata } from "@/lib/seo";
import { findCategoryName } from "@/lib/site-config";

export async function generateMetadata({
  searchParams,
}: {
  searchParams: Promise<{ category?: string; keyword?: string }>;
}): Promise<Metadata> {
  const params = await searchParams;
  const categoryId = params.category ? Number(params.category) : undefined;
  const keyword = params.keyword?.trim();

  const [siteConfig, categories] = await Promise.all([
    portalAPI.getSiteConfig().catch(() => null),
    portalAPI.getCategories().catch(() => []),
  ]);

  if (!siteConfig) {
    return {
      title: "Products | OSEN FURNITURE",
      description: "Browse the furniture collection of OSEN FURNITURE.",
    };
  }

  const categoryName = categoryId ? findCategoryName(categories, categoryId) : "";
  return buildProductsPageMetadata(siteConfig, { categoryName, categoryId, keyword });
}

export default async function ProductsPage({
  searchParams,
}: {
  searchParams: Promise<{ category?: string; keyword?: string }>;
}) {
  const params = await searchParams;
  const categoryId = params.category ? Number(params.category) : undefined;
  const keyword = params.keyword;

  // 并行获取数据
  const [categoriesRaw, productsDataRaw] = await Promise.all([
    portalAPI.getCategories().catch(() => []),
    portalAPI
      .getProducts({
        current: 1,
        size: 12,
        categoryId,
        keyword,
      })
      .catch(() => null),
  ]);

  const categories = categoriesRaw || [];
  const productsData = productsDataRaw || {
    records: [],
    current: 1,
    size: 12,
    total: 0,
  };

  return (
    <>
      <PageBanner
        title="Our Collection"
        imageKey="products_banner_image"
        breadcrumbs={[
          { label: "Home", href: "/" },
          { label: "Shop" },
        ]}
      />

      {/* Content */}
      <section className="py-10 md:py-15 px-4 md:px-15">
        <div className="grid grid-cols-1 md:grid-cols-[240px_1fr] gap-6 md:gap-10 max-w-[1200px] mx-auto">
          {/* 移动端筛选按钮 */}
          <div className="md:hidden">
            <ProductsFilterMobile categories={categories} activeCategoryId={categoryId} keyword={keyword} />
          </div>

          {/* Sidebar - 桌面端 */}
          <aside className="hidden md:block">
            <FilterSidebar categories={categories} activeCategoryId={categoryId} keyword={keyword} />
          </aside>

          {/* Products Grid */}
          <div>
            <div className="flex flex-col gap-3 mb-6 md:mb-8 md:flex-row md:items-center md:justify-between">
              <p className="text-sm text-muted">
                {productsData.total ?? 0} products
              </p>
              <ProductsSearchForm categoryId={categoryId} keyword={keyword} />
            </div>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
              {(productsData.records || []).map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
            {(!productsData.records || productsData.records.length === 0) && (
              <div className="flex min-h-[320px] flex-col items-center justify-center text-center text-muted">
                <div className="mb-5 flex h-18 w-18 items-center justify-center rounded-2xl border border-border bg-white shadow-[0_14px_32px_rgba(15,23,42,0.05)]">
                  <svg
                    width="34"
                    height="34"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.6"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M5.75 8.75 12 5.5l6.25 3.25" />
                    <path d="M6.5 9.25V15.5c0 .9.48 1.73 1.25 2.16L12 20l4.25-2.34a2.47 2.47 0 0 0 1.25-2.16V9.25" />
                    <path d="M12 20v-6.5" />
                    <path d="M6.5 9.25 12 12l5.5-2.75" />
                    <path d="M9.25 14.25h5.5" />
                  </svg>
                </div>
                <p className="mb-2 text-base font-normal text-foreground">
                  No products found
                </p>
                <p className="text-sm">
                  Try another keyword or choose a different category.
                </p>
              </div>
            )}
          </div>
        </div>
      </section>
    </>
  );
}

function FilterSidebar({
  categories,
  activeCategoryId,
  keyword,
}: {
  categories: import("@/lib/api").Category[];
  activeCategoryId?: number;
  keyword?: string;
}) {
  return (
    <>
      <h3 className="text-[13px] font-medium tracking-wider uppercase mb-6">Filters</h3>
      <div className="mb-8">
        <h4 className="text-sm font-medium mb-4">Category</h4>
        <ProductsCategoryFilter
          categories={categories}
          activeCategoryId={activeCategoryId}
          keyword={keyword}
        />
      </div>
    </>
  );
}

function ProductCard({ product }: { product: Product }) {
  return (
    <Link href={`/products/${product.id}`} className="group cursor-pointer">
      <div className="relative aspect-[3/4] overflow-hidden bg-surface mb-4">
        <Image
          src={product.mainImage}
          alt={product.name}
          fill
          unoptimized
          className="w-full h-full object-cover transition-transform duration-600 group-hover:scale-105"
        />
      </div>
      <p className="text-[15px] font-normal tracking-wider mb-2 text-foreground">
        {product.name}
      </p>
      <p className="text-sm font-medium text-foreground">
        ${product.price}
      </p>
    </Link>
  );
}
