import Link from "next/link";
import { portalAPI } from "@/lib/api";
import { normalizeSiteConfig, resolvePageBannerImage } from "@/lib/site-config";
import { buildPortalImageUrl, PORTAL_IMAGE_PRESETS } from "@/lib/image-url";

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
  const siteConfigResponse = await portalAPI
    .getSiteConfig()
    .catch(() => null);
  const siteConfig = normalizeSiteConfig(siteConfigResponse);
  const bannerImage = resolvePageBannerImage(siteConfig, imageKey);
  const optimizedBannerImage = buildPortalImageUrl(
    bannerImage,
    PORTAL_IMAGE_PRESETS.pageBanner
  );

  return (
    <section className="relative h-[200px] md:h-[250px] flex items-center justify-center">
      {bannerImage ? (
        <div
          className="absolute inset-0"
          style={{
            background: `
              linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.4)),
              url(${optimizedBannerImage}) center/cover no-repeat
            `,
          }}
        />
      ) : (
        <div className="absolute inset-0 bg-[#1a1a1a]" />
      )}
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
