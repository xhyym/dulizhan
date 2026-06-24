"use client";

import { useState } from "react";
import { useAuth, useAuthFetch } from "@/lib/auth";
import { useCart } from "@/lib/cart";

interface AddToCartButtonProps {
  productId: number;
  skus?: Array<{
    id: number;
    specName: string;
    specValue: string;
    price: number;
    stock: number;
  }>;
}

export default function AddToCartButton({
  productId,
  skus = [],
}: AddToCartButtonProps) {
  const [quantity, setQuantity] = useState(1);
  const [selectedSku, setSelectedSku] = useState<number | undefined>(
    skus.length > 0 ? skus[0].id : undefined
  );
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const { user, setShowLogin } = useAuth();
  const { addItem } = useCart();

  const handleAddToCart = async () => {
    if (!user) {
      setShowLogin(true);
      return;
    }

    setLoading(true);
    setMessage("");
    try {
      await addItem(productId, selectedSku, quantity);
      setMessage("Added to cart!");
      setTimeout(() => setMessage(""), 2000);
    } catch (err: any) {
      setMessage(err.message || "Failed to add to cart");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* SKU Selection */}
      {skus.length > 0 && (
        <div className="mb-6">
          <p className="text-sm font-medium mb-3">{skus[0].specName}</p>
          <div className="flex gap-2">
            {skus.map((sku) => (
              <button
                key={sku.id}
                onClick={() => setSelectedSku(sku.id)}
                className={`px-4 py-2 text-sm border transition-colors ${
                  selectedSku === sku.id
                    ? "border-foreground bg-foreground text-white"
                    : "border-border hover:border-foreground"
                }`}
              >
                {sku.specValue}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Quantity */}
      <div className="flex items-center gap-4 mb-6">
        <p className="text-sm font-medium">Qty</p>
        <div className="flex items-center border border-border">
          <button
            className="px-3 py-2 text-sm hover:bg-surface transition-colors"
            onClick={() => setQuantity(Math.max(1, quantity - 1))}
          >
            -
          </button>
          <span className="px-4 py-2 text-sm min-w-[40px] text-center">
            {quantity}
          </span>
          <button
            className="px-3 py-2 text-sm hover:bg-surface transition-colors"
            onClick={() => setQuantity(quantity + 1)}
          >
            +
          </button>
        </div>
      </div>

      {/* Add to Cart Button */}
      <button
        onClick={handleAddToCart}
        disabled={loading}
        className="w-full md:w-auto px-12 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300 disabled:opacity-50"
      >
        {loading ? "Adding..." : "Add to Cart"}
      </button>

      {/* Message */}
      {message && (
        <p
          className={`mt-3 text-sm ${
            message.includes("Failed") ? "text-red-500" : "text-green-600"
          }`}
        >
          {message}
        </p>
      )}
    </div>
  );
}
