import Image from "next/image";
import Link from "next/link";
import { portalAPI } from "@/lib/api";

export default async function Footer() {
  const siteConfig = await portalAPI
    .getSiteConfig()
    .catch(() => null);

  if (!siteConfig) {
    return (
      <footer className="bg-[#1a1a1a] text-white px-6 py-10">
        <p className="text-red-400 text-sm text-center">站点配置加载失败</p>
      </footer>
    );
  }

  const siteTitle = siteConfig.site_title;
  const siteLogo = siteConfig.site_logo?.trim() || "";
  if (!siteTitle) {
    return (
      <footer className="bg-[#1a1a1a] text-white px-6 py-10">
        <p className="text-red-400 text-sm text-center">站点配置缺失: site_title</p>
      </footer>
    );
  }

  const contactEmail = siteConfig.contact_email;
  if (!contactEmail) {
    return (
      <footer className="bg-[#1a1a1a] text-white px-6 py-10">
        <p className="text-red-400 text-sm text-center">站点配置缺失: contact_email</p>
      </footer>
    );
  }

  const contactWhatsapp = siteConfig.contact_whatsapp;
  if (!contactWhatsapp) {
    return (
      <footer className="bg-[#1a1a1a] text-white px-6 py-10">
        <p className="text-red-400 text-sm text-center">站点配置缺失: contact_whatsapp</p>
      </footer>
    );
  }

  let footerData: { copyright: string; links: { title: string; url: string }[] };
  try {
    footerData = JSON.parse(siteConfig.footer_info);
  } catch {
    return (
      <footer className="bg-[#1a1a1a] text-white px-6 py-10">
        <p className="text-red-400 text-sm text-center">站点配置错误: footer_info JSON 格式异常</p>
      </footer>
    );
  }

  let socialLinks: Record<string, string>;
  try {
    socialLinks = JSON.parse(siteConfig.social_links);
  } catch {
    socialLinks = {};
  }

  const activeSocialLinks = Object.entries(socialLinks).filter(([_key, url]) =>
    Boolean(url?.trim())
  );

  const socialLabels: Record<string, string> = {
    facebook: "Facebook",
    instagram: "Instagram",
    pinterest: "Pinterest",
    twitter: "Twitter",
    youtube: "YouTube",
    tiktok: "TikTok",
  };

  return (
    <footer className="bg-[#1a1a1a] text-white px-6 md:px-15 py-20">
      <div className="max-w-[1200px] mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-15 mb-15">
          <div>
            <div className="flex items-center gap-3 mb-4">
              {siteLogo ? (
                <Image
                  src={siteLogo}
                  alt={siteTitle}
                  width={120}
                  height={20}
                  unoptimized
                  className="h-5 w-auto max-w-[120px] object-contain shrink-0"
                />
              ) : null}
              <span className="text-lg font-semibold tracking-widest block">
                {siteTitle}
              </span>
            </div>
            <p className="text-sm font-light leading-relaxed text-white/60">
              Handcrafted furniture for modern living. Simple, functional, and
              beautiful.
            </p>
          </div>
          <div>
            <h4 className="text-[13px] font-medium tracking-wider uppercase mb-6">
              Quick Links
            </h4>
            <ul className="space-y-3">
              <li>
                <Link href="/" className="text-sm font-light text-white/60 hover:text-white transition-colors">
                  Home
                </Link>
              </li>
              <li>
                <Link href="/products" className="text-sm font-light text-white/60 hover:text-white transition-colors">
                  Shop
                </Link>
              </li>
              <li>
                <Link href="/about" className="text-sm font-light text-white/60 hover:text-white transition-colors">
                  About Us
                </Link>
              </li>
              <li>
                <Link href="/contact" className="text-sm font-light text-white/60 hover:text-white transition-colors">
                  Contact
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h4 className="text-[13px] font-medium tracking-wider uppercase mb-6">
              Customer Service
            </h4>
            <ul className="space-y-3">
              <li>
                <a href={`mailto:${contactEmail}`} className="text-sm font-light text-white/60 hover:text-white transition-colors">
                  {contactEmail}
                </a>
              </li>
              <li>
                <a href={`https://wa.me/${contactWhatsapp.replace(/[^0-9]/g, "")}`} target="_blank" rel="noopener noreferrer" className="text-sm font-light text-white/60 hover:text-white transition-colors">
                  WhatsApp: {contactWhatsapp}
                </a>
              </li>
              {footerData.links?.map((link, i) => (
                <li key={i}>
                  <Link href={link.url} className="text-sm font-light text-white/60 hover:text-white transition-colors">
                    {link.title}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
          {activeSocialLinks.length > 0 ? (
          <div>
            <h4 className="text-[13px] font-medium tracking-wider uppercase mb-6">
              Connect
            </h4>
            <ul className="space-y-3">
              {activeSocialLinks.map(([key, url]) => (
                <li key={key}>
                  <a href={url} target="_blank" rel="noopener noreferrer" className="text-sm font-light text-white/60 hover:text-white transition-colors">
                    {socialLabels[key] || key}
                  </a>
                </li>
              ))}
            </ul>
          </div>
          ) : null}
        </div>
        <div className="border-t border-white/10 pt-10 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-[13px] font-light text-white/40">
            {footerData.copyright}
          </p>
          {activeSocialLinks.length > 0 ? (
          <div className="flex gap-5">
            {activeSocialLinks.map(([key, url]) => (
              <a key={key} href={url} target="_blank" rel="noopener noreferrer" className="text-sm text-white/60 hover:text-white transition-colors">
                {socialLabels[key] || key}
              </a>
            ))}
          </div>
          ) : null}
        </div>
      </div>
    </footer>
  );
}
