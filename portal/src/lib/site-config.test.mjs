import assert from "node:assert/strict";
import {
  parseBannerImages,
  resolvePageBannerImage,
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

function run() {
  testParseBannerImages();
  testResolvePageBannerImage();
  console.log("site-config tests passed");
}

run();
