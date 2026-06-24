"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

interface HeroCarouselProps {
  images: string[];
  tagline: string;
  title: string;
  subtitle: string;
}

export default function HeroCarousel({ images, tagline, title, subtitle }: HeroCarouselProps) {
  const [current, setCurrent] = useState(0);

  useEffect(() => {
    if (images.length <= 1) return;
    const timer = setInterval(() => {
      setCurrent((prev) => (prev + 1) % images.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [images.length]);

  return (
    <section className="relative h-screen min-h-[600px] flex items-center justify-center overflow-hidden">
      {/* 图片层 */}
      {images.map((img, i) => (
        <div
          key={i}
          className="absolute inset-0 transition-opacity duration-1000"
          style={{
            opacity: i === current ? 1 : 0,
            background: `
              linear-gradient(to right, rgba(0,0,0,0.3), rgba(0,0,0,0.1)),
              linear-gradient(to left, rgba(0,0,0,0.3), rgba(0,0,0,0.1)),
              linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.1)),
              linear-gradient(to top, rgba(0,0,0,0.4), rgba(0,0,0,0.1)),
              url(${img}) center/cover no-repeat
            `,
          }}
        />
      ))}
      <div className="absolute inset-0 hero-overlay pointer-events-none" />

      {/* 文案 */}
      <div className="relative z-10 text-center text-white">
        <p className="text-sm font-light tracking-[4px] uppercase mb-4 opacity-90">
          {tagline}
        </p>
        <h1 className="text-4xl md:text-6xl font-light tracking-[8px] uppercase mb-6 leading-tight">
          {title}
        </h1>
        <p className="text-base font-light tracking-wider mb-10 opacity-90">
          {subtitle}
        </p>
        <Link
          href="/products"
          className="inline-block px-12 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-white text-[#1a1a1a] hover:bg-[#1a1a1a] hover:text-white transition-all duration-300"
        >
          Shop Now
        </Link>
      </div>

      {/* 指示器 */}
      {images.length > 1 && (
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-10 flex gap-2">
          {images.map((_, i) => (
            <button
              key={i}
              onClick={() => setCurrent(i)}
              className={`w-2 h-2 rounded-full transition-all duration-300 ${
                i === current ? "bg-white w-6" : "bg-white/50"
              }`}
            />
          ))}
        </div>
      )}
    </section>
  );
}
