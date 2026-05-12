const KIS_VTS_BASE_URL = "https://openapivts.koreainvestment.com:29443";

export function resolveKisBaseUrl(): string {
  const configured = process.env.KIS_BASE_URL?.replace(/\/$/, "").trim();

  if (configured) {
    return configured;
  }

  return KIS_VTS_BASE_URL;
}
