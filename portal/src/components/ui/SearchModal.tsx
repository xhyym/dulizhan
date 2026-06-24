"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function SearchModal({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const [keyword, setKeyword] = useState("");
  const router = useRouter();

  if (!open) return null;

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      router.push(`/products?keyword=${encodeURIComponent(keyword.trim())}`);
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 z-[100] flex items-start justify-center pt-[20vh]"
      onClick={onClose}
    >
      <div className="absolute inset-0 bg-black/50" />
      <div
        className="relative bg-white w-full max-w-2xl mx-4"
        onClick={(e) => e.stopPropagation()}
      >
        <form onSubmit={handleSearch} className="flex">
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search products..."
            className="flex-1 px-6 py-5 text-lg focus:outline-none"
            autoFocus
          />
          <button
            type="submit"
            className="px-6 bg-[#1a1a1a] text-white hover:bg-[#333] transition-colors"
          >
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
          </button>
        </form>
      </div>
    </div>
  );
}
