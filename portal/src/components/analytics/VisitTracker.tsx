"use client";

import { useEffect, useMemo, useRef } from "react";
import { usePathname, useSearchParams } from "next/navigation";
import { portalAPI } from "@/lib/api";

export default function VisitTracker() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const lastTrackedUrlRef = useRef<string>("");

  const currentPageUrl = useMemo(() => {
    const queryString = searchParams.toString();
    return queryString ? `${pathname}?${queryString}` : pathname;
  }, [pathname, searchParams]);

  useEffect(() => {
    if (!currentPageUrl || lastTrackedUrlRef.current === currentPageUrl) {
      return;
    }

    lastTrackedUrlRef.current = currentPageUrl;

    void portalAPI.recordVisit({
      pageUrl: currentPageUrl,
    }).catch(() => {
      // 门户埋点失败不影响用户主流程
    });
  }, [currentPageUrl]);

  return null;
}
