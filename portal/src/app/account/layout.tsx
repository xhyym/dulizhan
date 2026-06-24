"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth";
import ClientBanner from "@/components/ui/ClientBanner";

const NAV_ITEMS = [
  { label: "Account Info", href: "/account" },
  { label: "My Inquiries", href: "/account/orders" },
];

// 根据路径生成面包屑
function getBreadcrumbs(pathname: string) {
  const crumbs: { label: string; href?: string }[] = [
    { label: "Home", href: "/" },
    { label: "My Account", href: "/account" },
  ];

  if (pathname.startsWith("/account/orders")) {
    crumbs.push({ label: "My Inquiries", href: "/account/orders" });
  }

  return crumbs;
}

export default function AccountLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, loading, logout, setShowLogin } = useAuth();
  const pathname = usePathname();

  if (loading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center text-muted">
        Loading...
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <p className="text-muted mb-6">Please login to view your account</p>
        <button
          onClick={() => setShowLogin(true)}
          className="px-8 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300"
        >
          Login
        </button>
      </div>
    );
  }

  const breadcrumbs = getBreadcrumbs(pathname);

  return (
    <>
      <ClientBanner title="My Account" breadcrumbs={breadcrumbs} />

      <section className="py-15 px-6 md:px-15">
        <div className="max-w-[1200px] mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-[250px_1fr] gap-10">
            {/* Sidebar */}
            <div className="bg-surface p-6">
              <div className="text-center mb-6">
                <div className="w-20 h-20 bg-border rounded-full mx-auto mb-4 flex items-center justify-center">
                  <svg
                    className="w-10 h-10 text-muted"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.5"
                  >
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </div>
                <h3 className="font-medium">{user.username}</h3>
                <p className="text-sm text-muted">{user.email}</p>
                {user.whatsapp && (
                  <p className="text-sm text-muted">{user.whatsapp}</p>
                )}
              </div>
              <nav className="space-y-2">
                {NAV_ITEMS.map((item) => {
                  const isActive =
                    item.href === "/account"
                      ? pathname === "/account"
                      : pathname.startsWith(item.href);
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={`block px-4 py-2 text-sm transition-colors ${
                        isActive
                          ? "bg-foreground text-white"
                          : "hover:bg-white"
                      }`}
                    >
                      {item.label}
                    </Link>
                  );
                })}
                <button
                  onClick={logout}
                  className="block w-full text-left px-4 py-2 text-sm hover:bg-white transition-colors"
                >
                  Logout
                </button>
              </nav>
            </div>

            {/* Main Content */}
            <div>{children}</div>
          </div>
        </div>
      </section>
    </>
  );
}
