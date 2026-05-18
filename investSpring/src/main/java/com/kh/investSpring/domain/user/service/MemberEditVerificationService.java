package com.kh.investSpring.domain.user.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kh.investSpring.domain.user.dao.UserDao;
import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberEditVerificationService {

    private static final String VERIFIED_KEY_PREFIX = "member-edit:verified:";
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public String verifyAndIssueToken(Long userNo, String currentPassword) {
        assertLocalUser(userNo);

        if (!StringUtils.hasText(currentPassword)) {
            throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
        }

        LocalUser localUser = userDao.selectLocalUserByUserNo(userNo);

        if (localUser == null) {
            throw new IllegalArgumentException("로컬 계정 정보를 찾을 수 없습니다.");
        }

        if (!passwordEncoder.matches(currentPassword, localUser.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String editToken = UUID.randomUUID().toString();
        redis.opsForValue().set(VERIFIED_KEY_PREFIX + userNo, editToken, VERIFIED_TTL);

        return editToken;
    }

    public void assertVerified(Long userNo, String editToken) {
        assertLocalUser(userNo);

        if (!StringUtils.hasText(editToken)) {
            throw new IllegalArgumentException("회원정보 수정 인증이 필요합니다.");
        }

        String storedToken = redis.opsForValue().get(VERIFIED_KEY_PREFIX + userNo);

        if (!StringUtils.hasText(storedToken) || !storedToken.equals(editToken)) {
            throw new IllegalArgumentException("회원정보 수정 인증이 만료되었거나 유효하지 않습니다.");
        }
    }

    public void clearVerification(Long userNo) {
        if (userNo == null) {
            return;
        }

        redis.delete(VERIFIED_KEY_PREFIX + userNo);
    }

    private void assertLocalUser(Long userNo) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        User user = userDao.selectUserByUserNo(userNo);

        if (user == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }

        if (!"LOCAL".equalsIgnoreCase(user.getProvider())) {
            throw new IllegalArgumentException("로컬 계정만 비밀번호 확인이 필요합니다.");
        }
    }
}
