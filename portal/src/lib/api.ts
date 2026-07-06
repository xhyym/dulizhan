const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

async function fetchAPI<T>(endpoint: string): Promise<T> {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    cache: "no-store",
  });

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`);
  }

  const json = await res.json();
  return json.data;
}

// 门户端 API
export const portalAPI = {
  // 分类
  getCategories: () => fetchAPI<Category[]>("/api/portal/categories"),

  // 商品
  getProducts: (params?: {
    current?: number;
    size?: number;
    categoryId?: number;
    keyword?: string;
  }) => {
    const searchParams = new URLSearchParams();
    if (params?.current) searchParams.set("current", String(params.current));
    if (params?.size) searchParams.set("size", String(params.size));
    if (params?.categoryId)
      searchParams.set("categoryId", String(params.categoryId));
    if (params?.keyword) searchParams.set("keyword", params.keyword);
    return fetchAPI<PageResult<Product>>(
      `/api/portal/products?${searchParams.toString()}`
    );
  },

  getProductDetail: (id: number) =>
    fetchAPI<ProductDetail>(`/api/portal/products/${id}`),

  getNewProducts: (limit?: number) =>
    fetchAPI<Product[]>(
      `/api/portal/products/new${limit ? `?limit=${limit}` : ""}`
    ),

  // 站点配置
  getSiteConfig: () =>
    fetchAPI<Record<string, string>>("/api/portal/site-config"),

  // 访客记录
  recordVisit: (data: { pageUrl: string }) =>
    fetch(`${API_BASE}/api/portal/visit`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }),
};

// 类型定义
export interface Category {
  id: number;
  name: string;
  image: string | null;
  parentId: number;
  sort: number;
  status: number;
  createTime: string;
  children?: Category[];
}

export interface Product {
  id: number;
  name: string;
  description: string;
  categoryId: number;
  price: number;
  discountPrice: number | null;
  skuCode: string;
  mainImage: string;
  posterImage: string;
  detailImage: string;
  status: number;
  sort: number;
  createTime: string;
}

export interface ProductDetail extends Product {
  images: string[];
  skus: Sku[];
}

export interface Sku {
  id: number;
  skuCode: string;
  specName: string;
  specValue: string;
  price: number;
  stock: number;
  status: number;
}

export interface PageResult<T> {
  records: T[];
  current: number;
  size: number;
  total: number;
}
