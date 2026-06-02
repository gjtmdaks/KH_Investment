import type { QuoteSession } from "@/lib/stock/stockDetailTypes";

export function getQuoteSessionLabel(session?: QuoteSession | null) {
  switch (session) {
    case "NXT_PRE":
      return "프리마켓";
    case "KRX_REGULAR":
      return "정규장";
    case "NXT_AFTER":
      return "애프터마켓";
    case "OVERTIME":
      return "시간외";
    case "CLOSED":
      return "마감";
    default:
      return "정규장";
  }
}

export function isQuotePollingStopped(session?: QuoteSession | null) {
  return session === "CLOSED";
}
