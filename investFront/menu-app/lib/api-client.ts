"use client";

import axios, { AxiosHeaders, type AxiosError } from "axios";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

type AuthErrorBody = {
  code?: string;
  message?: string;
};

function redirectToLogin() {
  if (typeof window === "undefined") return;
  const path = window.location.pathname + window.location.search;
  if (path.startsWith("/login")) return;
  window.location.assign("/login");
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

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<AuthErrorBody>) => {
    const status = error.response?.status;
    const code = error.response?.data?.code;

    if (status === 401 && code === "AUTH_REQUIRED") {
      redirectToLogin();
    }

    return Promise.reject(error);
  }
);
