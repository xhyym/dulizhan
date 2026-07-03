import assert from "node:assert/strict";
import {
  getNextCarouselIndex,
  resolveCarouselSwipeDirection,
} from "./carousel-gesture.ts";

function testResolveCarouselSwipeDirection() {
  assert.equal(
    resolveCarouselSwipeDirection({ deltaX: 40, deltaY: 0, threshold: 60 }),
    null
  );

  assert.equal(
    resolveCarouselSwipeDirection({ deltaX: 80, deltaY: 90, threshold: 60 }),
    null
  );

  assert.equal(
    resolveCarouselSwipeDirection({ deltaX: 90, deltaY: 20, threshold: 60 }),
    "previous"
  );

  assert.equal(
    resolveCarouselSwipeDirection({ deltaX: -90, deltaY: 20, threshold: 60 }),
    "next"
  );
}

function testGetNextCarouselIndex() {
  assert.equal(getNextCarouselIndex(0, 3, "previous"), 2);
  assert.equal(getNextCarouselIndex(2, 3, "next"), 0);
  assert.equal(getNextCarouselIndex(1, 3, "next"), 2);
}

function run() {
  testResolveCarouselSwipeDirection();
  testGetNextCarouselIndex();
  console.log("carousel-gesture tests passed");
}

run();
