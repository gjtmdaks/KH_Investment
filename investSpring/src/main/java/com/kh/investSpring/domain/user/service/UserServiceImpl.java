package com.kh.investSpring.domain.user.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.main.dto.MainResponse.Header;
import com.kh.investSpring.domain.user.dao.UserDao;
import com.kh.investSpring.domain.user.dto.UserSignInRequest;
import com.kh.investSpring.domain.user.dto.UserSignInResponse;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;
import com.kh.investSpring.global.util.JwtUtil;
import com.kh.investSpring.domain.user.dto.UserResetPasswordRequest;
import lombok.RequiredArgsConstructor;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;
    
    
    @Override
    public Header getHeader(Long userNo) {
        if (userNo == null) {
            return null;
        }
        User user = userDao.selectUserByUserNo(userNo);
        if (user == null) {
            return null;
        }
        String name = user.getUserName();
        if (name == null || name.isBlank()) {
            name = "회원";
        }
        return Header.builder().userNo(userNo).userName(name).build();
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

	@Override
	public UserSignInResponse signIn(UserSignInRequest request) {
	    LocalUser localUser = userDao.selectLocalUserByUserId(request.getUserId());

	    if (localUser == null) {
	        throw new RuntimeException("존재하지 않는 아이디입니다.");
	    }

	    if (!localUser.getPassword().equals(request.getPassword())) {
	        throw new RuntimeException("비밀번호가 일치하지 않습니다.");
	    }

	    User user = userDao.selectUserByUserNo(localUser.getUserNo());

	    if (user == null) {
	        throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
	    }

	    if (!"LOCAL".equalsIgnoreCase(user.getProvider())) {
	        throw new RuntimeException("로컬 로그인 계정이 아닙니다.");
	    }

	    String accessToken = jwtUtil.createToken((long) user.getUserNo());

	    return new UserSignInResponse(
	            accessToken,
	            user.getUserNo(),
	            localUser.getUserId(),
	            user.getUserName(),
	            user.getEmail(),
	            user.getPhone(),
	            user.getProvider(),
	            user.getAuth()
	    );
	}
	@Override
	@Transactional
	public void resetPassword(UserResetPasswordRequest request) {

	    LocalUser localUser =
	            userDao.selectLocalUserByUserIdAndUserName(
	                    request.getUserId(),
	                    request.getUserName()
	            );

	    if (localUser == null) {
	        throw new RuntimeException("회원 정보가 일치하지 않습니다.");
	    }

	    localUser.setPassword(request.getNewPassword());

	    userDao.updatePassword(localUser);
	}
}
