import assert from "node:assert/strict";
import {
  normalizeSiteConfig,
  parseAboutUsConfig,
  parseBannerImages,
  parseFooterInfo,
  parseSocialLinks,
  resolvePageBannerImage,
  buildAbsoluteUrl,
  getSiteBaseUrl,
} from "./site-config.ts";

function testParseBannerImages() {
  assert.deepEqual(
    parseBannerImages(
      JSON.stringify([
        { image: " https://example.com/a.jpg " },
        " https://example.com/b.jpg ",
      ])
    ),
    ["https://example.com/a.jpg", "https://example.com/b.jpg"]
  );
}

function testResolvePageBannerImage() {
  const siteConfig = {
    banner_images: JSON.stringify([
      { image: "https://example.com/home.jpg" },
      { image: "https://example.com/fallback.jpg" },
    ]),
    products_banner_image: "https://example.com/products.jpg",
  };

  assert.equal(
    resolvePageBannerImage(siteConfig, "products_banner_image"),
    "https://example.com/products.jpg"
  );

  assert.equal(
    resolvePageBannerImage(
      { banner_images: siteConfig.banner_images },
      "products_banner_image"
    ),
    "https://example.com/fallback.jpg"
  );

  assert.equal(
    resolvePageBannerImage({
      banner_images: JSON.stringify(["https://example.com/only.jpg"]),
    }),
    "https://example.com/only.jpg"
  );

  assert.equal(resolvePageBannerImage({}), "");
}

function testNormalizeSiteConfig() {
  const config = normalizeSiteConfig({
    site_title: "  ",
    hero_title: "Custom title",
  });

  assert.equal(config.site_title, "OSEN FURNITURE");
  assert.equal(config.hero_title, "Custom title");
  assert.equal(config.banner_images, "[]");
}

function testConfigFallbackParsers() {
  const aboutUs = parseAboutUsConfig("");
  assert.equal(aboutUs.storyTitle, "Our Story");
  assert.equal(aboutUs.storyImage, "");
  assert.equal(aboutUs.philosophy.length, 3);

  const footerInfo = parseFooterInfo("{");
  assert.equal(footerInfo.links.length, 0);
  assert.match(footerInfo.copyright, /OSEN FURNITURE/);
  assert.match(footerInfo.description, /Handcrafted furniture/);

  assert.equal(parseAboutUsConfig("null").storyTitle, "Our Story");
  assert.equal(parseFooterInfo("null").links.length, 0);
  assert.deepEqual(parseSocialLinks("[]"), {});
  assert.deepEqual(
    parseFooterInfo('{"links":[{"name":"Legacy link","url":"/about"}]}').links,
    [{ title: "Legacy link", url: "/about" }]
  );
}

function testProductionSiteBaseUrl() {
  assert.equal(getSiteBaseUrl(), "https://osensino.com");
  assert.equal(buildAbsoluteUrl("/sitemap.xml"), "https://osensino.com/sitemap.xml");
}

function run() {
  testParseBannerImages();
  testResolvePageBannerImage();
  testNormalizeSiteConfig();
  testConfigFallbackParsers();
  testProductionSiteBaseUrl();
  console.log("site-config tests passed");
}

run();
