export const POST_LOGIN_REDIRECT_SESSION_KEY = "postLoginRedirect";

export function sanitizeRedirectPath(
  path: string | null | undefined
): string | null {
  if (!path || typeof path !== "string") {
    return null;
  }

  const trimmed = path.trim();

  if (!trimmed.startsWith("/")) {
    return null;
  }

  if (trimmed.startsWith("//")) {
    return null;
  }

  if (trimmed.includes("://")) {
    return null;
  }

  if (trimmed.startsWith("/sign-in")) {
    return null;
  }

  return trimmed;
}

export function buildSignInUrl(returnPath: string): string {
  const safe = sanitizeRedirectPath(returnPath);

  if (!safe) {
    return "/sign-in";
  }

  return `/sign-in?redirect=${encodeURIComponent(safe)}`;
}

export function storePostLoginRedirect(path: string): void {
  const safe = sanitizeRedirectPath(path);

  if (!safe || typeof window === "undefined") {
    return;
  }

  try {
    sessionStorage.setItem(POST_LOGIN_REDIRECT_SESSION_KEY, safe);
  } catch {
    /* ignore quota / private mode */
  }
}

export function consumePostLoginRedirect(fallback = "/main"): string {
  if (typeof window === "undefined") {
    return fallback;
  }

  try {
    const fromSession = sessionStorage.getItem(POST_LOGIN_REDIRECT_SESSION_KEY);
    sessionStorage.removeItem(POST_LOGIN_REDIRECT_SESSION_KEY);

    return sanitizeRedirectPath(fromSession) ?? fallback;
  } catch {
    return fallback;
  }
}

export function resolvePostLoginRedirect(
  redirectParam: string | null | undefined,
  fallback = "/main"
): string {
  return sanitizeRedirectPath(redirectParam) ?? fallback;
}
