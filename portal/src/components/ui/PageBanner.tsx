import Link from "next/link";
import { portalAPI } from "@/lib/api";
import { resolvePageBannerImage } from "@/lib/site-config";

interface Breadcrumb {
  label: string;
  href?: string;
}

export default async function PageBanner({
  title,
  breadcrumbs,
  imageKey,
}: {
  title: string;
  breadcrumbs?: Breadcrumb[];
  imageKey?: string;
}) {
  const siteConfig = await portalAPI
    .getSiteConfig()
    .catch(() => null);

  if (!siteConfig) {
    return (
      <section className="relative h-[200px] md:h-[250px] flex items-center justify-center bg-red-50">
        <p className="text-red-500 text-sm">站点配置加载失败，请检查后台设置</p>
      </section>
    );
  }

  const bannerImage = resolvePageBannerImage(siteConfig, imageKey);
  if (!bannerImage) {
    return (
      <section className="relative h-[200px] md:h-[250px] flex items-center justify-center bg-red-50">
        <p className="text-red-500 text-sm">
          站点配置缺失: {imageKey || "banner_images"}，请在后台补充页面背景图
        </p>
      </section>
    );
  }

  return (
    <section className="relative h-[200px] md:h-[250px] flex items-center justify-center">
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
        {breadcrumbs && breadcrumbs.length > 0 && (
          <p className="text-sm font-light tracking-wider mb-2">
            {breadcrumbs.map((crumb, i) => (
              <span key={i}>
                {i > 0 && " > "}
                {crumb.href ? (
                  <Link href={crumb.href} className="hover:opacity-80">
                    {crumb.label}
                  </Link>
                ) : (
                  crumb.label
                )}
              </span>
            ))}
          </p>
        )}
        <h1 className="text-3xl md:text-4xl font-light tracking-[6px] uppercase">
          {title}
        </h1>
      </div>
    </section>
  );
}
