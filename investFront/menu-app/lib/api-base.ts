// function resolveApiBase(): string {
//   const raw = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "").trim();

//   if (!raw) {
//     return "http://localhost:8081/final";
//   }

//   if (raw.endsWith("/final")) {
//     return raw;
//   }

//   if (/^https?:\/\/[^/]+$/i.test(raw)) {
//     return `${raw}/final`;
//   }

//   return raw;
// }

// export const API_BASE_URL = resolveApiBase();

// 위

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
    return "http://localhost:8081/final";
  }

  return normalizeFinalUrl(raw);
}

function resolveApiBase(): string {
  if (typeof window !== "undefined") {
    const protocol = window.location.protocol;
    const hostname = window.location.hostname;

    if (hostname === "localhost" || hostname === "127.0.0.1") {
      return "http://localhost:8081/final";
    }

    return `${protocol}//${hostname}:8081/final`;
  }

  const raw = process.env.NEXT_PUBLIC_API_URL;

  if (!raw) {
    return "http://localhost:8081/final";
  }

  return normalizeFinalUrl(raw);
}

export const API_BASE_URL = resolveApiBase();
