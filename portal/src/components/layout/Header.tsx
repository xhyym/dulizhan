"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState, useCallback } from "react";
import { useAuth } from "@/lib/auth";
import { useCart } from "@/lib/cart";
import SearchModal from "@/components/ui/SearchModal";
import LanguageSwitcher from "@/components/ui/LanguageSwitcher";
import { buildPortalImageUrl, PORTAL_IMAGE_PRESETS } from "@/lib/image-url";

interface HeaderProps {
  siteName: string;
  siteLogo?: string;
}

const NAV_LINKS = [
  { href: "/", label: "Home" },
  { href: "/products", label: "Shop" },
  { href: "/about", label: "About" },
  { href: "/contact", label: "Contact" },
];

export default function Header({ siteName, siteLogo = "" }: HeaderProps) {
  const [scrolled, setScrolled] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const { user, setShowLogin, logout } = useAuth();
  const { itemCount } = useCart();

  const linkColor = scrolled ? "text-foreground" : "text-white";
  const iconButtonClass = `inline-flex h-10 w-10 items-center justify-center transition-colors ${linkColor}`;

  const closeMenu = useCallback(() => setMenuOpen(false), []);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 50);
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // 抽屉打开时锁定 body 滚动
  useEffect(() => {
    if (menuOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => { document.body.style.overflow = ""; };
  }, [menuOpen]);

  return (
    <>
      <header
        className={`fixed top-0 left-0 right-0 z-50 px-4 lg:px-15 py-4 lg:py-5 flex items-center justify-between transition-all duration-300 ${
          scrolled ? "header-scrolled" : "bg-transparent"
        }`}
      >
        {/* 左侧: Logo */}
        <Link
          href="/"
          className={`flex items-center gap-2 lg:gap-3 min-w-0 shrink-0 transition-colors ${linkColor}`}
        >
          {siteLogo ? (
            <Image
              src={buildPortalImageUrl(siteLogo, PORTAL_IMAGE_PRESETS.logo)}
              alt={siteName}
              width={120}
              height={24}
              unoptimized
              priority
              className="hidden lg:block h-6 w-auto max-w-[120px] object-contain shrink-0"
            />
          ) : null}
          <span className="text-sm lg:text-xl font-semibold tracking-widest truncate">
            {siteName}
          </span>
        </Link>

        {/* 中间: 桌面端导航 */}
        <nav className="hidden lg:flex items-center gap-10">
          {NAV_LINKS.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={`text-sm font-normal tracking-wider uppercase transition-colors hover:opacity-70 ${linkColor}`}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        {/* 右侧: 操作图标 */}
        <div className="flex items-center gap-2 lg:gap-6 shrink-0">
          {/* 桌面端显示语言切换 */}
          <div className="hidden lg:block">
            <LanguageSwitcher scrolled={scrolled} />
          </div>

          <button onClick={() => setShowSearch(true)} className={iconButtonClass} aria-label="Search">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" />
            </svg>
          </button>

          <Link href="/cart" className={`relative ${iconButtonClass}`} aria-label="Cart">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <path d="M16 10a4 4 0 0 1-8 0" />
            </svg>
            {itemCount > 0 && (
              <span className="absolute -top-1.5 -right-2 bg-foreground text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center">
                {itemCount}
              </span>
            )}
          </Link>

          {/* 桌面端用户图标 */}
          <div className="hidden lg:flex">
            {user ? (
              <div className="relative flex items-center group">
                <button className={iconButtonClass} aria-label="Account">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </button>
                <div className="absolute right-0 top-full mt-2 w-48 bg-white shadow-lg rounded opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
                  <div className="py-2">
                    <p className="px-4 py-2 text-sm text-muted border-b border-border">{user.email}</p>
                    <Link href="/account" className="block px-4 py-2 text-sm hover:bg-surface transition-colors">My Account</Link>
                    <Link href="/account/orders" className="block px-4 py-2 text-sm hover:bg-surface transition-colors">My Inquiries</Link>
                    <button onClick={logout} className="block w-full text-left px-4 py-2 text-sm hover:bg-surface transition-colors">Logout</button>
                  </div>
                </div>
              </div>
            ) : (
              <button onClick={() => setShowLogin(true)} className={iconButtonClass} aria-label="Login">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </button>
            )}
          </div>

          {/* 移动端汉堡菜单按钮 */}
          <button
            onClick={() => setMenuOpen(true)}
            className={`lg:hidden ${iconButtonClass}`}
            aria-label="Menu"
          >
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="4" y1="6" x2="20" y2="6" />
              <line x1="4" y1="12" x2="20" y2="12" />
              <line x1="4" y1="18" x2="20" y2="18" />
            </svg>
          </button>
        </div>
      </header>

      <SearchModal open={showSearch} onClose={() => setShowSearch(false)} />

      {/* 移动端侧滑抽屉 */}
      {/* 遮罩 */}
      <div
        className={`fixed inset-0 z-[60] bg-black/40 transition-opacity duration-300 lg:hidden ${
          menuOpen ? "opacity-100" : "opacity-0 pointer-events-none"
        }`}
        onClick={closeMenu}
      />

      {/* 抽屉面板 */}
      <div
        className={`fixed top-0 right-0 z-[61] h-full w-[75vw] max-w-[320px] bg-white shadow-2xl transition-transform duration-300 ease-out lg:hidden ${
          menuOpen ? "translate-x-0" : "translate-x-full"
        }`}
      >
        <div className="flex flex-col h-full">
          {/* 关闭按钮 */}
          <div className="flex items-center justify-end p-4">
            <button
              onClick={closeMenu}
              className="inline-flex h-10 w-10 items-center justify-center text-foreground"
              aria-label="Close menu"
            >
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          {/* 导航链接 */}
          <nav className="flex flex-col px-8 pt-4">
            {NAV_LINKS.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                onClick={closeMenu}
                className="py-4 text-lg font-normal tracking-wider uppercase border-b border-border/40 text-foreground hover:opacity-60 transition-opacity"
              >
                {link.label}
              </Link>
            ))}
          </nav>

          {/* 语言 & 账户 */}
          <div className="mt-auto px-8 py-8 border-t border-border/40 space-y-4">
            <div className="flex items-center gap-3 text-sm text-muted">
              <span className="text-xs tracking-wider uppercase">Language</span>
              <LanguageSwitcher scrolled />
            </div>

            {user ? (
              <div className="space-y-2">
                <p className="text-xs text-muted">{user.email}</p>
                <Link
                  href="/account"
                  onClick={closeMenu}
                  className="block py-2 text-sm text-foreground hover:opacity-60 transition-opacity"
                >
                  My Account
                </Link>
                <Link
                  href="/account/orders"
                  onClick={closeMenu}
                  className="block py-2 text-sm text-foreground hover:opacity-60 transition-opacity"
                >
                  My Inquiries
                </Link>
                <button
                  onClick={() => { logout(); closeMenu(); }}
                  className="block py-2 text-sm text-red-500 hover:opacity-60 transition-opacity"
                >
                  Logout
                </button>
              </div>
            ) : (
              <button
                onClick={() => { setShowLogin(true); closeMenu(); }}
                className="inline-flex items-center gap-2 text-sm text-foreground hover:opacity-60 transition-opacity"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                Sign In
              </button>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
