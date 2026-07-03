import assert from "node:assert/strict";
import {
  SWIPE_DIRECTION_THRESHOLD,
  SWIPE_THRESHOLD,
  resolveSwipeAction,
  shouldTreatAsHorizontalDrag,
} from "./hero-carousel-gesture.ts";

function testShouldTreatAsHorizontalDrag() {
  assert.equal(
    shouldTreatAsHorizontalDrag({
      deltaX: SWIPE_DIRECTION_THRESHOLD + 4,
      deltaY: 3,
    }),
    true
  );

  assert.equal(
    shouldTreatAsHorizontalDrag({
      deltaX: 6,
      deltaY: 20,
    }),
    false
  );

  assert.equal(
    shouldTreatAsHorizontalDrag({
      deltaX: SWIPE_DIRECTION_THRESHOLD - 1,
      deltaY: 0,
    }),
    false
  );
}

function testResolveSwipeAction() {
  assert.equal(
    resolveSwipeAction({
      deltaX: SWIPE_THRESHOLD + 8,
      deltaY: 12,
    }),
    "previous"
  );

  assert.equal(
    resolveSwipeAction({
      deltaX: -(SWIPE_THRESHOLD + 8),
      deltaY: 10,
    }),
    "next"
  );

  assert.equal(
    resolveSwipeAction({
      deltaX: SWIPE_THRESHOLD - 5,
      deltaY: 6,
    }),
    null
  );

  assert.equal(
    resolveSwipeAction({
      deltaX: 20,
      deltaY: 40,
    }),
    null
  );
}

function run() {
  testShouldTreatAsHorizontalDrag();
  testResolveSwipeAction();
  console.log("hero-carousel-gesture tests passed");
}

run();
