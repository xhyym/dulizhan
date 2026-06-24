"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense } from "react";
import ClientBanner from "@/components/ui/ClientBanner";

function InquirySuccessContent() {
  const searchParams = useSearchParams();
  const inquiryId = searchParams.get("id");

  return (
    <>
      <ClientBanner title="Inquiry Submitted" />

      {/* Content */}
      <section className="py-20 px-6 md:px-15">
        <div className="max-w-[600px] mx-auto text-center">
          <div className="mb-8">
            <svg
              className="w-16 h-16 mx-auto text-green-600 mb-6"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
            >
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <h2 className="text-2xl font-light tracking-wider uppercase mb-4">
              Successfully Submitted
            </h2>
            {inquiryId && (
              <p className="text-muted mb-2">
                Inquiry No: <span className="font-medium text-foreground">{inquiryId}</span>
              </p>
            )}
            <p className="text-muted">
              We will contact you via WhatsApp within 24 hours.
            </p>
          </div>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              href="/account/orders"
              className="px-8 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300"
            >
              View My Inquiries
            </Link>
            <Link
              href="/products"
              className="px-8 py-4 text-[13px] font-medium tracking-[3px] uppercase border border-[#1a1a1a] text-[#1a1a1a] hover:bg-[#1a1a1a] hover:text-white transition-all duration-300"
            >
              Continue Shopping
            </Link>
          </div>
        </div>
      </section>
    </>
  );
}

export default function InquirySuccessPage() {
  return (
    <Suspense fallback={<div className="py-20 text-center text-muted">Loading...</div>}>
      <InquirySuccessContent />
    </Suspense>
  );
}
