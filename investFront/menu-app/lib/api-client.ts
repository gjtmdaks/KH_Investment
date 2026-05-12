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

function redirectToLogin() {
  if (typeof window === "undefined") return;
  const path = window.location.pathname + window.location.search;
    if (path.startsWith("/sign-in")) return;
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
      redirectToLogin();
    }

    return Promise.reject(error);
  }
);
