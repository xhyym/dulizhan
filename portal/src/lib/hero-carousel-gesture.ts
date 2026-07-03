export const SWIPE_THRESHOLD = 60;
export const SWIPE_DIRECTION_THRESHOLD = 10;

export type SwipeAction = "previous" | "next" | null;

interface SwipeGestureInput {
  deltaX: number;
  deltaY: number;
}

export function shouldTreatAsHorizontalDrag({
  deltaX,
  deltaY,
}: SwipeGestureInput): boolean {
  const absDeltaX = Math.abs(deltaX);
  const absDeltaY = Math.abs(deltaY);

  if (absDeltaX <= SWIPE_DIRECTION_THRESHOLD) {
    return false;
  }

  return absDeltaX > absDeltaY;
}

export function resolveSwipeAction({
  deltaX,
  deltaY,
}: SwipeGestureInput): SwipeAction {
  if (!shouldTreatAsHorizontalDrag({ deltaX, deltaY })) {
    return null;
  }

  if (Math.abs(deltaX) < SWIPE_THRESHOLD) {
    return null;
  }

  return deltaX > 0 ? "previous" : "next";
}
