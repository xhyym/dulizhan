import assert from "node:assert/strict";

process.env.NEXT_PUBLIC_SITE_URL = "https://osensino.com";

const {
  buildPortalImageUrl,
  buildPortalImageSrcSet,
  PORTAL_IMAGE_PRESETS,
} = await import("./image-url.ts");

function testBuildPortalImageUrl() {
  assert.equal(
    buildPortalImageUrl(
      "https://pub-example.r2.dev/products/main image.jpg",
      PORTAL_IMAGE_PRESETS.productCard
    ),
    "https://osensino.com/cdn-cgi/image/format=auto,metadata=none,width=720,quality=82/https://pub-example.r2.dev/products/main%20image.jpg"
  );
}

function testBuildPortalImageUrlFallback() {
  delete process.env.NEXT_PUBLIC_SITE_URL;

  assert.equal(
    buildPortalImageUrl("https://pub-example.r2.dev/products/main.jpg"),
    "https://pub-example.r2.dev/products/main.jpg"
  );

  process.env.NEXT_PUBLIC_SITE_URL = "https://osensino.com";
}

function testBuildPortalImageSrcSet() {
  assert.equal(
    buildPortalImageSrcSet("https://pub-example.r2.dev/products/main.jpg", [360, 720, 720], {
      quality: 80,
    }),
    "https://osensino.com/cdn-cgi/image/format=auto,metadata=none,width=360,quality=80/https://pub-example.r2.dev/products/main.jpg 360w, https://osensino.com/cdn-cgi/image/format=auto,metadata=none,width=720,quality=80/https://pub-example.r2.dev/products/main.jpg 720w"
  );
}

function run() {
  testBuildPortalImageUrl();
  testBuildPortalImageUrlFallback();
  testBuildPortalImageSrcSet();
  console.log("image-url tests passed");
}

run();
