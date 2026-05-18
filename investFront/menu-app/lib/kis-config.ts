const KIS_DEFAULT_BASE_URL = "https://openapi.koreainvestment.com:9443";

export function resolveKisBaseUrl(): string {
  const configured = process.env.KIS_BASE_URL?.replace(/\/$/, "").trim();

  if (configured) {
    return configured;
  }

  return KIS_DEFAULT_BASE_URL;
}
