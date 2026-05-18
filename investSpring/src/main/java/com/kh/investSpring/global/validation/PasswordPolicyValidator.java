package com.kh.investSpring.global.validation;

import java.util.regex.Pattern;

public final class PasswordPolicyValidator {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 16;

    private static final Pattern EXCLUDED_CHARS = Pattern.compile("[\\s'\"\\\\<>|`]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};:,./?~]");
    private static final Pattern ALLOWED_CHARS = Pattern.compile(
            "^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};:,./?~]+$"
    );

    private PasswordPolicyValidator() {
    }

    public static void validate(String password) {
        String message = validateMessage(password);
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String validateMessage(String password) {
        if (password == null || password.isBlank()) {
            return "비밀번호는 필수입니다.";
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return "비밀번호는 8자 이상 16자 이하여야 합니다.";
        }

        if (EXCLUDED_CHARS.matcher(password).find()) {
            return "비밀번호에 사용할 수 없는 문자가 있습니다. (공백, ', \", \\, <, >, |, ` 제외)";
        }

        if (!ALLOWED_CHARS.matcher(password).matches()) {
            return "비밀번호는 영문, 숫자, 허용된 특수문자(!@#$%^&* 등)만 사용할 수 있습니다.";
        }

        if (!UPPERCASE.matcher(password).find()) {
            return "비밀번호에 영문 대문자를 포함해야 합니다.";
        }

        if (!LOWERCASE.matcher(password).find()) {
            return "비밀번호에 영문 소문자를 포함해야 합니다.";
        }

        if (!SPECIAL_CHAR.matcher(password).find()) {
            return "비밀번호에 특수문자(!@#$%^&* 등)를 포함해야 합니다.";
        }

        return null;
    }
}
