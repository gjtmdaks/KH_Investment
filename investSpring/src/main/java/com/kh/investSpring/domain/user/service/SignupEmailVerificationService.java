package com.kh.investSpring.domain.user.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.kh.investSpring.domain.user.dao.UserDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupEmailVerificationService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final String CODE_KEY_PREFIX = "signup:email:code:";
    private static final String VERIFIED_KEY_PREFIX = "signup:email:verified:";

    private final StringRedisTemplate redis;
    private final JavaMailSender mailSender;
    private final UserDao userDao;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.signup.email.code-ttl-seconds:300}")
    private long codeTtlSeconds;

    @Value("${app.signup.email.verified-ttl-seconds:300}")
    private long verifiedTtlSeconds;

    @Value("${spring.mail.username}")
    private String fromAddress;

    private static final String FROM_DISPLAY_NAME = "KH증권";
    private static final String MAIL_SUBJECT = "[KH증권] 이메일 인증번호";

    public void sendVerificationCode(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        validateEmailFormat(email);

        if (userDao.countActiveUserByEmail(email) > 0) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String code = generateSixDigitCode();
        String codeKey = CODE_KEY_PREFIX + email;

        redis.opsForValue().set(codeKey, code, Duration.ofSeconds(codeTtlSeconds));
        redis.delete(VERIFIED_KEY_PREFIX + email);

        String bodyText =
                "KH증권 회원가입 이메일 인증번호입니다.\n\n"
                        + "인증번호: " + code + "\n"
                        + "유효시간: " + (codeTtlSeconds / 60) + "분\n\n"
                        + "본인이 요청하지 않았다면 이 메일을 무시해주세요.";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            InternetAddress from = new InternetAddress(
                    fromAddress,
                    FROM_DISPLAY_NAME,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject(MAIL_SUBJECT);
            helper.setText(bodyText, false);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("인증 메일 발송에 실패했습니다.", e);
        }
    }

    public long getCodeTtlSeconds() {
        return codeTtlSeconds;
    }

    public void verifyCode(String rawEmail, String rawCode) {
        String email = normalizeEmail(rawEmail);
        validateEmailFormat(email);

        if (!StringUtils.hasText(rawCode) || !CODE_PATTERN.matcher(rawCode.trim()).matches()) {
            throw new IllegalArgumentException("인증번호는 6자리 숫자여야 합니다.");
        }

        String storedCode = redis.opsForValue().get(CODE_KEY_PREFIX + email);
        if (!StringUtils.hasText(storedCode)) {
            throw new IllegalArgumentException("인증번호가 만료되었거나 발송 이력이 없습니다.");
        }

        if (!storedCode.equals(rawCode.trim())) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        redis.opsForValue().set(
                VERIFIED_KEY_PREFIX + email,
                "1",
                Duration.ofSeconds(verifiedTtlSeconds)
        );
        redis.delete(CODE_KEY_PREFIX + email);
    }

    public void assertEmailVerified(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        validateEmailFormat(email);

        String verified = redis.opsForValue().get(VERIFIED_KEY_PREFIX + email);
        if (!"1".equals(verified)) {
            throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
        }
    }

    public void clearEmailVerified(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        redis.delete(VERIFIED_KEY_PREFIX + email);
    }

    public String normalizeEmail(String rawEmail) {
        if (!StringUtils.hasText(rawEmail)) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        return rawEmail.trim().toLowerCase();
    }

    private void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }

    private String generateSixDigitCode() {
        int value = secureRandom.nextInt(900_000) + 100_000;
        return String.valueOf(value);
    }
}
