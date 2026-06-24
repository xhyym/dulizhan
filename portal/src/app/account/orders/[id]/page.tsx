"use client";

import Link from "next/link";
import { useAuth, useAuthFetch } from "@/lib/auth";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";

interface InquiryItem {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  skuId: number | null;
  skuSpec: string | null;
  price: number;
  quantity: number;
}

interface Inquiry {
  id: number;
  inquiryNo: string;
  userId: number;
  userName: string;
  userEmail: string;
  userWhatsapp: string;
  totalAmount: number;
  remark: string | null;
  status: number;
  adminRemark: string | null;
  createTime: string;
  items: InquiryItem[];
}

const STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: "Pending", color: "text-yellow-600" },
  1: { label: "Contacted", color: "text-blue-600" },
  2: { label: "Completed", color: "text-green-600" },
  3: { label: "Cancelled", color: "text-red-600" },
};

export default function OrderDetailPage() {
  const { user } = useAuth();
  const authFetch = useAuthFetch();
  const params = useParams();
  const [inquiry, setInquiry] = useState<Inquiry | null>(null);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    if (!user || !params.id) return;
    const fetchInquiry = async () => {
      setLoadingData(true);
      try {
        const res = await authFetch(`/api/portal/inquiries/${params.id}`);
        if (res.ok) {
          const data = await res.json();
          setInquiry(data.data);
        }
      } catch {
        // ignore
      } finally {
        setLoadingData(false);
      }
    };
    fetchInquiry();
  }, [user, params.id]);

  if (!user) return null;

  if (loadingData) {
    return (
      <div className="text-center py-10 text-muted">Loading...</div>
    );
  }

  if (!inquiry) {
    return (
      <div className="text-center py-10">
        <p className="text-muted mb-6">Inquiry not found</p>
        <Link
          href="/account/orders"
          className="px-8 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300"
        >
          Back to Inquiries
        </Link>
      </div>
    );
  }

  return (
    <>
      <div className="mb-8">
        <div className="flex flex-wrap items-center gap-6 mb-6">
          <div>
            <p className="text-sm text-muted">Inquiry No</p>
            <p className="font-medium">{inquiry.inquiryNo}</p>
          </div>
          <div>
            <p className="text-sm text-muted">Status</p>
            <p className={`font-medium ${STATUS_MAP[inquiry.status]?.color || ""}`}>
              {STATUS_MAP[inquiry.status]?.label || "Unknown"}
            </p>
          </div>
          <div>
            <p className="text-sm text-muted">Date</p>
            <p className="font-medium">
              {new Date(inquiry.createTime).toLocaleDateString()}
            </p>
          </div>
        </div>
      </div>

      {/* Items */}
      <div className="mb-8">
        <h3 className="text-lg font-medium tracking-wider uppercase mb-4">Items</h3>
        <div className="space-y-4">
          {inquiry.items?.map((item) => (
            <div key={item.id} className="flex gap-4 pb-4 border-b border-border">
              <div className="w-20 h-20 overflow-hidden bg-surface flex-shrink-0">
                <img
                  src={item.productImage}
                  alt={item.productName}
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="flex-1">
                <h4 className="text-sm font-medium mb-1">{item.productName}</h4>
                {item.skuSpec && (
                  <p className="text-sm text-muted mb-1">{item.skuSpec}</p>
                )}
                <div className="flex justify-between">
                  <span className="text-sm text-muted">Qty: {item.quantity}</span>
                  <span className="text-sm font-medium">${item.price * item.quantity}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Summary */}
      <div className="bg-surface p-6">
        <div className="flex justify-between mb-4">
          <span className="text-muted">Total Amount</span>
          <span className="text-lg font-medium">${inquiry.totalAmount}</span>
        </div>
        {inquiry.remark && (
          <div className="mb-4">
            <p className="text-sm text-muted mb-1">Your Note</p>
            <p className="text-sm">{inquiry.remark}</p>
          </div>
        )}
        {inquiry.adminRemark && (
          <div>
            <p className="text-sm text-muted mb-1">Admin Response</p>
            <p className="text-sm">{inquiry.adminRemark}</p>
          </div>
        )}
      </div>

      <div className="mt-6">
        <Link
          href="/account/orders"
          className="text-sm text-muted hover:text-foreground transition-colors"
        >
          &larr; Back to Inquiries
        </Link>
      </div>
    </>
  );
}
