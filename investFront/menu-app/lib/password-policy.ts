export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_MAX_LENGTH = 16;

/** 공백, ', ", \, <, >, |, ` */
export const PASSWORD_EXCLUDED_PATTERN = /[\s'"\\<>|`]/;

export const PASSWORD_ALLOWED_PATTERN =
  /^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};:,./?~]+$/;

const PASSWORD_UPPERCASE = /[A-Z]/;
const PASSWORD_LOWERCASE = /[a-z]/;
const PASSWORD_SPECIAL = /[!@#$%^&*()_+\-=[\]{};:,./?~]/;

export const PASSWORD_POLICY_HINT =
  "8~16자, 영문 대·소문자, 특수문자(!@#$%^&* 등) 포함. 공백은 사용할 수 없습니다.";

export type PasswordValidationResult = {
  valid: boolean;
  message: string | null;
};

export function validatePassword(password: string): PasswordValidationResult {
  if (!password) {
    return { valid: false, message: "비밀번호는 필수입니다." };
  }

  if (
    password.length < PASSWORD_MIN_LENGTH ||
    password.length > PASSWORD_MAX_LENGTH
  ) {
    return {
      valid: false,
      message: "비밀번호는 8자 이상 16자 이하여야 합니다.",
    };
  }

  if (PASSWORD_EXCLUDED_PATTERN.test(password)) {
    return {
      valid: false,
      message:
        "비밀번호에 사용할 수 없는 문자가 있습니다. (공백, ', \", \\, <, >, |, ` 제외)",
    };
  }

  if (!PASSWORD_ALLOWED_PATTERN.test(password)) {
    return {
      valid: false,
      message:
        "비밀번호는 영문, 숫자, 허용된 특수문자(!@#$%^&* 등)만 사용할 수 있습니다.",
    };
  }

  if (!PASSWORD_UPPERCASE.test(password)) {
    return { valid: false, message: "비밀번호에 영문 대문자를 포함해야 합니다." };
  }

  if (!PASSWORD_LOWERCASE.test(password)) {
    return { valid: false, message: "비밀번호에 영문 소문자를 포함해야 합니다." };
  }

  if (!PASSWORD_SPECIAL.test(password)) {
    return {
      valid: false,
      message: "비밀번호에 특수문자(!@#$%^&* 등)를 포함해야 합니다.",
    };
  }

  return { valid: true, message: null };
}

export function isPasswordPolicyValid(password: string): boolean {
  return validatePassword(password).valid;
}
