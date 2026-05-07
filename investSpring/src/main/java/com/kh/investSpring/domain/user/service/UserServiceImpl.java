package com.kh.investSpring.domain.user.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.main.dto.MainResponse.Header;
import com.kh.investSpring.domain.user.dao.UserDao;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;

import lombok.RequiredArgsConstructor;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public Header getHeader(Long userNo) {
        return null;
    }

    @Override
    @Transactional
    public UserSignUpResponse signUp(UserSignUpRequest request) {
        validateSignUpRequest(request);

        int count = userDao.selectByUserId(request.userId());

        if (count > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = new User();
        user.setUserName(request.userName());
        user.setAuth(2); // 일반 유저

        userDao.insertUser(user);

        LocalUser localUser = new LocalUser(
                user.getUserNo(),
                request.userId(),
                request.password()
        );

        userDao.insertLocalUser(localUser);

        return new UserSignUpResponse(
                user.getUserNo(),
                request.userId(),
                request.userName()
        );
    }

    private void validateSignUpRequest(UserSignUpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("회원가입 요청값이 없습니다.");
        }

        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        if (request.password().length() < 4) {
            throw new IllegalArgumentException("비밀번호는 최소 4글자 이상이어야 합니다.");
        }

        if (request.userName() == null || request.userName().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
    }
}