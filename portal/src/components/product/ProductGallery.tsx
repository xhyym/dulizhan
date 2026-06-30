"use client";

import { useMemo, useRef, useState } from "react";
import {
  buildGalleryImages,
  createMagnifierBackgroundPosition,
} from "@/lib/product-gallery";

interface ProductGalleryProps {
  productName: string;
  mainImage: string;
  images?: string[];
  detailImage?: string | null;
}

interface PointerState {
  visible: boolean;
  pointerX: number;
  pointerY: number;
  backgroundPosition: string;
}

const PREVIEW_BUTTON_CLASS =
  "inline-flex h-10 w-10 items-center justify-center bg-white/90 text-[#1a1a1a] transition-colors hover:bg-white";

export default function ProductGallery({
  productName,
  mainImage,
  images = [],
  detailImage,
}: ProductGalleryProps) {
  const galleryImages = useMemo(
    () => buildGalleryImages(mainImage, images, detailImage),
    [detailImage, images, mainImage]
  );
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [pointerState, setPointerState] = useState<PointerState>({
    visible: false,
    pointerX: 0,
    pointerY: 0,
    backgroundPosition: "50.00% 50.00%",
  });
  const imageContainerRef = useRef<HTMLDivElement>(null);

  const selectedImage = galleryImages[selectedIndex] ?? mainImage;

  const handlePointerMove = (event: React.MouseEvent<HTMLDivElement>) => {
    const rect = imageContainerRef.current?.getBoundingClientRect();
    if (!rect) {
      return;
    }

    const backgroundPosition = createMagnifierBackgroundPosition({
      pointerX: event.clientX,
      pointerY: event.clientY,
      rectLeft: rect.left,
      rectTop: rect.top,
      rectWidth: rect.width,
      rectHeight: rect.height,
    });

    setPointerState({
      visible: true,
      pointerX: event.clientX - rect.left,
      pointerY: event.clientY - rect.top,
      backgroundPosition,
    });
  };

  const handlePointerLeave = () => {
    setPointerState((prevState) => ({
      ...prevState,
      visible: false,
    }));
  };

  const handleSelectImage = (index: number) => {
    setSelectedIndex(index);
  };

  const handleShowPrevious = () => {
    setSelectedIndex((prevIndex) =>
      prevIndex === 0 ? galleryImages.length - 1 : prevIndex - 1
    );
  };

  const handleShowNext = () => {
    setSelectedIndex((prevIndex) =>
      prevIndex === galleryImages.length - 1 ? 0 : prevIndex + 1
    );
  };

  const renderMagnifierMask = () => {
    if (!pointerState.visible) {
      return null;
    }

    return (
      <div
        className="pointer-events-none absolute hidden h-28 w-28 -translate-x-1/2 -translate-y-1/2 border border-white/80 bg-white/10 backdrop-blur-[1px] md:block"
        style={{
          left: `${pointerState.pointerX}px`,
          top: `${pointerState.pointerY}px`,
        }}
      />
    );
  };

  return (
    <>
      <div className="grid gap-4 md:grid-cols-[minmax(0,1fr)_120px]">
        <div className="space-y-4">
          <div className="relative overflow-hidden bg-surface">
            <div
              ref={imageContainerRef}
              className="group relative aspect-[3/4] cursor-zoom-in overflow-hidden"
              onMouseMove={handlePointerMove}
              onMouseLeave={handlePointerLeave}
              onClick={() => setPreviewOpen(true)}
            >
              <img
                src={selectedImage}
                alt={productName}
                className="h-full w-full object-cover"
              />

              <div className="pointer-events-none absolute inset-0 bg-black/0 transition-colors duration-300 group-hover:bg-black/6" />
              <div className="pointer-events-none absolute right-4 top-4 hidden items-center gap-2 bg-white/92 px-3 py-2 text-[11px] font-medium uppercase tracking-[2px] text-[#1a1a1a] md:flex">
                View
              </div>
              {renderMagnifierMask()}
            </div>
          </div>

          {galleryImages.length > 1 && (
            <div className="grid grid-cols-4 gap-2 md:hidden">
              {galleryImages.map((image, index) => (
                <button
                  key={`${image}-${index}`}
                  type="button"
                  onClick={() => handleSelectImage(index)}
                  className={`aspect-square overflow-hidden border transition-colors ${
                    index === selectedIndex
                      ? "border-foreground"
                      : "border-border hover:border-foreground"
                  }`}
                >
                  <img
                    src={image}
                    alt={`${productName} preview ${index + 1}`}
                    className="h-full w-full object-cover"
                  />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="hidden md:block">
          <div className="sticky top-28 space-y-3">
            {galleryImages.map((image, index) => (
              <button
                key={`${image}-${index}`}
                type="button"
                onClick={() => handleSelectImage(index)}
                className={`block aspect-square w-full overflow-hidden border transition-colors ${
                  index === selectedIndex
                    ? "border-foreground"
                    : "border-border hover:border-foreground"
                }`}
              >
                <img
                  src={image}
                  alt={`${productName} preview ${index + 1}`}
                  className="h-full w-full object-cover"
                />
              </button>
            ))}
          </div>
        </div>
      </div>

      {pointerState.visible && (
        <div className="pointer-events-none fixed right-8 top-1/2 z-40 hidden h-[320px] w-[240px] -translate-y-1/2 overflow-hidden border border-border bg-white shadow-[0_18px_50px_rgba(0,0,0,0.16)] md:block">
          <div
            className="h-full w-full bg-no-repeat"
            style={{
              backgroundImage: `url(${selectedImage})`,
              backgroundPosition: pointerState.backgroundPosition,
              backgroundSize: "220%",
            }}
          />
        </div>
      )}

      {previewOpen && (
        <div
          className="fixed inset-0 z-[120] flex items-center justify-center bg-black/85 px-4"
          onClick={() => setPreviewOpen(false)}
        >
          <button
            type="button"
            className="absolute right-6 top-6 text-white transition-opacity hover:opacity-70"
            onClick={() => setPreviewOpen(false)}
            aria-label="Close image preview"
          >
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>

          {galleryImages.length > 1 && (
            <>
              <button
                type="button"
                className={`absolute left-6 top-1/2 -translate-y-1/2 ${PREVIEW_BUTTON_CLASS}`}
                onClick={(event) => {
                  event.stopPropagation();
                  handleShowPrevious();
                }}
                aria-label="Previous image"
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path d="m15 18-6-6 6-6" />
                </svg>
              </button>
              <button
                type="button"
                className={`absolute right-6 top-1/2 -translate-y-1/2 ${PREVIEW_BUTTON_CLASS}`}
                onClick={(event) => {
                  event.stopPropagation();
                  handleShowNext();
                }}
                aria-label="Next image"
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path d="m9 18 6-6-6-6" />
                </svg>
              </button>
            </>
          )}

          <div
            className="max-h-[90vh] max-w-[min(90vw,1100px)]"
            onClick={(event) => event.stopPropagation()}
          >
            <img
              src={selectedImage}
              alt={`${productName} preview`}
              className="max-h-[90vh] max-w-full object-contain"
            />
          </div>
        </div>
      )}
    </>
  );
}
