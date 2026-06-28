"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { parseBannerImages } from "@/lib/site-config";

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
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch(`${API_BASE}/api/portal/site-config`)
      .then((res) => res.json())
      .then((data) => {
        const config = data.data || {};
        if (!config.banner_images) {
          setError("站点配置缺失: banner_images，请在后台添加轮播图");
          return;
        }
        try {
          const images = parseBannerImages(config.banner_images);
          if (!images.length) {
            setError("站点配置错误: banner_images 数组为空");
            return;
          }
          setBannerImage(images.length > 1 ? images[1] : images[0]);
        } catch {
          setError("站点配置错误: banner_images JSON 格式异常");
        }
      })
      .catch(() => setError("站点配置加载失败，请检查后端服务"));
  }, []);

  if (error) {
    return (
      <section className="relative h-[200px] md:h-[250px] flex items-center justify-center bg-red-50">
        <p className="text-red-500 text-sm">{error}</p>
      </section>
    );
  }

  if (!bannerImage) {
    return (
      <section className="relative h-[200px] md:h-[250px] flex items-center justify-center bg-gray-100">
        <p className="text-muted text-sm">加载中...</p>
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
