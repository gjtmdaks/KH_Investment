package com.kh.investSpring.domain.user.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class TemporaryPasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        char upper = UPPERCASE.charAt(secureRandom.nextInt(UPPERCASE.length()));
        char lower = LOWERCASE.charAt(secureRandom.nextInt(LOWERCASE.length()));
        int digits = secureRandom.nextInt(90000) + 10000;
        return "@" + upper + lower + digits;
    }
}
