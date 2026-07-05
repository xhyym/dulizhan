"use client";

import { useState, useEffect, useCallback } from "react";
import { Category } from "@/lib/api";
import ProductsCategoryFilter from "./ProductsCategoryFilter";

interface Props {
  categories: Category[];
  activeCategoryId?: number;
  keyword?: string;
}

export default function ProductsFilterMobile({ categories, activeCategoryId, keyword }: Props) {
  const [open, setOpen] = useState(false);
  const close = useCallback(() => setOpen(false), []);

  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  return (
    <>
      <button
        onClick={() => setOpen(true)}
        className="inline-flex items-center gap-2 px-4 py-2.5 border border-border text-sm tracking-wider uppercase"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <line x1="4" y1="6" x2="20" y2="6" />
          <line x1="8" y1="12" x2="20" y2="12" />
          <circle cx="5" cy="12" r="1.5" fill="currentColor" stroke="none" />
          <line x1="12" y1="18" x2="20" y2="18" />
          <circle cx="9" cy="18" r="1.5" fill="currentColor" stroke="none" />
        </svg>
        Filters
      </button>

      {/* 遮罩 */}
      <div
        className={`fixed inset-0 z-[65] bg-black/40 transition-opacity duration-300 md:hidden ${
          open ? "opacity-100" : "opacity-0 pointer-events-none"
        }`}
        onClick={close}
      />

      {/* 侧滑面板 */}
      <div
        className={`fixed top-0 left-0 z-[66] h-full w-[75vw] max-w-[300px] bg-white shadow-2xl transition-transform duration-300 ease-out md:hidden overflow-y-auto ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex items-center justify-between p-4 border-b border-border/40">
          <h3 className="text-[13px] font-medium tracking-wider uppercase">Filters</h3>
          <button onClick={close} className="inline-flex h-10 w-10 items-center justify-center">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className="p-6">
          <h4 className="text-sm font-medium mb-4">Category</h4>
          <ProductsCategoryFilter
            categories={categories}
            activeCategoryId={activeCategoryId}
            keyword={keyword}
            onSelect={close}
          />
        </div>
      </div>
    </>
  );
}
