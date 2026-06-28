import type { Metadata } from "next";
import Link from "next/link";
import { portalAPI } from "@/lib/api";
import { buildContactPageMetadata } from "@/lib/seo";
import { parseConfigJson } from "@/lib/site-config";

export async function generateMetadata(): Promise<Metadata> {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);

  if (!siteConfig) {
    return {
      title: "Contact Us | OSEN FURNITURE",
      description: "Contact OSEN FURNITURE for furniture catalogs, quotations and customization support.",
    };
  }

  return buildContactPageMetadata(siteConfig);
}

export default async function ContactPage() {
  const siteConfig = await portalAPI.getSiteConfig().catch(() => null);

  if (!siteConfig) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-500 text-lg">站点配置加载失败，请检查后端服务是否正常</p>
      </div>
    );
  }

  const contactEmail = siteConfig.contact_email;
  if (!contactEmail) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-500 text-lg">站点配置缺失: contact_email</p>
      </div>
    );
  }

  const contactWhatsapp = siteConfig.contact_whatsapp;
  if (!contactWhatsapp) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-500 text-lg">站点配置缺失: contact_whatsapp</p>
      </div>
    );
  }

  const socialLinks = parseConfigJson<Record<string, string>>(siteConfig.social_links, {});

  const socialLabels: Record<string, string> = {
    facebook: "Facebook",
    instagram: "Instagram",
    pinterest: "Pinterest",
    twitter: "Twitter",
    youtube: "YouTube",
    tiktok: "TikTok",
  };

  return (
    <>
      {/* Banner */}
      <section className="relative h-[40vh] min-h-[300px] flex items-center justify-center bg-[#1a1a1a]">
        <div className="relative z-10 text-center text-white">
          <p className="text-sm font-light tracking-wider mb-2">
            <Link href="/" className="hover:opacity-80">Home</Link> &gt; Contact
          </p>
          <h1 className="text-3xl md:text-4xl font-light tracking-[6px] uppercase">
            Contact Us
          </h1>
        </div>
      </section>

      {/* Contact Info */}
      <section className="py-30 px-6 md:px-15">
        <div className="max-w-[800px] mx-auto text-center">
          <h2 className="text-2xl md:text-3xl font-light tracking-[6px] uppercase mb-6">
            Get in Touch
          </h2>
          <p className="text-sm font-light leading-[1.8] text-muted mb-15">
            We would love to hear from you. Whether you have a question about our products,
            pricing, or anything else, our team is ready to answer all your questions.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-10 mb-15">
            {/* Email */}
            <div className="bg-surface p-10">
              <div className="w-14 h-14 mx-auto mb-6 flex items-center justify-center">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <rect x="2" y="4" width="20" height="16" rx="2" />
                  <path d="M22 4L12 13 2 4" />
                </svg>
              </div>
              <h3 className="text-lg font-medium tracking-wider uppercase mb-3">Email</h3>
              <a
                href={`mailto:${contactEmail}`}
                className="text-sm text-muted hover:text-foreground transition-colors"
              >
                {contactEmail}
              </a>
            </div>

            {/* WhatsApp */}
            <div className="bg-surface p-10">
              <div className="w-14 h-14 mx-auto mb-6 flex items-center justify-center">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium tracking-wider uppercase mb-3">WhatsApp</h3>
              <a
                href={`https://wa.me/${contactWhatsapp.replace(/[^0-9]/g, "")}`}
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-muted hover:text-foreground transition-colors"
              >
                {contactWhatsapp}
              </a>
            </div>
          </div>

          {/* Social Links */}
          {Object.keys(socialLinks).length > 0 && (
            <div>
              <h3 className="text-lg font-medium tracking-wider uppercase mb-6">
                Follow Us
              </h3>
              <div className="flex justify-center gap-6">
                {Object.entries(socialLinks).map(([key, url]) => (
                  <a
                    key={key}
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm text-muted hover:text-foreground transition-colors tracking-wider uppercase"
                  >
                    {socialLabels[key] || key}
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>
      </section>
    </>
  );
}
