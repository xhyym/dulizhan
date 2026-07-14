import type { Metadata } from "next";
import { Geist } from "next/font/google";
import { Suspense } from "react";
import "./globals.css";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import { AuthProvider } from "@/lib/auth";
import { CartProvider } from "@/lib/cart";
import LoginModal from "@/components/auth/LoginModal";
import VisitTracker from "@/components/analytics/VisitTracker";
import AnalyticsScripts from "@/components/analytics/AnalyticsScripts";
import TranslateProvider from "@/components/TranslateProvider";
import { portalAPI } from "@/lib/api";
import { buildRootMetadata } from "@/lib/seo";
import { getSiteBaseUrl, getSiteDisplayName, normalizeSiteConfig } from "@/lib/site-config";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

export async function generateMetadata(): Promise<Metadata> {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);
  return buildRootMetadata(normalizeSiteConfig(siteConfig));
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const siteConfigResponse = await portalAPI.getSiteConfig().catch(() => null);
  const siteConfig = normalizeSiteConfig(siteConfigResponse);
  const siteName = getSiteDisplayName(siteConfig);
  const siteLogo = siteConfig?.site_logo?.trim() || "";
  const siteFavicon = siteConfig?.site_favicon?.trim() || "";
  const analyticsConfigJson = siteConfig?.analytics_config;
  const websiteStructuredData = {
    "@context": "https://schema.org",
    "@type": "WebSite",
    name: siteName,
    alternateName: "OSEN",
    url: `${getSiteBaseUrl()}/`,
  };
  // 防止后台配置文字中的尖括号意外影响 script 标签结构。
  const websiteStructuredDataJson = JSON.stringify(websiteStructuredData).replace(/</g, "\\u003c");

  let gtmId = "";
  let customBodyHtml = "";
  if (analyticsConfigJson) {
    try {
      const analytics = JSON.parse(analyticsConfigJson);
      gtmId = analytics.gtm_id?.trim() || "";
      customBodyHtml = analytics.custom_body?.trim() || "";
    } catch { /* ignore parse errors */ }
  }
  const footerContent = await Footer();

  return (
    <html lang="en" className={`${geistSans.variable} h-full antialiased`}>
      <head>
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: websiteStructuredDataJson }}
        />
        {siteFavicon ? (
          <>
            <link rel="icon" href={siteFavicon} />
            <link rel="shortcut icon" href={siteFavicon} />
            <link rel="apple-touch-icon" href={siteFavicon} />
          </>
        ) : null}
        <AnalyticsScripts configJson={analyticsConfigJson} />
      </head>
      <body className="min-h-full flex flex-col">
        {/* GTM noscript fallback */}
        {gtmId && (
          <noscript
            dangerouslySetInnerHTML={{
              __html: `<iframe src="https://www.googletagmanager.com/ns.html?id=${gtmId}" height="0" width="0" style="display:none;visibility:hidden"></iframe>`,
            }}
          />
        )}
        {customBodyHtml && (
          <div dangerouslySetInnerHTML={{ __html: customBodyHtml }} />
        )}
        <AuthProvider>
          <CartProvider>
            <Header siteName={siteName} siteLogo={siteLogo} />
            <Suspense fallback={null}>
              <VisitTracker />
            </Suspense>
            <main className="flex-1">{children}</main>
            {footerContent}
            <LoginModal />
            <TranslateProvider />
          </CartProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
