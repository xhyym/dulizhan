"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import {
  resolveSwipeAction,
  shouldTreatAsHorizontalDrag,
} from "@/lib/hero-carousel-gesture";

interface HeroCarouselProps {
  images: string[];
  tagline: string;
  title: string;
  subtitle: string;
}

export default function HeroCarousel({ images, tagline, title, subtitle }: HeroCarouselProps) {
  const [current, setCurrent] = useState(0);
  const [isInteracting, setIsInteracting] = useState(false);
  const dragStartRef = useRef({ x: 0, y: 0 });
  const isPointerActiveRef = useRef(false);
  const isHorizontalDragRef = useRef(false);
  const suppressClickRef = useRef(false);
  const hasImages = images.length > 0;

  function showPreviousImage() {
    setCurrent((prev) => (prev - 1 + images.length) % images.length);
  }

  function showNextImage() {
    setCurrent((prev) => (prev + 1) % images.length);
  }

  useEffect(() => {
    if (images.length <= 1 || isInteracting) return;
    const timer = setInterval(() => {
      setCurrent((prev) => (prev + 1) % images.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [images.length, isInteracting]);

  function handlePointerDown(event: React.PointerEvent<HTMLElement>) {
    if (images.length <= 1) return;

    // 不拦截可交互元素上的点击，避免 pointer capture 吞掉 click 事件
    const target = event.target as HTMLElement;
    if (target.closest("a, button")) return;

    dragStartRef.current = { x: event.clientX, y: event.clientY };
    isPointerActiveRef.current = true;
    isHorizontalDragRef.current = false;
    suppressClickRef.current = false;
    setIsInteracting(true);
    event.currentTarget.setPointerCapture(event.pointerId);
  }

  function handlePointerMove(event: React.PointerEvent<HTMLElement>) {
    if (!isPointerActiveRef.current) return;

    const deltaX = event.clientX - dragStartRef.current.x;
    const deltaY = event.clientY - dragStartRef.current.y;

    if (!isHorizontalDragRef.current) {
      isHorizontalDragRef.current = shouldTreatAsHorizontalDrag({ deltaX, deltaY });
    }
  }

  function resetPointerState() {
    isPointerActiveRef.current = false;
    isHorizontalDragRef.current = false;
    setIsInteracting(false);
  }

  function handlePointerUp(event: React.PointerEvent<HTMLElement>) {
    if (!isPointerActiveRef.current) return;

    const deltaX = event.clientX - dragStartRef.current.x;
    const deltaY = event.clientY - dragStartRef.current.y;
    const swipeAction = resolveSwipeAction({ deltaX, deltaY });

    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId);
    }

    resetPointerState();

    if (swipeAction === "previous") {
      suppressClickRef.current = true;
      showPreviousImage();
      return;
    }

    if (swipeAction === "next") {
      suppressClickRef.current = true;
      showNextImage();
    }
  }

  function handlePointerCancel(event: React.PointerEvent<HTMLElement>) {
    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId);
    }

    resetPointerState();
  }

  function handleClickCapture(event: React.MouseEvent<HTMLElement>) {
    if (!suppressClickRef.current) return;

    event.preventDefault();
    event.stopPropagation();
    suppressClickRef.current = false;
  }

  return (
    <section
      className="relative h-screen min-h-[500px] md:min-h-[600px] flex items-center justify-center overflow-hidden select-none cursor-grab active:cursor-grabbing"
      style={{ touchAction: "pan-y" }}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onPointerCancel={handlePointerCancel}
      onClickCapture={handleClickCapture}
    >
      {/* 未配置轮播图时保留稳定的纯色首屏，避免空数组触发轮播计算异常。 */}
      {hasImages ? (
        images.map((img, i) => (
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
        ))
      ) : (
        <div className="absolute inset-0 bg-[#1a1a1a]" />
      )}
      <div className="absolute inset-0 hero-overlay pointer-events-none" />

      {/* 文案 */}
      <div className="relative z-10 text-center text-white">
        <p className="text-sm font-light tracking-[4px] uppercase mb-4 opacity-90">
          {tagline}
        </p>
        <h1 className="text-3xl md:text-6xl font-light tracking-[4px] md:tracking-[8px] uppercase mb-4 md:mb-6 leading-tight">
          {title}
        </h1>
        <p className="text-sm md:text-base font-light tracking-wider mb-8 md:mb-10 opacity-90">
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
