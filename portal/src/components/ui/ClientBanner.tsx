"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { parseBannerImages } from "@/lib/site-config";
import { buildPortalImageUrl, PORTAL_IMAGE_PRESETS } from "@/lib/image-url";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface Breadcrumb {
  label: string;
  href?: string;
}

export default function ClientBanner({
  title,
  breadcrumbs,
}: {
  title: string;
  breadcrumbs?: Breadcrumb[];
}) {
  const [bannerImage, setBannerImage] = useState<string | null>(null);

  useEffect(() => {
    fetch(`${API_BASE}/api/portal/site-config`)
      .then((res) => res.json())
      .then((data) => {
        const config = data.data || {};
        const images = parseBannerImages(config.banner_images);
        if (images.length > 0) {
          setBannerImage(images.length > 1 ? images[1] : images[0]);
        }
      })
      .catch(() => setBannerImage(null));
  }, []);

  if (!bannerImage) {
    return (
      <section className="relative h-[200px] md:h-[250px] flex items-center justify-center bg-[#1a1a1a]">
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
  const optimizedBannerImage = buildPortalImageUrl(
    bannerImage,
    PORTAL_IMAGE_PRESETS.pageBanner
  );

  return (
    <section className="relative h-[200px] md:h-[250px] flex items-center justify-center">
      <div
        className="absolute inset-0"
        style={{
          background: `
            linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.4)),
            url(${optimizedBannerImage}) center/cover no-repeat
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
