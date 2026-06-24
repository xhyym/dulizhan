import Link from "next/link";
import { portalAPI, Product, Category } from "@/lib/api";
import PageBanner from "@/components/ui/PageBanner";

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
        breadcrumbs={[
          { label: "Home", href: "/" },
          { label: "Shop" },
        ]}
      />

      {/* Content */}
      <section className="py-15 px-6 md:px-15">
        <div className="grid grid-cols-1 md:grid-cols-[240px_1fr] gap-10 max-w-[1200px] mx-auto">
          {/* Sidebar */}
          <aside>
            <h3 className="text-[13px] font-medium tracking-wider uppercase mb-6">
              Filters
            </h3>
            <div className="mb-8">
              <h4 className="text-sm font-medium mb-4">Category</h4>
              <ul className="space-y-3">
                <li>
                  <Link
                    href="/products"
                    className={`text-sm font-light transition-colors ${
                      !categoryId ? "text-foreground font-normal" : "text-muted hover:text-foreground"
                    }`}
                  >
                    All Products
                  </Link>
                </li>
                {categories.map((cat) => (
                  <li key={cat.id}>
                    <Link
                      href={`/products?category=${cat.id}`}
                      className={`text-sm font-light transition-colors ${
                        categoryId === cat.id
                          ? "text-foreground font-normal"
                          : "text-muted hover:text-foreground"
                      }`}
                    >
                      {cat.name}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          </aside>

          {/* Products Grid */}
          <div>
            <div className="flex justify-between items-center mb-8">
              <p className="text-sm text-muted">
                {productsData.total ?? 0} products
              </p>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
              {(productsData.records || []).map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
            {(!productsData.records || productsData.records.length === 0) && (
              <div className="text-center py-20 text-muted">
                No products found
              </div>
            )}
          </div>
        </div>
      </section>
    </>
  );
}

function ProductCard({ product }: { product: Product }) {
  return (
    <Link href={`/products/${product.id}`} className="group cursor-pointer">
      <div className="relative aspect-[3/4] overflow-hidden bg-surface mb-4">
        <img
          src={product.mainImage}
          alt={product.name}
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
