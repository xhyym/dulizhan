"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";
import { useAuth, useAuthFetch } from "./auth";

interface CartItem {
  id: number;
  productId: number;
  skuId: number | null;
  quantity: number;
  productName: string;
  mainImage: string;
  price: number;
  discountPrice: number | null;
  skuCode: string | null;
  specName: string | null;
  specValue: string | null;
  skuPrice: number | null;
}

interface CartContextType {
  items: CartItem[];
  itemCount: number;
  loading: boolean;
  addItem: (productId: number, skuId?: number, quantity?: number) => Promise<void>;
  updateItem: (id: number, quantity: number) => Promise<void>;
  removeItem: (id: number) => Promise<void>;
  clearCart: () => Promise<void>;
  refreshCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | null>(null);

interface ApiResponse<T> {
  code: number;
  msg: string;
  data: T;
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  const authFetch = useAuthFetch();

  const itemCount = items.reduce((sum, item) => sum + item.quantity, 0);

  const refreshCart = useCallback(async () => {
    if (!user) {
      setItems([]);
      return;
    }
    setLoading(true);
    try {
      const res = await authFetch("/api/portal/cart");
      if (res.ok) {
        const data = await res.json();
        setItems(data.data || []);
      }
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, [user, authFetch]);

  useEffect(() => {
    void Promise.resolve().then(refreshCart);
  }, [refreshCart]);

  const addItem = useCallback(
    async (productId: number, skuId?: number, quantity = 1) => {
      const res = await authFetch("/api/portal/cart/items", {
        method: "POST",
        body: JSON.stringify({ productId, skuId, quantity }),
      });
      if (!res.ok) {
        const err = (await res.json()) as ApiResponse<null>;
        throw new Error(err.msg || "Failed to add item");
      }
      await refreshCart();
    },
    [authFetch, refreshCart]
  );

  const updateItem = useCallback(
    async (id: number, quantity: number) => {
      const res = await authFetch(
        `/api/portal/cart/items/${id}?quantity=${quantity}`,
        { method: "PUT" }
      );
      if (!res.ok) {
        throw new Error("Failed to update item");
      }
      await refreshCart();
    },
    [authFetch, refreshCart]
  );

  const removeItem = useCallback(
    async (id: number) => {
      const res = await authFetch(`/api/portal/cart/items/${id}`, {
        method: "DELETE",
      });
      if (!res.ok) {
        throw new Error("Failed to remove item");
      }
      await refreshCart();
    },
    [authFetch, refreshCart]
  );

  const clearCart = useCallback(async () => {
    const res = await authFetch("/api/portal/cart", { method: "DELETE" });
    if (!res.ok) {
      throw new Error("Failed to clear cart");
    }
    await refreshCart();
  }, [authFetch, refreshCart]);

  return (
    <CartContext.Provider
      value={{
        items,
        itemCount,
        loading,
        addItem,
        updateItem,
        removeItem,
        clearCart,
        refreshCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within CartProvider");
  }
  return context;
}
