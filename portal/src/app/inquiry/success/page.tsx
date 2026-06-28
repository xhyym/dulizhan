import type { Metadata } from "next";
import { Suspense } from "react";
import { portalAPI } from "@/lib/api";
import { buildInquirySuccessMetadata } from "@/lib/seo";
import InquirySuccessContent from "./InquirySuccessContent";

export async function generateMetadata(): Promise<Metadata> {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);

  if (!siteConfig) {
    return {
      title: "Inquiry Submitted | OSEN FURNITURE",
      description: "Your inquiry has been submitted successfully. OSEN FURNITURE will contact you soon.",
    };
  }

  return buildInquirySuccessMetadata(siteConfig);
}

export default function InquirySuccessPage() {
  return (
    <Suspense fallback={<div className="py-20 text-center text-muted">Loading...</div>}>
      <InquirySuccessContent />
    </Suspense>
  );
}
