import Link from "next/link";
import { portalAPI } from "@/lib/api";

export default async function AboutPage() {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);

  if (!siteConfig?.about_us) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-500 text-lg">站点配置缺失: about_us，请在后台关于我们页面配置中添加</p>
      </div>
    );
  }

  let aboutData: any;
  try {
    aboutData = JSON.parse(siteConfig.about_us);
  } catch {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-500 text-lg">站点配置错误: about_us JSON 格式异常</p>
      </div>
    );
  }

  const { bannerImage, storyImage, storyTitle, storyContent, philosophy, craftImage, craftTitle, craftContent, stats, ctaText, ctaButtonText } = aboutData;

  // 校验必填字段
  const missing: string[] = [];
  if (!bannerImage) missing.push("bannerImage");
  if (!storyImage) missing.push("storyImage");
  if (!storyTitle) missing.push("storyTitle");
  if (!storyContent) missing.push("storyContent");
  if (!philosophy?.length) missing.push("philosophy");
  if (!craftImage) missing.push("craftImage");
  if (!craftTitle) missing.push("craftTitle");
  if (!craftContent) missing.push("craftContent");
  if (!stats?.length) missing.push("stats");
  if (!ctaText) missing.push("ctaText");
  if (!ctaButtonText) missing.push("ctaButtonText");

  if (missing.length > 0) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-500 text-lg">about_us 配置缺少字段: {missing.join(", ")}</p>
      </div>
    );
  }

  return (
    <>
      {/* Banner */}
      <section className="relative h-[40vh] min-h-[300px] flex items-center justify-center">
        <div
          className="absolute inset-0"
          style={{
            background: `
              linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.4)),
              url(${bannerImage}) center/cover no-repeat
            `,
          }}
        />
        <div className="relative z-10 text-center text-white">
          <p className="text-sm font-light tracking-wider mb-2">
            <Link href="/" className="hover:opacity-80">
              Home
            </Link>{" "}
            &gt; About Us
          </p>
          <h1 className="text-3xl md:text-4xl font-light tracking-[6px] uppercase">
            About Us
          </h1>
        </div>
      </section>

      {/* Story */}
      <section className="py-30 px-6 md:px-15">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-20 max-w-[1200px] mx-auto items-center">
          <div className="aspect-[4/5] overflow-hidden">
            <img src={storyImage} alt={storyTitle} className="w-full h-full object-cover" />
          </div>
          <div>
            <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase mb-6 text-left">
              {storyTitle}
            </h2>
            <p className="text-sm font-light leading-[1.8] text-muted">{storyContent}</p>
          </div>
        </div>
      </section>

      {/* Philosophy */}
      <section className="py-30 px-6 md:px-15 bg-surface">
        <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase text-center mb-15">
          Our Philosophy
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-10 max-w-[1200px] mx-auto">
          {philosophy.map((item: any, index: number) => (
            <div key={index} className="text-center">
              <div className="w-16 h-16 mx-auto mb-6 flex items-center justify-center">
                <PhilosophyIcon icon={item.icon} />
              </div>
              <h3 className="text-lg font-medium tracking-widest uppercase mb-4">{item.title}</h3>
              <p className="text-sm font-light text-muted">{item.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Craft */}
      <section className="py-30 px-6 md:px-15">
        <div className="max-w-[1200px] mx-auto">
          <div className="aspect-[16/9] overflow-hidden mb-10">
            <img src={craftImage} alt={craftTitle} className="w-full h-full object-cover" />
          </div>
          <div className="max-w-[600px] mx-auto text-center">
            <h2 className="text-2xl font-light tracking-[6px] uppercase mb-6">{craftTitle}</h2>
            <p className="text-sm font-light leading-[1.8] text-muted">{craftContent}</p>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="py-20 px-6 md:px-15 bg-[#1a1a1a] text-white">
        <div className="grid grid-cols-3 gap-10 max-w-[800px] mx-auto text-center">
          {stats.map((stat: any, index: number) => (
            <div key={index}>
              <p className="text-3xl md:text-4xl font-light tracking-wider mb-2">{stat.number}</p>
              <p className="text-sm font-light tracking-wider text-white/60 uppercase">{stat.label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className="py-30 px-6 md:px-15 text-center">
        <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase mb-8">{ctaText}</h2>
        <Link
          href="/products"
          className="inline-block px-12 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300"
        >
          {ctaButtonText}
        </Link>
      </section>
    </>
  );
}

function PhilosophyIcon({ icon }: { icon: string }) {
  const icons: Record<string, React.ReactNode> = {
    quality: (
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
      </svg>
    ),
    simple: (
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <circle cx="12" cy="12" r="10" />
      </svg>
    ),
    sustainable: (
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" />
        <path d="M7 13.5s1.5 2 5 2 5-2 5-2" />
        <path d="M7 8.5s1.5 2 5 2 5-2 5-2" />
      </svg>
    ),
    craft: (
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
      </svg>
    ),
    design: (
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M12 19l7-7 3 3-7 7-3-3z" />
        <path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z" />
        <path d="M2 2l7.586 7.586" />
        <circle cx="11" cy="11" r="2" />
      </svg>
    ),
    service: (
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
      </svg>
    ),
  };

  return icons[icon] || icons.quality;
}
