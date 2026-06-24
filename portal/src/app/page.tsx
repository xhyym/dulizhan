import Link from "next/link";
import { portalAPI, Product } from "@/lib/api";
import HeroCarousel from "@/components/ui/HeroCarousel";
import CategoryMarquee from "@/components/ui/CategoryMarquee";

function requireConfig(config: Record<string, string>, key: string): string {
  const val = config[key];
  if (!val) throw new Error(`站点配置缺失: ${key}，请在后台系统设置中添加`);
  return val;
}

function parseJsonConfig(config: Record<string, string>, key: string): any {
  const raw = requireConfig(config, key);
  try {
    return JSON.parse(raw);
  } catch {
    throw new Error(`站点配置格式错误: ${key}，JSON 解析失败`);
  }
}

export default async function HomePage() {
  const [categories, newProducts, siteConfig] = await Promise.all([
    portalAPI.getCategories().catch(() => []),
    portalAPI.getNewProducts(4).catch(() => []),
    portalAPI.getSiteConfig().catch(() => null),
  ]);

  if (!siteConfig) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-500 text-lg">站点配置加载失败，请检查后端服务是否正常</p>
      </div>
    );
  }

  // 解析 banner_images
  const bannerImages = parseJsonConfig(siteConfig, "banner_images") as string[];
  if (!bannerImages.length) {
    throw new Error("站点配置错误: banner_images 数组为空，请在后台添加轮播图");
  }

  // 解析首页文案
  const heroTagline = requireConfig(siteConfig, "hero_tagline");
  const heroTitle = requireConfig(siteConfig, "hero_title");
  const heroSubtitle = requireConfig(siteConfig, "hero_subtitle");

  // 解析 about_us 中的 Our Story
  const aboutUs = parseJsonConfig(siteConfig, "about_us");
  const storyImage = aboutUs.storyImage;
  const storyTitle = aboutUs.storyTitle;
  const storyContent = aboutUs.storyContent;
  if (!storyImage || !storyTitle || !storyContent) {
    throw new Error("站点配置错误: about_us 中缺少 storyImage/storyTitle/storyContent");
  }

  return (
    <>
      <HeroCarousel
        images={bannerImages}
        tagline={heroTagline}
        title={heroTitle}
        subtitle={heroSubtitle}
      />

      {/* Categories */}
      <CategoryMarquee categories={categories} />

      {/* Featured Products */}
      <section className="py-30 px-6 md:px-15 bg-surface">
        <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase text-center mb-4">
          Featured Products
        </h2>
        <p className="text-sm font-light text-muted text-center tracking-wider mb-15">
          Our most popular pieces
        </p>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 max-w-[1200px] mx-auto">
          {newProducts.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
        <div className="text-center mt-15">
          <Link
            href="/products"
            className="inline-block px-12 py-4 text-[13px] font-medium tracking-[3px] uppercase border border-[#1a1a1a] text-[#1a1a1a] hover:bg-[#1a1a1a] hover:text-white transition-all duration-300"
          >
            View All Products
          </Link>
        </div>
      </section>

      {/* Our Story */}
      <section className="py-30 px-6 md:px-15">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-20 max-w-[1200px] mx-auto items-center">
          <div className="aspect-[4/5] overflow-hidden">
            <img
              src={storyImage}
              alt={storyTitle}
              className="w-full h-full object-cover"
            />
          </div>
          <div>
            <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase mb-6 text-left">
              {storyTitle}
            </h2>
            <p className="text-sm font-light leading-[1.8] text-muted mb-8">
              {storyContent}
            </p>
            <Link
              href="/about"
              className="inline-block px-12 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300"
            >
              Learn More
            </Link>
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
