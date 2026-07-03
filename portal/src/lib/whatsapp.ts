import {
  COUNTRY_CODE_OPTIONS,
  DEFAULT_WHATSAPP_COUNTRY_CODE,
} from "@/lib/country-codes";

const KNOWN_WHATSAPP_COUNTRY_CODES = COUNTRY_CODE_OPTIONS.map((item) => item.code).sort(
  (leftCode, rightCode) => rightCode.length - leftCode.length
);

interface ParsedWhatsapp {
  countryCode: string;
  phoneNumber: string;
}

export function parseWhatsappNumber(whatsapp?: string | null): ParsedWhatsapp {
  const normalizedWhatsapp = whatsapp?.trim() ?? "";
  if (!normalizedWhatsapp) {
    return {
      countryCode: DEFAULT_WHATSAPP_COUNTRY_CODE,
      phoneNumber: "",
    };
  }

  const matchedCountryCode = KNOWN_WHATSAPP_COUNTRY_CODES.find((countryCode) =>
    normalizedWhatsapp.startsWith(countryCode)
  );

  if (matchedCountryCode) {
    return {
      countryCode: matchedCountryCode,
      phoneNumber: normalizedWhatsapp.slice(matchedCountryCode.length),
    };
  }

  if (normalizedWhatsapp.startsWith("+")) {
    const fallbackCountryCode = normalizedWhatsapp.match(/^\+\d{1,4}/)?.[0];
    if (fallbackCountryCode) {
      return {
        countryCode: fallbackCountryCode,
        phoneNumber: normalizedWhatsapp.slice(fallbackCountryCode.length),
      };
    }
  }

  return {
    countryCode: DEFAULT_WHATSAPP_COUNTRY_CODE,
    phoneNumber: normalizedWhatsapp.replace(/\D/g, ""),
  };
}

export function formatWhatsappNumber(whatsapp?: string | null): string {
  if (!whatsapp) {
    return "-";
  }

  const { countryCode, phoneNumber } = parseWhatsappNumber(whatsapp);
  return phoneNumber ? `${countryCode} ${phoneNumber}` : countryCode;
}
