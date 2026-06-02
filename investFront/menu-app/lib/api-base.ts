import { TEAM_API_BASE } from "@/lib/team-dev";

function normalizeFinalUrl(raw: string): string {
  let cleaned = raw.replace(/\/$/, "").trim();
  cleaned = cleaned.replace(
    /^(https?:\/\/)(localhost)(\d{4})(?=\/|$)/i,
    (_, proto, _host, port) => `${proto}localhost:${port}`
  );

  if (cleaned.endsWith("/final")) {
    return cleaned;
  }

  if (/^https?:\/\/[^/]+$/i.test(cleaned)) {
    return `${cleaned}/final`;
  }

  return cleaned;
}

export function getPublicApiBase(): string {
  const raw = process.env.NEXT_PUBLIC_API_URL;

  if (!raw) {
    return TEAM_API_BASE;
  }

  return normalizeFinalUrl(raw);
}

function resolveApiBase(): string {
  const raw = process.env.NEXT_PUBLIC_API_URL;

  if (!raw) {
    return TEAM_API_BASE;
  }

  return normalizeFinalUrl(raw);
}

export const API_BASE_URL = resolveApiBase();
