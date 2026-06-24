"use client";

import Link from "next/link";
import { useAuth, useAuthFetch } from "@/lib/auth";
import { useEffect, useState } from "react";

interface Inquiry {
  id: number;
  inquiryNo: string;
  totalAmount: number;
  status: number;
  createTime: string;
}

const STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: "Pending", color: "text-yellow-600" },
  1: { label: "Contacted", color: "text-blue-600" },
  2: { label: "Completed", color: "text-green-600" },
  3: { label: "Cancelled", color: "text-red-600" },
};

export default function OrdersPage() {
  const { user } = useAuth();
  const authFetch = useAuthFetch();
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    if (!user) return;
    const fetchInquiries = async () => {
      setLoadingData(true);
      try {
        const res = await authFetch("/api/portal/inquiries");
        if (res.ok) {
          const data = await res.json();
          setInquiries(data.data || []);
        }
      } catch {
        // ignore
      } finally {
        setLoadingData(false);
      }
    };
    fetchInquiries();
  }, [user]);

  if (!user) return null;

  return (
    <>
      <h2 className="text-xl font-medium tracking-wider uppercase mb-8">
        Inquiry History
      </h2>
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
                    {new Date(inquiry.createTime).toLocaleDateString()}
                  </td>
                  <td className="py-4 px-4">
                    <Link
                      href={`/account/orders/${inquiry.id}`}
                      className="text-sm text-foreground hover:underline"
                    >
                      View Details
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
