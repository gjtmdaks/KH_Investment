const HIGH_RISK_NAME_PATTERN =
  /레버리지|인버스|INVERSE|곱버스|2X|2배|레버/i;

export function isHighRiskStock(stockName?: string | null): boolean {
  const name = stockName?.trim();

  if (!name) {
    return false;
  }

  return HIGH_RISK_NAME_PATTERN.test(name);
}

export const HIGH_RISK_ACK_SESSION_KEY = "invest:highRiskAck";

export function hasHighRiskAckInSession(): boolean {
  if (typeof window === "undefined") {
    return true;
  }

  try {
    return sessionStorage.getItem(HIGH_RISK_ACK_SESSION_KEY) === "1";
  } catch {
    return true;
  }
}

export function setHighRiskAckInSession(): void {
  if (typeof window === "undefined") {
    return;
  }

  try {
    sessionStorage.setItem(HIGH_RISK_ACK_SESSION_KEY, "1");
  } catch {
    /* ignore quota / private mode */
  }
}
