function resolveApiBase(): string {
  const raw = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "").trim();

  if (!raw) {
    return "http://localhost:8081/final";
  }

  if (raw.endsWith("/final")) {
    return raw;
  }

  if (/^https?:\/\/[^/]+$/i.test(raw)) {
    return `${raw}/final`;
  }

  return raw;
}

export const API_BASE_URL = resolveApiBase();
