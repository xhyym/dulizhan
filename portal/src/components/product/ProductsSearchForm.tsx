"use client";

import { FormEvent, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { buildProductsPageHref } from "@/lib/products-page";

interface ProductsSearchFormProps {
  categoryId?: number;
  keyword?: string;
}

export default function ProductsSearchForm({
  categoryId,
  keyword,
}: ProductsSearchFormProps) {
  const router = useRouter();
  const [searchKeyword, setSearchKeyword] = useState(keyword ?? "");
  const searchInputRef = useRef<HTMLInputElement | null>(null);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    router.push(
      buildProductsPageHref({
        categoryId,
        keyword: searchKeyword,
      })
    );
  };

  useEffect(() => {
    const searchInputElement = searchInputRef.current;
    if (!searchInputElement) {
      return undefined;
    }

    const handleNativeSearch = () => {
      const nextKeyword = searchInputElement.value.trim();

      // 兼容浏览器原生搜索框右侧清空按钮，清空后立即恢复当前分类列表。
      if (!nextKeyword) {
        setSearchKeyword("");
        router.replace(
          buildProductsPageHref({
            categoryId,
          })
        );
      }
    };

    searchInputElement.addEventListener("search", handleNativeSearch);

    return () => {
      searchInputElement.removeEventListener("search", handleNativeSearch);
    };
  }, [categoryId, router]);

  return (
    <form
      onSubmit={handleSubmit}
      className="flex w-full max-w-sm items-center gap-2"
    >
      <input
        ref={searchInputRef}
        type="search"
        name="keyword"
        value={searchKeyword}
        onChange={(event) => setSearchKeyword(event.target.value)}
        placeholder="Search products"
        className="h-10 w-full border border-border bg-white px-4 text-sm text-foreground outline-none transition-colors placeholder:text-muted focus:border-foreground"
      />
      <button
        type="submit"
        className="inline-flex h-10 shrink-0 items-center justify-center border border-foreground px-4 text-[12px] font-medium uppercase tracking-[2px] text-foreground transition-colors hover:bg-foreground hover:text-white"
      >
        Search
      </button>
    </form>
  );
}
