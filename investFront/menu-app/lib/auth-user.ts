import { API_BASE_URL, apiClient } from "@/lib/api-client";

export type LoginUser = {
  userNo: number;
  userId?: string | null;
  userName: string;
  email?: string | null;
  phone?: string | null;
  provider: string;
  auth: number;

  investmentTotalPoint?: number | null;
  investmentType?: string | null;
};

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

type LogoutData = {
  kakaoLogoutUrl?: string | null;
};

export function getLogoutUrl(): string {
  return `${API_BASE_URL.replace(/\/$/, "")}/users/logout/kakao`;
}

function redirectToLoggedOutHome(): void {
  if (typeof window === "undefined") {
    return;
  }

  window.location.assign("/main?auth=logged_out");
}

export async function getCurrentUser(): Promise<LoginUser | null> {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const { data } = await apiClient.get<ApiEnvelope<LoginUser>>("/users/me");

    if (!data.success || !data.data) {
      return null;
    }

    window.localStorage.setItem("user", JSON.stringify(data.data));

    return data.data;
  } catch {
    const savedUser = window.localStorage.getItem("user");

    if (!savedUser) {
      return null;
    }

    try {
      return JSON.parse(savedUser);
    } catch {
      return null;
    }
  }
}

export function getSavedUser(): LoginUser | null {
  if (typeof window === "undefined") {
    return null;
  }

  const savedUser = window.localStorage.getItem("user");

  if (!savedUser) {
    return null;
  }

  try {
    return JSON.parse(savedUser);
  } catch {
    return null;
  }
}

export function clearLoginStorage() {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.removeItem("accessToken");
  window.localStorage.removeItem("user");
  window.sessionStorage.clear();
}

export function completeLogoutFromQuery(): boolean {
  if (typeof window === "undefined") {
    return false;
  }

  const params = new URLSearchParams(window.location.search);
  if (params.get("auth") !== "logged_out") {
    return false;
  }

  clearLoginStorage();
  params.delete("auth");
  const query = params.toString();
  const nextUrl = query
    ? `${window.location.pathname}?${query}`
    : window.location.pathname;
  window.history.replaceState(null, "", nextUrl);
  return true;
}

export async function performAppLogout(): Promise<void> {
  try {
    await apiClient.post("/users/logout");
  } catch {
    // ignore
  }

  clearLoginStorage();
}

export async function performLogout(): Promise<void> {
  if (typeof window === "undefined") {
    return;
  }

  const provider = getSavedUser()?.provider?.toUpperCase();

  if (provider === "LOCAL") {
    await performAppLogout();
    redirectToLoggedOutHome();
    return;
  }

  try {
    const { data } = await apiClient.post<ApiEnvelope<LogoutData>>("/users/logout");
    const kakaoLogoutUrl = data.data?.kakaoLogoutUrl?.trim();

    clearLoginStorage();

    if (kakaoLogoutUrl) {
      window.location.assign(kakaoLogoutUrl);
      return;
    }
  } catch {
    // ignore
  }

  window.location.assign(getLogoutUrl());
}
