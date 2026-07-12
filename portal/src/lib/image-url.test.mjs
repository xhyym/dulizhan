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
    "https://osensino.com/cdn-cgi/image/format=auto,metadata=none,width=720,quality=82/https%3A%2F%2Fpub-example.r2.dev%2Fproducts%2Fmain%20image.jpg"
  );
}

function testBuildPortalImageUrlWithQueryString() {
  assert.equal(
    buildPortalImageUrl(
      "https://images.unsplash.com/photo-1541123603104-512919d6a96c?w=800&q=80",
      PORTAL_IMAGE_PRESETS.productCard
    ),
    "https://osensino.com/cdn-cgi/image/format=auto,metadata=none,width=720,quality=82/https%3A%2F%2Fimages.unsplash.com%2Fphoto-1541123603104-512919d6a96c%3Fw%3D800%26q%3D80"
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
    "https://osensino.com/cdn-cgi/image/format=auto,metadata=none,width=360,quality=80/https%3A%2F%2Fpub-example.r2.dev%2Fproducts%2Fmain.jpg 360w, https://osensino.com/cdn-cgi/image/format=auto,metadata=none,width=720,quality=80/https%3A%2F%2Fpub-example.r2.dev%2Fproducts%2Fmain.jpg 720w"
  );
}

function run() {
  testBuildPortalImageUrl();
  testBuildPortalImageUrlWithQueryString();
  testBuildPortalImageUrlFallback();
  testBuildPortalImageSrcSet();
  console.log("image-url tests passed");
}

run();
