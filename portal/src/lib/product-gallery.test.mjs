import assert from "node:assert/strict";
import {
  buildGalleryImages,
  createMagnifierBackgroundPosition,
} from "./product-gallery.ts";

function testBuildGalleryImages() {
  const images = buildGalleryImages(
    "https://example.com/main.jpg",
    [
      "https://example.com/side-1.jpg",
      "https://example.com/main.jpg",
      "   ",
      "https://example.com/side-2.jpg",
    ],
    "https://example.com/detail.jpg"
  );

  assert.deepEqual(images, [
    "https://example.com/main.jpg",
    "https://example.com/side-1.jpg",
    "https://example.com/side-2.jpg",
    "https://example.com/detail.jpg",
  ]);
}

function testCreateMagnifierBackgroundPosition() {
  const backgroundPosition = createMagnifierBackgroundPosition({
    pointerX: 150,
    pointerY: 200,
    rectLeft: 50,
    rectTop: 100,
    rectWidth: 400,
    rectHeight: 500,
  });

  assert.equal(backgroundPosition, "25.00% 20.00%");
}

function run() {
  testBuildGalleryImages();
  testCreateMagnifierBackgroundPosition();
  console.log("product-gallery tests passed");
}

run();
