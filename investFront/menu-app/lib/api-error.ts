import { isAxiosError } from "axios";

type ApiErrorBody = {
  success?: boolean;
  message?: string;
};

const LOGIN_CREDENTIAL_MISMATCH_MESSAGE =
  "아이디 혹은 비밀번호가 일치하지 않습니다.";

const LEGACY_LOGIN_MISMATCH_MESSAGES = new Set([
  "존재하지 않는 아이디입니다.",
  "비밀번호가 일치하지 않습니다.",
]);

export function normalizeLoginErrorMessage(message: string | null | undefined): string {
  if (!message || message.trim() === "") {
    return LOGIN_CREDENTIAL_MISMATCH_MESSAGE;
  }

  if (LEGACY_LOGIN_MISMATCH_MESSAGES.has(message)) {
    return LOGIN_CREDENTIAL_MISMATCH_MESSAGE;
  }

  return message;
}

export function getLoginErrorMessage(
  error: unknown,
  fallback = "서버와 통신할 수 없습니다."
): string {
  if (isAxiosError<ApiErrorBody>(error)) {
    const message = error.response?.data?.message;
    if (typeof message === "string" && message.trim() !== "") {
      return normalizeLoginErrorMessage(message);
    }
  }

  return fallback;
}

export function getApiErrorMessage(
  error: unknown,
  fallback = "서버와 통신할 수 없습니다."
): string {
  if (isAxiosError<ApiErrorBody>(error)) {
    const message = error.response?.data?.message;
    if (typeof message === "string" && message.trim() !== "") {
      return message;
    }
  }

  return fallback;
}
