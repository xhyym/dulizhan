import type { Metadata } from "next";
import { Geist } from "next/font/google";
import "./globals.css";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import { AuthProvider } from "@/lib/auth";
import { CartProvider } from "@/lib/cart";
import LoginModal from "@/components/auth/LoginModal";
import TranslateProvider from "@/components/TranslateProvider";
import { portalAPI } from "@/lib/api";
import { buildRootMetadata } from "@/lib/seo";
import { getSiteDisplayName } from "@/lib/site-config";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

export async function generateMetadata(): Promise<Metadata> {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);

  if (!siteConfig) {
    return {
      title: "OSEN FURNITURE",
      description:
        "OSEN FURNITURE provides modern sofas, tables, chairs and custom furniture solutions for wholesale, retail and project orders worldwide.",
    };
  }

  return buildRootMetadata(siteConfig);
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);
  const siteName = siteConfig ? getSiteDisplayName(siteConfig) : "OSEN FURNITURE";

  return (
    <html lang="en" className={`${geistSans.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col">
        <AuthProvider>
          <CartProvider>
            <Header siteName={siteName} />
            <main className="flex-1">{children}</main>
            <Footer />
            <LoginModal />
            <TranslateProvider />
          </CartProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
