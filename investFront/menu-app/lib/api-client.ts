"use client";

import axios, { AxiosHeaders, type AxiosError } from "axios";

import { API_BASE_URL } from "@/lib/api-base";

export { API_BASE_URL };

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

type AuthErrorBody = {
  code?: string;
  message?: string;
};

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
    const status = error.response?.status;
    const code = error.response?.data?.code;

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
