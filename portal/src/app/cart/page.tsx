"use client";

import Link from "next/link";
import { useState } from "react";
import { useCart } from "@/lib/cart";
import { useAuth, useAuthFetch } from "@/lib/auth";
import { useRouter } from "next/navigation";
import ClientBanner from "@/components/ui/ClientBanner";
import MinimalDatePicker from "@/components/ui/MinimalDatePicker";
import { buildPortalImageUrl, PORTAL_IMAGE_PRESETS } from "@/lib/image-url";

function getTodayDateString(): string {
  const currentDate = new Date();
  const localDate = new Date(
    currentDate.getTime() - currentDate.getTimezoneOffset() * 60 * 1000
  );
  return localDate.toISOString().slice(0, 10);
}

export default function CartPage() {
  const { items, updateItem, removeItem, refreshCart, loading } = useCart();
  const { user, setShowLogin } = useAuth();
  const authFetch = useAuthFetch();
  const router = useRouter();
  const todayDate = getTodayDateString();
  const [note, setNote] = useState("");
  const [deliveryDate, setDeliveryDate] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [showWhatsappReminder, setShowWhatsappReminder] = useState(false);
  const [deliveryDateError, setDeliveryDateError] = useState("");
  const [submitError, setSubmitError] = useState("");

  const subtotal = items.reduce(
    (sum, item) =>
      sum + item.price * item.quantity,
    0
  );

  const handleSubmitInquiry = async () => {
    setDeliveryDateError("");
    setSubmitError("");

    if (!user) {
      setShowLogin(true);
      return;
    }

    if (!user.whatsapp?.trim()) {
      setShowWhatsappReminder(true);
      return;
    }

    if (!deliveryDate) {
      setDeliveryDateError("Please select a delivery date.");
      return;
    }

    if (deliveryDate < todayDate) {
      setDeliveryDateError("Delivery date cannot be earlier than today.");
      return;
    }

    setSubmitting(true);
    try {
      const res = await authFetch("/api/portal/inquiries", {
        method: "POST",
        body: JSON.stringify({ remark: note, deliveryDate }),
      });

      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.msg || err.message || "Failed to submit inquiry");
      }

      const data = await res.json();
      await refreshCart();
      router.push(`/inquiry/success?id=${data.data}`);
    } catch (error: unknown) {
      setSubmitError(
        error instanceof Error ? error.message : "Failed to submit inquiry"
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      {showWhatsappReminder ? (
        <div className="fixed inset-0 z-[110] flex items-center justify-center px-4">
          <div
            className="absolute inset-0 bg-black/45"
            onClick={() => setShowWhatsappReminder(false)}
          />
          <div className="relative w-full max-w-md bg-white p-8 shadow-xl">
            <button
              type="button"
              onClick={() => setShowWhatsappReminder(false)}
              className="absolute right-4 top-4 text-muted transition-colors hover:text-foreground"
            >
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            </button>
            <h3 className="mb-3 text-lg font-medium tracking-wider uppercase text-foreground">
              Update WhatsApp
            </h3>
            <p className="text-sm leading-6 text-muted">
              Please add your WhatsApp number in My Account before submitting an inquiry, so we can contact you about your request.
            </p>
            <div className="mt-6 flex gap-3">
              <button
                type="button"
                onClick={() => setShowWhatsappReminder(false)}
                className="flex-1 border border-border px-4 py-3 text-[12px] font-medium uppercase tracking-[2px] text-foreground transition-colors hover:border-foreground"
              >
                Not Now
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowWhatsappReminder(false);
                  router.push("/account");
                }}
                className="flex-1 bg-[#1a1a1a] px-4 py-3 text-[12px] font-medium uppercase tracking-[2px] text-white transition-colors hover:bg-[#333]"
              >
                Go to Account
              </button>
            </div>
          </div>
        </div>
      ) : null}

      <ClientBanner
        title="Your Cart"
        breadcrumbs={[
          { label: "Home", href: "/" },
          { label: "Cart" },
        ]}
      />

      {/* Content */}
      <section className="py-15 px-6 md:px-15">
        <div className="max-w-[1200px] mx-auto">
          {loading ? (
            <div className="text-center py-20 text-muted">Loading...</div>
          ) : items.length === 0 ? (
            <div className="text-center py-20">
              <p className="text-muted mb-6">Your cart is empty</p>
              <Link
                href="/products"
                className="inline-block px-12 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300"
              >
                Continue Shopping
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-[1fr_300px] gap-10">
              {/* Cart Items */}
              <div>
                <h2 className="text-lg font-medium tracking-wider uppercase mb-6">
                  Your Cart ({items.length} items)
                </h2>
                <div className="space-y-6">
                  {items.map((item) => (
                    <div
                      key={item.id}
                      className="flex gap-6 pb-6 border-b border-border"
                    >
                      <div className="w-24 h-24 overflow-hidden bg-surface flex-shrink-0">
                        <img
                          src={buildPortalImageUrl(item.mainImage, PORTAL_IMAGE_PRESETS.cartThumb)}
                          alt={item.productName}
                          className="w-full h-full object-cover"
                          loading="lazy"
                          decoding="async"
                        />
                      </div>
                      <div className="flex-1">
                        <h3 className="text-[15px] font-normal tracking-wider mb-1">
                          {item.productName}
                        </h3>
                        {item.specName && item.specValue && (
                          <p className="text-sm text-muted mb-3">
                            {item.specName}: {item.specValue}
                          </p>
                        )}
                        <div className="flex items-center justify-between">
                          <div className="flex items-center border border-border">
                            <button
                              className="px-3 py-1 text-sm hover:bg-surface transition-colors"
                              onClick={() =>
                                updateItem(item.id, item.quantity - 1)
                              }
                              disabled={item.quantity <= 1}
                            >
                              -
                            </button>
                            <span className="px-3 py-1 text-sm min-w-[32px] text-center">
                              {item.quantity}
                            </span>
                            <button
                              className="px-3 py-1 text-sm hover:bg-surface transition-colors"
                              onClick={() =>
                                updateItem(item.id, item.quantity + 1)
                              }
                            >
                              +
                            </button>
                          </div>
                          <div className="flex items-center gap-4">
                            <span className="text-sm font-medium">
                              ${item.price * item.quantity}
                            </span>
                            <button
                              onClick={() => removeItem(item.id)}
                              className="text-muted hover:text-foreground transition-colors"
                            >
                              <svg
                                width="16"
                                height="16"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                              >
                                <path d="M18 6L6 18M6 6l12 12" />
                              </svg>
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Summary */}
              <div>
                <div className="bg-surface p-6">
                  <h3 className="text-lg font-medium tracking-wider uppercase mb-6">
                    Summary
                  </h3>
                  <div className="flex justify-between mb-6">
                    <span className="text-sm text-muted">Subtotal</span>
                    <span className="text-sm font-medium">${subtotal}</span>
                  </div>
                  <div className="mb-6">
                    <MinimalDatePicker
                      label={
                        <>
                          <span className="mr-1 text-red-600">*</span>
                          Delivery Date
                        </>
                      }
                      value={deliveryDate}
                      minDate={todayDate}
                      onChange={(nextDate) => {
                        setDeliveryDate(nextDate);
                        setDeliveryDateError("");
                        setSubmitError("");
                      }}
                    />
                    {deliveryDateError ? (
                      <p className="mt-2 text-sm text-red-600">{deliveryDateError}</p>
                    ) : null}
                  </div>
                  <div className="mb-6">
                    <label className="text-sm text-muted block mb-2">
                      Note (optional)
                    </label>
                    <textarea
                      value={note}
                      onChange={(e) => {
                        setNote(e.target.value);
                        setSubmitError("");
                      }}
                      placeholder="Any special requirements..."
                      className="w-full p-3 text-sm border border-border resize-none h-24 focus:outline-none focus:border-foreground transition-colors"
                    />
                  </div>
                  {submitError ? (
                    <div className="mb-4 border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                      {submitError}
                    </div>
                  ) : null}
                  <button
                    onClick={handleSubmitInquiry}
                    disabled={submitting}
                    className="w-full px-8 py-4 text-[13px] font-medium tracking-[3px] uppercase bg-[#1a1a1a] text-white hover:bg-[#333] transition-all duration-300 disabled:opacity-50"
                  >
                    {submitting ? "Submitting..." : "Submit Inquiry"}
                  </button>
                  <Link
                    href="/products"
                    className="block text-center text-sm text-muted hover:text-foreground mt-4 transition-colors"
                  >
                    Continue Shopping
                  </Link>
                </div>
              </div>
            </div>
          )}
        </div>
      </section>
    </>
  );
}
