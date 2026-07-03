"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth";
import { useCart } from "@/lib/cart";
import SearchModal from "@/components/ui/SearchModal";
import LanguageSwitcher from "@/components/ui/LanguageSwitcher";

interface HeaderProps {
  siteName: string;
  siteLogo?: string;
}

export default function Header({ siteName, siteLogo = "" }: HeaderProps) {
  const [scrolled, setScrolled] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const { user, setShowLogin, logout } = useAuth();
  const { itemCount } = useCart();
  const iconButtonClass = `inline-flex h-10 w-10 items-center justify-center transition-colors ${
    scrolled ? "text-foreground" : "text-white"
  }`;

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 px-6 md:px-15 py-5 flex items-center justify-between transition-all duration-300 ${
        scrolled ? "header-scrolled py-4" : "bg-transparent"
      }`}
    >
      <Link
        href="/"
        className={`flex items-center gap-3 transition-colors ${
          scrolled ? "text-foreground" : "text-white"
        }`}
      >
        {siteLogo ? (
          <Image
            src={siteLogo}
            alt={siteName}
            width={120}
            height={24}
            unoptimized
            priority
            className="h-6 w-auto max-w-[120px] object-contain shrink-0"
          />
        ) : null}
        <span className="text-xl font-semibold tracking-widest">
          {siteName}
        </span>
      </Link>

      <nav className="hidden md:flex items-center gap-10">
        <Link
          href="/"
          className={`text-sm font-normal tracking-wider uppercase transition-colors hover:opacity-70 ${
            scrolled ? "text-foreground" : "text-white"
          }`}
        >
          Home
        </Link>
        <Link
          href="/products"
          className={`text-sm font-normal tracking-wider uppercase transition-colors hover:opacity-70 ${
            scrolled ? "text-foreground" : "text-white"
          }`}
        >
          Shop
        </Link>
        <Link
          href="/about"
          className={`text-sm font-normal tracking-wider uppercase transition-colors hover:opacity-70 ${
            scrolled ? "text-foreground" : "text-white"
          }`}
        >
          About
        </Link>
        <Link
          href="/contact"
          className={`text-sm font-normal tracking-wider uppercase transition-colors hover:opacity-70 ${
            scrolled ? "text-foreground" : "text-white"
          }`}
        >
          Contact
        </Link>
      </nav>

      <div className="flex items-center gap-6">
        <LanguageSwitcher scrolled={scrolled} />
        <button
          onClick={() => setShowSearch(true)}
          className={iconButtonClass}
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
        </button>
        <Link
          href="/cart"
          className={`relative ${iconButtonClass}`}
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <path d="M16 10a4 4 0 0 1-8 0"></path>
          </svg>
          {itemCount > 0 && (
            <span className="absolute -top-1.5 -right-2 bg-foreground text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center">
              {itemCount}
            </span>
          )}
        </Link>
        {user ? (
          <div className="relative flex items-center group">
            <button
              className={iconButtonClass}
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </button>
            <div className="absolute right-0 top-full mt-2 w-48 bg-white shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
              <div className="py-2">
                <p className="px-4 py-2 text-sm text-muted border-b border-border">
                  {user.email}
                </p>
                <Link
                  href="/account"
                  className="block px-4 py-2 text-sm hover:bg-surface transition-colors"
                >
                  My Account
                </Link>
                <Link
                  href="/account/orders"
                  className="block px-4 py-2 text-sm hover:bg-surface transition-colors"
                >
                  My Inquiries
                </Link>
                <button
                  onClick={logout}
                  className="block w-full text-left px-4 py-2 text-sm hover:bg-surface transition-colors"
                >
                  Logout
                </button>
              </div>
            </div>
          </div>
        ) : (
          <button
            onClick={() => setShowLogin(true)}
            className={iconButtonClass}
          >
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </button>
        )}
      </div>
      <SearchModal open={showSearch} onClose={() => setShowSearch(false)} />
    </header>
  );
}
