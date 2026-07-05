"use client";

import Link from "next/link";
import { useState } from "react";
import type { Category } from "@/lib/api";
import {
  buildProductsPageHref,
  categoryContainsId,
  getInitialExpandedCategoryIds,
} from "@/lib/products-page";

interface ProductsCategoryFilterProps {
  categories: Category[];
  activeCategoryId: number | undefined;
  keyword?: string;
  /** 移动端抽屉中点击分类后关闭抽屉 */
  onSelect?: () => void;
}

export default function ProductsCategoryFilter({
  categories,
  activeCategoryId,
  keyword,
  onSelect,
}: ProductsCategoryFilterProps) {
  const [expandedCategoryIds, setExpandedCategoryIds] = useState<number[]>(() =>
    getInitialExpandedCategoryIds(categories, activeCategoryId)
  );

  function toggleCategory(categoryId: number) {
    setExpandedCategoryIds((prev) =>
      prev.includes(categoryId)
        ? prev.filter((id) => id !== categoryId)
        : [...prev, categoryId]
    );
  }

  return (
    <ul className="space-y-3">
      <li>
        <Link
          href={buildProductsPageHref({ keyword })}
          onClick={onSelect}
          className={`block rounded-sm px-2 py-1 text-sm transition-colors ${
            !activeCategoryId
              ? "bg-surface font-medium text-foreground"
              : "font-light text-muted hover:text-foreground"
          }`}
        >
          All Products
        </Link>
      </li>

      {categories.map((category) => {
        const isExpanded = expandedCategoryIds.includes(category.id);
        const isActive = activeCategoryId === category.id;
        const isInActiveBranch = categoryContainsId(category, activeCategoryId);
        const secondLevelCategories = category.children?.slice(0, 20) ?? [];

        return (
          <li key={category.id}>
            <div
              className={`flex items-center gap-2 rounded-sm transition-colors ${
                isActive ? "bg-surface" : ""
              }`}
            >
              {secondLevelCategories.length ? (
                <button
                  type="button"
                  onClick={() => toggleCategory(category.id)}
                  className={`inline-flex h-7 w-7 shrink-0 items-center justify-center transition-colors ${
                    isActive || isInActiveBranch
                      ? "text-foreground"
                      : "text-muted hover:text-foreground"
                  }`}
                  aria-label={isExpanded ? "Collapse category" : "Expand category"}
                >
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    className={`transition-transform duration-200 ${
                      isExpanded ? "rotate-90" : ""
                    }`}
                  >
                    <path d="m9 6 6 6-6 6" />
                  </svg>
                </button>
              ) : (
                <span className="inline-block h-6 w-6 shrink-0" />
              )}

              <Link
                href={buildProductsPageHref({
                  categoryId: category.id,
                  keyword,
                })}
                onClick={onSelect}
                className={`block flex-1 rounded-sm px-2 py-1 text-sm transition-colors ${
                  isActive
                    ? "font-medium text-foreground"
                    : isInActiveBranch
                      ? "font-medium text-foreground"
                      : "font-light text-muted hover:text-foreground"
                }`}
              >
                {category.name}
              </Link>
            </div>

            {isExpanded && secondLevelCategories.length ? (
              <ul className="mt-3 space-y-3 border-l border-border/80 pl-8">
                {secondLevelCategories.map((childCategory) => {
                  const isChildActive = activeCategoryId === childCategory.id;

                  return (
                    <li key={childCategory.id}>
                      <Link
                        href={buildProductsPageHref({
                          categoryId: childCategory.id,
                          keyword,
                        })}
                        onClick={onSelect}
                        className={`block rounded-sm px-2 py-1 text-sm transition-colors ${
                          isChildActive
                            ? "bg-surface font-medium text-foreground"
                            : "font-light text-muted hover:text-foreground"
                        }`}
                      >
                        {childCategory.name}
                      </Link>
                    </li>
                  );
                })}
              </ul>
            ) : null}
          </li>
        );
      })}
    </ul>
  );
}
