"use client";

import Link from "next/link";
import { useAuth, useAuthFetch } from "@/lib/auth";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";

interface Inquiry {
  id: number;
  inquiryNo: string;
  totalAmount: number;
  status: number;
  createTime: string;
  deliveryDate: string | null;
}

interface InquiryPageData {
  records: Inquiry[];
  current: number;
  size: number;
  total: number;
  pages: number;
}

const STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: "Pending", color: "text-yellow-600" },
  1: { label: "Contacted", color: "text-blue-600" },
  2: { label: "Completed", color: "text-green-600" },
  3: { label: "Cancelled", color: "text-red-600" },
};

function formatDateValue(dateValue?: string | null): string {
  if (!dateValue) {
    return "-";
  }

  const normalizedDateValue = dateValue.replace(/\//g, "-");
  if (normalizedDateValue.includes("T")) {
    return normalizedDateValue.slice(0, 10);
  }

  return normalizedDateValue;
}

export default function OrdersPage() {
  const { user } = useAuth();
  const authFetch = useAuthFetch();
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [loadingData, setLoadingData] = useState(true);
  const [cancellingInquiryId, setCancellingInquiryId] = useState<number | null>(null);
  const [confirmingInquiryId, setConfirmingInquiryId] = useState<number | null>(null);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const [totalCount, setTotalCount] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchInquiries = useCallback(async () => {
    if (!user) {
      return;
    }

    try {
      const searchParams = new URLSearchParams({
        current: String(currentPage),
        size: String(pageSize),
      });

      if (appliedKeyword.trim()) {
        searchParams.set("inquiryNo", appliedKeyword.trim());
      }

      const res = await authFetch(`/api/portal/inquiries?${searchParams.toString()}`);
      if (res.ok) {
        const data = await res.json();
        const pageData = (data.data || {}) as InquiryPageData;
        setInquiries(pageData.records || []);
        setTotalCount(pageData.total || 0);
        setTotalPages(pageData.pages || 0);
      }
    } catch {
      // ignore
    } finally {
      setLoadingData(false);
    }
  }, [appliedKeyword, authFetch, currentPage, pageSize, user]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void fetchInquiries();
    }, 0);

    return () => window.clearTimeout(timer);
  }, [fetchInquiries]);

  const handleCancelInquiry = async (inquiryId: number) => {
    setCancellingInquiryId(inquiryId);
    try {
      const res = await authFetch(`/api/portal/inquiries/${inquiryId}/cancel`, {
        method: "POST",
      });
      const data = await res.json();
      if (!res.ok || data.code !== 200) {
        throw new Error(data.msg || "Failed to cancel inquiry");
      }

      await fetchInquiries();
      setConfirmingInquiryId(null);
    } catch (error) {
      alert(error instanceof Error ? error.message : "Failed to cancel inquiry");
    } finally {
      setCancellingInquiryId(null);
    }
  };

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoadingData(true);
    setCurrentPage(1);
    setAppliedKeyword(searchKeyword.trim());
  };

  const handleClearSearch = () => {
    const shouldRefreshAllOrders = appliedKeyword.trim() !== "" || currentPage !== 1;
    setSearchKeyword("");
    if (!shouldRefreshAllOrders) {
      return;
    }
    setLoadingData(true);
    setAppliedKeyword("");
    setCurrentPage(1);
  };

  const pageNumbers = useMemo(() => {
    if (totalPages <= 0) {
      return [];
    }

    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, startPage + 4);
    const adjustedStartPage = Math.max(1, endPage - 4);
    return Array.from(
      { length: endPage - adjustedStartPage + 1 },
      (_unusedValue, index) => adjustedStartPage + index
    );
  }, [currentPage, totalPages]);

  if (!user) return null;

  return (
    <>
      {confirmingInquiryId !== null ? (
        <div className="fixed inset-0 z-[110] flex items-center justify-center px-4">
          <div
            className="absolute inset-0 bg-black/45"
            onClick={() => {
              if (cancellingInquiryId === null) {
                setConfirmingInquiryId(null);
              }
            }}
          />
          <div className="relative w-full max-w-md bg-white p-8 shadow-xl">
            <button
              type="button"
              onClick={() => {
                if (cancellingInquiryId === null) {
                  setConfirmingInquiryId(null);
                }
              }}
              className="absolute right-4 top-4 text-muted transition-colors hover:text-foreground disabled:opacity-50"
              disabled={cancellingInquiryId !== null}
            >
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            </button>
            <h3 className="mb-3 text-lg font-medium tracking-wider uppercase text-foreground">
              Cancel Inquiry
            </h3>
            <p className="text-sm leading-6 text-muted">
              Are you sure you want to cancel this inquiry? This action cannot be undone.
            </p>
            <div className="mt-6 flex gap-3">
              <button
                type="button"
                onClick={() => setConfirmingInquiryId(null)}
                disabled={cancellingInquiryId !== null}
                className="flex-1 border border-border px-4 py-3 text-[12px] font-medium uppercase tracking-[2px] text-foreground transition-colors hover:border-foreground disabled:opacity-50"
              >
                Keep Inquiry
              </button>
              <button
                type="button"
                onClick={() => void handleCancelInquiry(confirmingInquiryId)}
                disabled={cancellingInquiryId !== null}
                className="flex-1 bg-[#1a1a1a] px-4 py-3 text-[12px] font-medium uppercase tracking-[2px] text-white transition-colors hover:bg-[#333] disabled:opacity-50"
              >
                {cancellingInquiryId === confirmingInquiryId ? "Cancelling..." : "Confirm Cancel"}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      <h2 className="text-xl font-medium tracking-wider uppercase mb-8">
        Inquiry History
      </h2>
      <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <p className="text-sm text-muted">
          {totalCount} inquiries
        </p>
        <form
          onSubmit={handleSearch}
          className="flex w-full max-w-md items-center gap-2"
        >
          <div className="relative w-full">
            <input
              type="text"
              value={searchKeyword}
              onChange={(event) => setSearchKeyword(event.target.value)}
              placeholder="Search by inquiry number"
              className="h-10 w-full border border-border bg-white px-4 pr-10 text-sm text-foreground outline-none transition-colors placeholder:text-muted focus:border-foreground"
            />
            {searchKeyword ? (
              <button
                type="button"
                onClick={handleClearSearch}
                className="absolute right-3 top-1/2 inline-flex h-6 w-6 -translate-y-1/2 items-center justify-center text-muted transition-colors hover:text-foreground"
                aria-label="Clear search"
                title="Clear search"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M18 6 6 18M6 6l12 12" />
                </svg>
              </button>
            ) : null}
          </div>
          <button
            type="submit"
            className="inline-flex h-10 shrink-0 items-center justify-center border border-foreground px-4 text-[12px] font-medium uppercase tracking-[2px] text-foreground transition-colors hover:bg-foreground hover:text-white"
          >
            Search
          </button>
        </form>
      </div>
      {loadingData ? (
        <div className="text-center py-10 text-muted">Loading...</div>
      ) : inquiries.length === 0 ? (
        <div className="text-center py-10">
          <p className="text-muted mb-6">No inquiries yet</p>
          <Link
            href="/products"
            className="inline-block px-8 py-3 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300"
          >
            Start Shopping
          </Link>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4 text-sm font-medium text-muted">Inquiry No</th>
                <th className="text-left py-3 px-4 text-sm font-medium text-muted">Status</th>
                <th className="text-left py-3 px-4 text-sm font-medium text-muted">Amount</th>
                <th className="text-left py-3 px-4 text-sm font-medium text-muted">Date</th>
                <th className="text-left py-3 px-4 text-sm font-medium text-muted">Delivery Date</th>
                <th className="text-left py-3 px-4 text-sm font-medium text-muted">Action</th>
              </tr>
            </thead>
            <tbody>
              {inquiries.map((inquiry) => (
                <tr key={inquiry.id} className="border-b border-border">
                  <td className="py-4 px-4 text-sm">{inquiry.inquiryNo}</td>
                  <td className="py-4 px-4">
                    <span className={`text-sm ${STATUS_MAP[inquiry.status]?.color || ""}`}>
                      {STATUS_MAP[inquiry.status]?.label || "Unknown"}
                    </span>
                  </td>
                  <td className="py-4 px-4 text-sm">${inquiry.totalAmount}</td>
                  <td className="py-4 px-4 text-sm text-muted">
                    {formatDateValue(inquiry.createTime)}
                  </td>
                  <td className="py-4 px-4 text-sm text-muted">
                    {formatDateValue(inquiry.deliveryDate)}
                  </td>
                  <td className="py-4 px-4">
                    <div className="flex items-center gap-4">
                      <Link
                        href={`/account/orders/${inquiry.id}`}
                        className="text-sm text-foreground hover:underline"
                      >
                        View Details
                      </Link>
                      {inquiry.status === 0 ? (
                        <button
                          type="button"
                          onClick={() => setConfirmingInquiryId(inquiry.id)}
                          className="text-sm text-red-600 hover:underline"
                        >
                          Cancel
                        </button>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {totalPages > 1 ? (
            <div className="mt-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <p className="text-sm text-muted">
                Page {currentPage} of {totalPages}
              </p>
              <div className="flex flex-wrap items-center gap-2">
                <button
                  type="button"
                  onClick={() => {
                    setLoadingData(true);
                    setCurrentPage((previousPage) => Math.max(1, previousPage - 1));
                  }}
                  disabled={currentPage <= 1}
                  className="inline-flex h-10 items-center justify-center border border-border px-4 text-sm text-foreground transition-colors hover:border-foreground disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Previous
                </button>
                {pageNumbers.map((pageNumber) => (
                  <button
                    key={pageNumber}
                    type="button"
                    onClick={() => {
                      setLoadingData(true);
                      setCurrentPage(pageNumber);
                    }}
                    className={`inline-flex h-10 min-w-10 items-center justify-center border px-4 text-sm transition-colors ${
                      pageNumber === currentPage
                        ? "border-foreground bg-foreground text-white"
                        : "border-border text-foreground hover:border-foreground"
                    }`}
                  >
                    {pageNumber}
                  </button>
                ))}
                <button
                  type="button"
                  onClick={() => {
                    setLoadingData(true);
                    setCurrentPage((previousPage) => Math.min(totalPages, previousPage + 1));
                  }}
                  disabled={currentPage >= totalPages}
                  className="inline-flex h-10 items-center justify-center border border-border px-4 text-sm text-foreground transition-colors hover:border-foreground disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Next
                </button>
              </div>
            </div>
          ) : null}
        </div>
      )}
    </>
  );
}
