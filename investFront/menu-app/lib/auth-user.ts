import { apiClient } from "@/lib/api-client";

export type LoginUser = {
  accessToken?: string;
  userNo: number;
  userId?: string | null;
  userName: string;
  email?: string | null;
  phone?: string | null;
  provider: string;
  auth: number;
};

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

export async function getCurrentUser(): Promise<LoginUser | null> {
  if (typeof window === "undefined") {
    return null;
  }

  const token = window.localStorage.getItem("accessToken");

  if (!token) {
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