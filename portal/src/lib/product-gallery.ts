/**
 * 构建商品图库图片列表，按主图、副图、详情图顺序合并，并去除空值与重复项。
 */
export function buildGalleryImages(
  mainImage: string | null | undefined,
  images: string[] | null | undefined,
  detailImage?: string | null
): string[] {
  const candidateImages = [mainImage, ...(images ?? []), detailImage];
  const uniqueImages = new Set<string>();

  for (const image of candidateImages) {
    const normalizedImage = image?.trim();
    if (!normalizedImage || uniqueImages.has(normalizedImage)) {
      continue;
    }
    uniqueImages.add(normalizedImage);
  }

  return Array.from(uniqueImages);
}

interface MagnifierBackgroundPositionParams {
  pointerX: number;
  pointerY: number;
  rectLeft: number;
  rectTop: number;
  rectWidth: number;
  rectHeight: number;
}

/**
 * 根据鼠标在主图中的相对位置，生成背景定位百分比，用于放大镜预览。
 */
export function createMagnifierBackgroundPosition(
  params: MagnifierBackgroundPositionParams
): string {
  const { pointerX, pointerY, rectLeft, rectTop, rectWidth, rectHeight } = params;

  if (rectWidth <= 0 || rectHeight <= 0) {
    return "50.00% 50.00%";
  }

  const relativeX = ((pointerX - rectLeft) / rectWidth) * 100;
  const relativeY = ((pointerY - rectTop) / rectHeight) * 100;

  const safeX = clamp(relativeX, 0, 100);
  const safeY = clamp(relativeY, 0, 100);

  return `${safeX.toFixed(2)}% ${safeY.toFixed(2)}%`;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}
