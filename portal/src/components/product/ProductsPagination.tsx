"use client";

import Link from "next/link";
import { buildProductsPageHref } from "@/lib/products-page";

interface Props {
  current: number;
  total: number;
  size: number;
  categoryId?: number;
  keyword?: string;
}

export default function ProductsPagination({
  current,
  total,
  size,
  categoryId,
  keyword,
}: Props) {
  const totalPages = Math.max(1, Math.ceil(total / size));
  if (totalPages <= 1) return null;

  const pages = buildPageNumbers(current, totalPages);

  return (
    <nav className="flex items-center justify-center gap-1 mt-15" aria-label="Pagination">
      {/* Previous */}
      {current > 1 && (
        <Link
          href={buildProductsPageHref({ categoryId, keyword, page: current - 1 })}
          className="inline-flex items-center justify-center w-10 h-10 text-sm text-muted hover:text-foreground transition-colors"
          aria-label="Previous page"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="m15 18-6-6 6-6" />
          </svg>
        </Link>
      )}

      {pages.map((page, i) =>
        page === "..." ? (
          <span key={`dots-${i}`} className="w-10 h-10 flex items-center justify-center text-sm text-muted">
            ...
          </span>
        ) : (
          <Link
            key={page}
            href={buildProductsPageHref({ categoryId, keyword, page: page === 1 ? undefined : page })}
            className={`inline-flex items-center justify-center w-10 h-10 text-sm transition-colors ${
              page === current
                ? "bg-foreground text-white font-medium"
                : "text-muted hover:text-foreground"
            }`}
            aria-label={`Page ${page}`}
            aria-current={page === current ? "page" : undefined}
          >
            {page}
          </Link>
        )
      )}

      {/* Next */}
      {current < totalPages && (
        <Link
          href={buildProductsPageHref({ categoryId, keyword, page: current + 1 })}
          className="inline-flex items-center justify-center w-10 h-10 text-sm text-muted hover:text-foreground transition-colors"
          aria-label="Next page"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="m9 18 6-6-6-6" />
          </svg>
        </Link>
      )}
    </nav>
  );
}

function buildPageNumbers(current: number, totalPages: number): (number | "...")[] {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  const pages: (number | "...")[] = [1];

  if (current > 3) {
    pages.push("...");
  }

  const start = Math.max(2, current - 1);
  const end = Math.min(totalPages - 1, current + 1);

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  if (current < totalPages - 2) {
    pages.push("...");
  }

  pages.push(totalPages);

  return pages;
}
