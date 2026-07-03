export const MARQUEE_SPEED_PX_PER_SECOND = 36;

export function normalizeMarqueeOffset(
  offset: number,
  loopWidth: number
): number {
  if (loopWidth <= 0) {
    return 0;
  }

  let normalizedOffset = offset % loopWidth;

  if (normalizedOffset > 0) {
    normalizedOffset -= loopWidth;
  }

  return normalizedOffset;
}

interface AdvanceMarqueeOffsetOptions {
  currentOffset: number;
  elapsedMs: number;
  loopWidth: number;
  speedPxPerSecond: number;
}

export function advanceMarqueeOffset({
  currentOffset,
  elapsedMs,
  loopWidth,
  speedPxPerSecond,
}: AdvanceMarqueeOffsetOptions): number {
  const nextOffset =
    currentOffset - (speedPxPerSecond * elapsedMs) / 1000;

  return normalizeMarqueeOffset(nextOffset, loopWidth);
}
