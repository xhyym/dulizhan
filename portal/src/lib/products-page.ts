import type { Category } from "@/lib/api";

interface BuildProductsPageHrefOptions {
  categoryId?: number;
  keyword?: string;
}

export function buildProductsPageHref({
  categoryId,
  keyword,
}: BuildProductsPageHrefOptions): string {
  const searchParams = new URLSearchParams();

  if (categoryId) {
    searchParams.set("category", String(categoryId));
  }

  const normalizedKeyword = keyword?.trim();
  if (normalizedKeyword) {
    searchParams.set("keyword", normalizedKeyword);
  }

  const queryString = searchParams.toString();
  return queryString ? `/products?${queryString}` : "/products";
}

export function categoryContainsId(
  category: Pick<Category, "id" | "children">,
  targetCategoryId: number | undefined
): boolean {
  if (!targetCategoryId) {
    return false;
  }

  if (category.id === targetCategoryId) {
    return true;
  }

  return (
    category.children?.some((childCategory) =>
      categoryContainsId(childCategory, targetCategoryId)
    ) ?? false
  );
}

export function getInitialExpandedCategoryIds(
  categories: Pick<Category, "id" | "children">[],
  activeCategoryId: number | undefined
): number[] {
  if (!activeCategoryId) {
    return [];
  }

  return categories
    .filter((category) => categoryContainsId(category, activeCategoryId))
    .map((category) => category.id);
}
