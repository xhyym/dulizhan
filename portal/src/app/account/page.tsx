"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useAuth } from "@/lib/auth";
import {
  COUNTRY_CODE_OPTIONS,
  DEFAULT_WHATSAPP_COUNTRY_CODE,
} from "@/lib/country-codes";
import {
  formatWhatsappNumber,
  parseWhatsappNumber,
} from "@/lib/whatsapp";

export default function AccountPage() {
  const { user, updateUsername, updateWhatsapp } = useAuth();
  const [isEditingUsername, setIsEditingUsername] = useState(false);
  const [usernameInput, setUsernameInput] = useState("");
  const [isSavingUsername, setIsSavingUsername] = useState(false);
  const [usernameError, setUsernameError] = useState("");
  const [usernameSuccess, setUsernameSuccess] = useState("");
  const [isEditingWhatsapp, setIsEditingWhatsapp] = useState(false);
  const [countryCode, setCountryCode] = useState(DEFAULT_WHATSAPP_COUNTRY_CODE);
  const [phoneNumber, setPhoneNumber] = useState("");
  const [isSavingWhatsapp, setIsSavingWhatsapp] = useState(false);
  const [whatsappError, setWhatsappError] = useState("");
  const [whatsappSuccess, setWhatsappSuccess] = useState("");
  const [countryCodeDropdownOpen, setCountryCodeDropdownOpen] = useState(false);
  const countryCodeDropdownRef = useRef<HTMLDivElement>(null);

  const parsedWhatsapp = useMemo(
    () => parseWhatsappNumber(user?.whatsapp),
    [user?.whatsapp]
  );

  useEffect(() => {
    if (!countryCodeDropdownOpen) {
      return;
    }

    const handleClickOutside = (event: MouseEvent) => {
      if (
        countryCodeDropdownRef.current &&
        !countryCodeDropdownRef.current.contains(event.target as Node)
      ) {
        setCountryCodeDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [countryCodeDropdownOpen]);

  const handleStartEditWhatsapp = () => {
    setCountryCode(parsedWhatsapp.countryCode || DEFAULT_WHATSAPP_COUNTRY_CODE);
    setPhoneNumber(parsedWhatsapp.phoneNumber);
    setWhatsappError("");
    setWhatsappSuccess("");
    setCountryCodeDropdownOpen(false);
    setIsEditingWhatsapp(true);
  };

  const handleStartEditUsername = () => {
    setUsernameInput(user?.username ?? "");
    setUsernameError("");
    setUsernameSuccess("");
    setIsEditingUsername(true);
  };

  const handleSaveUsername = async () => {
    if (!user) {
      return;
    }

    const normalizedUsername = usernameInput.trim();
    setUsernameError("");
    setUsernameSuccess("");

    if (!normalizedUsername) {
      setUsernameError("Please enter a username.");
      return;
    }

    if (normalizedUsername.length > 30) {
      setUsernameError("Username must be 30 characters or fewer.");
      return;
    }

    if (normalizedUsername === user.username) {
      setIsEditingUsername(false);
      return;
    }

    setIsSavingUsername(true);
    try {
      await updateUsername(normalizedUsername);
      setUsernameSuccess("Username updated successfully.");
      setIsEditingUsername(false);
    } catch (error) {
      setUsernameError(
        error instanceof Error ? error.message : "Failed to update username."
      );
    } finally {
      setIsSavingUsername(false);
    }
  };

  const handleSaveWhatsapp = async () => {
    if (!user) {
      return;
    }

    const normalizedCountryCode = countryCode.trim();
    const normalizedPhoneNumber = phoneNumber.replace(/\D/g, "");

    setWhatsappError("");
    setWhatsappSuccess("");

    if (!normalizedCountryCode || !/^\+\d{1,4}$/.test(normalizedCountryCode)) {
      setWhatsappError("Please enter a valid country code.");
      return;
    }

    if (!normalizedPhoneNumber) {
      setWhatsappError("Please enter a valid phone number.");
      return;
    }

    const nextWhatsapp = `${normalizedCountryCode}${normalizedPhoneNumber}`;
    if (nextWhatsapp === user.whatsapp) {
      setIsEditingWhatsapp(false);
      return;
    }

    setIsSavingWhatsapp(true);
    try {
      await updateWhatsapp(nextWhatsapp);
      setWhatsappSuccess("WhatsApp updated successfully.");
      setIsEditingWhatsapp(false);
    } catch (error) {
      setWhatsappError(
        error instanceof Error ? error.message : "Failed to update WhatsApp."
      );
    } finally {
      setIsSavingWhatsapp(false);
    }
  };

  if (!user) return null;

  return (
    <>
      <h2 className="text-xl font-medium tracking-wider uppercase mb-8">
        Account Information
      </h2>
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="text-sm text-muted block mb-1">Username</label>
            <div className="max-w-[360px]">
              <div className="flex items-center gap-3">
                {isEditingUsername ? (
                  <input
                    type="text"
                    value={usernameInput}
                    maxLength={30}
                    onChange={(event) => {
                      setUsernameInput(event.target.value);
                      setUsernameError("");
                      setUsernameSuccess("");
                    }}
                    onKeyDown={(event) => {
                      if (event.key === "Enter") {
                        event.preventDefault();
                        void handleSaveUsername();
                      }
                    }}
                    className="h-11 min-w-0 flex-1 border border-border bg-white px-3 text-sm text-foreground outline-none transition-colors focus:border-foreground"
                    placeholder="Enter username"
                  />
                ) : (
                  <p className="flex-1 text-foreground">{user.username}</p>
                )}

                {isEditingUsername ? (
                  <button
                    type="button"
                    onClick={() => void handleSaveUsername()}
                    disabled={isSavingUsername}
                    className="inline-flex h-10 w-10 shrink-0 items-center justify-center border border-border text-foreground transition-colors hover:border-foreground disabled:cursor-not-allowed disabled:opacity-50"
                    aria-label="Save username"
                    title="Save username"
                  >
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1.8"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M20 6 9 17l-5-5" />
                    </svg>
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={handleStartEditUsername}
                    className="inline-flex h-10 w-10 shrink-0 items-center justify-center border border-border text-foreground transition-colors hover:border-foreground"
                    aria-label="Edit username"
                    title="Edit username"
                  >
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1.8"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M12 20h9" />
                      <path d="M16.5 3.5a2.12 2.12 0 1 1 3 3L7 19l-4 1 1-4Z" />
                    </svg>
                  </button>
                )}
              </div>
              {usernameError ? (
                <p className="mt-2 text-sm text-red-600">{usernameError}</p>
              ) : null}
              {usernameSuccess ? (
                <p className="mt-2 text-sm text-green-600">{usernameSuccess}</p>
              ) : null}
            </div>
          </div>
          <div>
            <label className="text-sm text-muted block mb-1">Email</label>
            <p className="text-foreground">{user.email}</p>
          </div>
          <div>
            <label className="text-sm text-muted block mb-1">WhatsApp</label>
            <div className="max-w-[360px]">
              <div className="flex items-center gap-3">
                {isEditingWhatsapp ? (
                  <div className="flex min-h-11 flex-1 items-center border border-border bg-white">
                    <div
                      ref={countryCodeDropdownRef}
                      className="relative h-11 w-[120px] shrink-0 border-r border-border"
                    >
                      <button
                        type="button"
                        onClick={() => setCountryCodeDropdownOpen((previousValue) => !previousValue)}
                        className="flex h-full w-full items-center gap-2 bg-transparent px-3 text-sm text-foreground transition-colors hover:text-foreground"
                      >
                        <span>
                          {COUNTRY_CODE_OPTIONS.find((item) => item.code === countryCode)?.flag}
                        </span>
                        <span>{countryCode}</span>
                        <svg
                          className={`ml-auto h-3 w-3 text-muted transition-transform ${countryCodeDropdownOpen ? "rotate-180" : ""}`}
                          viewBox="0 0 12 12"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <path d="M3 4.5L6 7.5L9 4.5" />
                        </svg>
                      </button>
                      {countryCodeDropdownOpen ? (
                        <div className="absolute left-0 top-full z-20 mt-2 max-h-[240px] w-[240px] overflow-y-auto border border-border bg-white shadow-lg">
                          {COUNTRY_CODE_OPTIONS.map((item, index) => (
                            <button
                              type="button"
                              key={`${item.code}-${item.country}-${index}`}
                              onClick={() => {
                                setCountryCode(item.code);
                                setCountryCodeDropdownOpen(false);
                                setWhatsappError("");
                                setWhatsappSuccess("");
                              }}
                              className={`flex w-full items-center gap-2 px-3 py-2 text-left text-sm transition-colors hover:bg-surface ${
                                item.code === countryCode ? "bg-surface text-foreground" : "text-muted"
                              }`}
                            >
                              <span>{item.flag}</span>
                              <span>{item.country}</span>
                              <span className="ml-auto text-muted-light">{item.code}</span>
                            </button>
                          ))}
                        </div>
                      ) : null}
                    </div>
                    <input
                      type="tel"
                      value={phoneNumber}
                      onChange={(event) => {
                        setPhoneNumber(event.target.value.replace(/\D/g, "").slice(0, 20));
                        setWhatsappError("");
                        setWhatsappSuccess("");
                      }}
                      onKeyDown={(event) => {
                        if (event.key === "Enter") {
                          event.preventDefault();
                          void handleSaveWhatsapp();
                        }
                      }}
                      className="h-11 min-w-0 flex-1 bg-transparent px-3 text-sm text-foreground outline-none"
                      placeholder="13800138000"
                    />
                  </div>
                ) : (
                  <p className="flex-1 text-foreground">
                    {formatWhatsappNumber(user.whatsapp)}
                  </p>
                )}

                {isEditingWhatsapp ? (
                  <button
                    type="button"
                    onClick={() => void handleSaveWhatsapp()}
                    disabled={isSavingWhatsapp}
                    className="inline-flex h-10 w-10 shrink-0 items-center justify-center border border-border text-foreground transition-colors hover:border-foreground disabled:cursor-not-allowed disabled:opacity-50"
                    aria-label="Save WhatsApp"
                    title="Save WhatsApp"
                  >
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1.8"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M20 6 9 17l-5-5" />
                    </svg>
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={handleStartEditWhatsapp}
                    className="inline-flex h-10 w-10 shrink-0 items-center justify-center border border-border text-foreground transition-colors hover:border-foreground"
                    aria-label="Edit WhatsApp"
                    title="Edit WhatsApp"
                  >
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1.8"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M12 20h9" />
                      <path d="M16.5 3.5a2.12 2.12 0 1 1 3 3L7 19l-4 1 1-4Z" />
                    </svg>
                  </button>
                )}
              </div>
              {whatsappError ? (
                <p className="mt-2 text-sm text-red-600">{whatsappError}</p>
              ) : null}
              {whatsappSuccess ? (
                <p className="mt-2 text-sm text-green-600">{whatsappSuccess}</p>
              ) : null}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
