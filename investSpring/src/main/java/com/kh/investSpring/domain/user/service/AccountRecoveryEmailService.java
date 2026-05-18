package com.kh.investSpring.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountRecoveryEmailService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final String FROM_DISPLAY_NAME = "KH증권";

    private final JavaMailSender mailSender;
    private final SignupEmailVerificationService signupEmailVerificationService;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendUserId(String rawEmail, String userId) {
        String email = normalizeEmail(rawEmail);

        String bodyText =
                "KH증권 아이디 찾기 안내입니다.\n\n"
                        + "가입하신 아이디: " + userId + "\n\n"
                        + "본인이 요청하지 않았다면 이 메일을 무시해주세요.";

        sendMail(email, "[KH증권] 아이디 찾기", bodyText);
    }

    public void sendTemporaryPassword(String rawEmail, String temporaryPassword) {
        String email = normalizeEmail(rawEmail);

        String bodyText =
                "KH증권 임시 비밀번호 안내입니다.\n\n"
                        + "임시 비밀번호: " + temporaryPassword + "\n\n"
                        + "본 비밀번호는 임시 비밀번호입니다. "
                        + "로그인 후 즉시 비밀번호를 재설정해 주세요.\n\n"
                        + "본인이 요청하지 않았다면 이 메일을 무시해주세요.";

        sendMail(email, "[KH증권] 임시 비밀번호 안내", bodyText);
    }

    public String normalizeEmail(String rawEmail) {
        return signupEmailVerificationService.normalizeEmail(rawEmail);
    }

    private void sendMail(String to, String subject, String bodyText) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            InternetAddress from = new InternetAddress(
                    fromAddress,
                    FROM_DISPLAY_NAME,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(bodyText, false);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("메일 발송에 실패했습니다.", e);
        }
    }

    public void validateEmailFormat(String email) {
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }
}
