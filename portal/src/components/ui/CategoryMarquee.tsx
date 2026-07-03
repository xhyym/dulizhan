"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useRef } from "react";
import { Category } from "@/lib/api";
import {
  advanceMarqueeOffset,
  MARQUEE_SPEED_PX_PER_SECOND,
  normalizeMarqueeOffset,
} from "@/lib/category-marquee-motion";

export default function CategoryMarquee({ categories }: { categories: Category[] }) {
  const trackRef = useRef<HTMLDivElement>(null);
  const loopWidthRef = useRef(0);
  const offsetRef = useRef(0);
  const animationFrameRef = useRef<number | null>(null);
  const lastFrameTimeRef = useRef<number | null>(null);
  const isHoveredRef = useRef(false);
  const hasCategories = categories.length > 0;

  function updateOffset(nextOffset: number) {
    offsetRef.current = nextOffset;

    const trackElement = trackRef.current;
    if (!trackElement) return;

    trackElement.style.transform = `translate3d(${nextOffset}px, 0, 0)`;
  }

  // 复制一份实现无缝循环
  const items = [...categories, ...categories];

  useEffect(() => {
    const trackElement = trackRef.current;
    if (!trackElement || !hasCategories) return;

    function measureLoopWidth() {
      const currentTrackElement = trackRef.current;
      if (!currentTrackElement) return;

      loopWidthRef.current = currentTrackElement.scrollWidth / 2;
      updateOffset(
        normalizeMarqueeOffset(offsetRef.current, loopWidthRef.current)
      );
    }

    measureLoopWidth();
    window.addEventListener("resize", measureLoopWidth);

    return () => {
      window.removeEventListener("resize", measureLoopWidth);
    };
  }, [hasCategories, categories]);

  useEffect(() => {
    if (!hasCategories) return;

    function step(timestamp: number) {
      if (lastFrameTimeRef.current === null) {
        lastFrameTimeRef.current = timestamp;
      }

      const elapsedMs = timestamp - lastFrameTimeRef.current;
      lastFrameTimeRef.current = timestamp;

      if (
        !isHoveredRef.current &&
        loopWidthRef.current > 0
      ) {
        updateOffset(
          advanceMarqueeOffset({
            currentOffset: offsetRef.current,
            elapsedMs,
            loopWidth: loopWidthRef.current,
            speedPxPerSecond: MARQUEE_SPEED_PX_PER_SECOND,
          })
        );
      }

      animationFrameRef.current = window.requestAnimationFrame(step);
    }

    animationFrameRef.current = window.requestAnimationFrame(step);

    return () => {
      if (animationFrameRef.current !== null) {
        window.cancelAnimationFrame(animationFrameRef.current);
      }
      lastFrameTimeRef.current = null;
    };
  }, [hasCategories]);

  if (!hasCategories) return null;

  return (
    <section className="py-30 overflow-hidden">
      <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase text-center mb-4">
        Shop by Category
      </h2>
      <p className="text-sm font-light text-muted text-center tracking-wider mb-15">
        Find the perfect piece for your space
      </p>

      <div
        className="group relative"
      >
        {/* 渐变遮罩 */}
        <div className="absolute left-0 top-0 bottom-0 w-20 z-10 bg-gradient-to-r from-white to-transparent pointer-events-none" />
        <div className="absolute right-0 top-0 bottom-0 w-20 z-10 bg-gradient-to-l from-white to-transparent pointer-events-none" />

        <div
          ref={trackRef}
          className="flex"
          style={{
            transform: "translate3d(0px, 0px, 0px)",
            willChange: "transform",
          }}
          onPointerEnter={() => {
            isHoveredRef.current = true;
          }}
          onPointerLeave={() => {
            isHoveredRef.current = false;
          }}
        >
          {items.map((cat, i) => (
            <CategoryCard key={`${cat.id}-${i}`} category={cat} />
          ))}
        </div>
      </div>
    </section>
  );
}

function CategoryCard({ category }: { category: Category }) {
  if (!category.image) {
    return (
      <div className="flex-shrink-0 w-[300px] h-[400px] mx-3 bg-red-50 flex items-center justify-center rounded">
        <p className="text-red-500 text-xs text-center px-4">
          分类「{category.name}」未配置图片
        </p>
      </div>
    );
  }

  return (
    <Link
      href={`/products?category=${category.id}`}
      className="flex-shrink-0 w-[300px] h-[400px] mx-3 relative overflow-hidden group/card cursor-pointer rounded transition-transform duration-300 hover:scale-[1.03]"
    >
      <Image
        src={category.image}
        alt={category.name}
        fill
        unoptimized
        className="w-full h-full object-cover transition-transform duration-500 group-hover/card:scale-110"
      />
      <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent flex flex-col justify-end p-6">
        <span className="text-white text-lg font-normal tracking-widest uppercase">
          {category.name}
        </span>
      </div>
    </Link>
  );
}
