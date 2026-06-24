"use client";

import Link from "next/link";
import { Category } from "@/lib/api";

export default function CategoryMarquee({ categories }: { categories: Category[] }) {
  if (!categories.length) return null;

  // 复制一份实现无缝循环
  const items = [...categories, ...categories];

  return (
    <section className="py-30 overflow-hidden">
      <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase text-center mb-4">
        Shop by Category
      </h2>
      <p className="text-sm font-light text-muted text-center tracking-wider mb-15">
        Find the perfect piece for your space
      </p>

      <div className="group relative">
        {/* 渐变遮罩 */}
        <div className="absolute left-0 top-0 bottom-0 w-20 z-10 bg-gradient-to-r from-white to-transparent pointer-events-none" />
        <div className="absolute right-0 top-0 bottom-0 w-20 z-10 bg-gradient-to-l from-white to-transparent pointer-events-none" />

        <div className="flex animate-marquee group-hover:[animation-play-state:paused]">
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
      className="flex-shrink-0 w-[300px] h-[400px] mx-3 relative overflow-hidden group/card cursor-pointer rounded"
    >
      <img
        src={category.image}
        alt={category.name}
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
