"use client";

import axios, {
  AxiosHeaders,
  type AxiosError,
  type AxiosResponse,
} from "axios";

import { API_BASE_URL } from "@/lib/api-base";

export { API_BASE_URL };

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

type AuthErrorBody = {
  code?: string;
  message?: string;
  success?: boolean;
};

type ApiFailureBody = {
  success: false;
  message?: string;
  data?: unknown;
};

/** 백엔드 ApiResponse.fail + 400 — 예상 가능한 검증 오류(로그인 실패 등) */
function isExpectedApiFailure(
  response: AxiosResponse<unknown> | undefined
): response is AxiosResponse<ApiFailureBody> {
  if (!response || response.status !== 400) {
    return false;
  }

  const body = response.data;
  return (
    body != null &&
    typeof body === "object" &&
    "success" in body &&
    (body as ApiFailureBody).success === false
  );
}

const authDebugEnabled = process.env.NODE_ENV === "development";

function redirectToLogin(payload?: Record<string, unknown>) {
  if (typeof window === "undefined") return;
  const path = window.location.pathname + window.location.search;
  if (path.startsWith("/sign-in")) {
    if (authDebugEnabled) {
      console.warn("[auth-debug] redirectToLogin skipped (already sign-in)", {
        pathname: path,
        ...payload,
      });
    }
    return;
  }
  if (authDebugEnabled) {
    console.warn("[auth-debug] redirect to /sign-in", {
      fromPath: path,
      ...payload,
    });
  }
  window.location.assign("/sign-in");
}

apiClient.interceptors.request.use((config) => {
  if (typeof window === "undefined") return config;

  const accessToken = window.localStorage.getItem("accessToken");
  if (!accessToken) return config;

  const headers = AxiosHeaders.from(config.headers);
  headers.set("Authorization", `Bearer ${accessToken}`);
  config.headers = headers;
  return config;
});

console.log("API_BASE_URL =", API_BASE_URL);

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<AuthErrorBody>) => {
    const response = error.response;

    if (isExpectedApiFailure(response)) {
      return response;
    }

    const status = response?.status;
    const code = response?.data?.code;

    if (status === 401 && code === "AUTH_REQUIRED") {
      const cfg = error.config;
      const reqUrl =
        cfg != null ? `${cfg.baseURL ?? ""}${cfg.url ?? ""}` : "(unknown)";
      redirectToLogin({
        reason: "401_AUTH_REQUIRED",
        requestMethod: cfg?.method?.toUpperCase(),
        requestUrl: reqUrl,
        responseStatus: status,
        responseCode: code,
        responseMessage: error.response?.data?.message,
        responseBody: error.response?.data,
      });
    } else if (authDebugEnabled && status === 401) {
      console.warn("[auth-debug] 401 without AUTH_REQUIRED (no redirect)", {
        url: `${error.config?.baseURL ?? ""}${error.config?.url ?? ""}`,
        method: error.config?.method?.toUpperCase(),
        bodyCode: error.response?.data?.code,
        body: error.response?.data,
      });
    }

    return Promise.reject(error);
  }
);
