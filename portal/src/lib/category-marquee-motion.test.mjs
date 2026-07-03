import assert from "node:assert/strict";
import {
  advanceMarqueeOffset,
  MARQUEE_SPEED_PX_PER_SECOND,
  normalizeMarqueeOffset,
} from "./category-marquee-motion.ts";

function testNormalizeMarqueeOffset() {
  assert.equal(normalizeMarqueeOffset(0, 600), 0);
  assert.equal(normalizeMarqueeOffset(-120, 600), -120);
  assert.equal(normalizeMarqueeOffset(-640, 600), -40);
  assert.equal(normalizeMarqueeOffset(-1240, 600), -40);
  assert.equal(normalizeMarqueeOffset(80, 600), -520);
}

function testConstants() {
  assert.equal(typeof MARQUEE_SPEED_PX_PER_SECOND, "number");
  assert.equal(MARQUEE_SPEED_PX_PER_SECOND > 0, true);
}

function testAdvanceMarqueeOffset() {
  assert.equal(
    advanceMarqueeOffset({
      currentOffset: -120,
      elapsedMs: 500,
      loopWidth: 600,
      speedPxPerSecond: 40,
    }),
    -140
  );

  assert.equal(
    advanceMarqueeOffset({
      currentOffset: -590,
      elapsedMs: 500,
      loopWidth: 600,
      speedPxPerSecond: 40,
    }),
    -10
  );
}

function run() {
  testNormalizeMarqueeOffset();
  testConstants();
  testAdvanceMarqueeOffset();
  console.log("category-marquee-motion tests passed");
}

run();
