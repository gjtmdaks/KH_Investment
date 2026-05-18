package com.kh.investSpring.domain.user.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.domain.account.dao.AccountDao;
import com.kh.investSpring.domain.main.dto.MainResponse.Header;
import com.kh.investSpring.domain.user.dao.UserDao;
import com.kh.investSpring.domain.user.dto.InvestmentTypeAnswerRequest;
import com.kh.investSpring.domain.user.dto.InvestmentTypeSaveRequest;
import com.kh.investSpring.domain.user.dto.UserMeResponse;
import com.kh.investSpring.domain.user.dto.UserResetPasswordRequest;
import com.kh.investSpring.domain.user.dto.UserSignInRequest;
import com.kh.investSpring.domain.user.dto.UserSignInResponse;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.dto.UserUpdateRequest;
import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;
import com.kh.investSpring.global.jwt.JwtTokenProvider;
import com.kh.investSpring.global.validation.PasswordPolicyValidator;

import lombok.RequiredArgsConstructor;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String LOGIN_CREDENTIAL_MISMATCH_MESSAGE =
            "아이디 혹은 비밀번호가 일치하지 않습니다.";

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$");

    private final UserDao userDao;
    private final AccountDao accountDao;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final SignupEmailVerificationService signupEmailVerificationService;
    
    
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

        String email = signupEmailVerificationService.normalizeEmail(request.email());
        signupEmailVerificationService.assertEmailVerified(email);

        int count = userDao.selectByUserId(request.userId());

        if (count > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userDao.countActiveUserByEmail(email) > 0) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setUserName(request.userName().trim());
        user.setEmail(email);
        user.setPhone(normalizePhone(request.phone()));
        user.setAuth(2);

        userDao.insertUser(user);

        String encodedPassword = passwordEncoder.encode(request.password());

        LocalUser localUser = new LocalUser(
                user.getUserNo(),
                request.userId(),
                encodedPassword
        );

        userDao.insertLocalUser(localUser);
        
        accountDao.insertAccount(user.getUserNo());
        accountDao.insertAccountBalance(user.getUserNo());

        signupEmailVerificationService.clearEmailVerified(email);

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

        PasswordPolicyValidator.validate(request.password());

        if (request.passwordConfirm() == null || request.passwordConfirm().isBlank()) {
            throw new IllegalArgumentException("비밀번호 확인은 필수입니다.");
        }

        if (!request.password().equals(request.passwordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (request.userName() == null || request.userName().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }

        String phone = normalizePhone(request.phone());
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("올바른 휴대전화 번호 형식이 아닙니다.");
        }
    }

    private String normalizePhone(String rawPhone) {
        return rawPhone.trim().replaceAll("\\s+", "");
    }

	@Override
	public UserSignInResponse signIn(UserSignInRequest request) {
	    LocalUser localUser = userDao.selectLocalUserByUserId(request.getUserId());
	    
	    if (localUser == null) {
	        throw new IllegalArgumentException(LOGIN_CREDENTIAL_MISMATCH_MESSAGE);
	    }

	    if (!passwordEncoder.matches(request.getPassword(), localUser.getPassword())) {
	        throw new IllegalArgumentException(LOGIN_CREDENTIAL_MISMATCH_MESSAGE);
	    }

	    User user = userDao.selectUserByUserNo(localUser.getUserNo());

	    if (user == null) {
	        throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
	    }

	    if (!"LOCAL".equalsIgnoreCase(user.getProvider())) {
	        throw new IllegalArgumentException("로컬 로그인 계정이 아닙니다.");
	    }

	    String accessToken = jwtTokenProvider.createAccessToken((long) user.getUserNo());

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
	public void userDelete(Long userNo) {
		if (userNo == null) {
	        throw new IllegalArgumentException("로그인이 필요합니다.");
	    }

	    User user = userDao.selectUserByUserNo(userNo);

	    if (user == null) {
	        throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
	    }
	    
	    
	    int result = userDao.updateUserStatusDelete(userNo);

	    if (result == 0) {
	        throw new IllegalStateException("회원 탈퇴 처리에 실패했습니다.");
	    }
	    
	    accountDao.updateAccountStatusDeleteByUserNo(userNo);
		
	}

	@Override
	public UserMeResponse getMyInfo(Long userNo) {
	    if (userNo == null) {
	        throw new IllegalArgumentException("로그인이 필요합니다.");
	    }

	    User user = userDao.selectUserByUserNo(userNo);

	    if (user == null) {
	        throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
	    }

	    String userId = null;

	    if ("LOCAL".equalsIgnoreCase(user.getProvider())) {
	        LocalUser localUser = userDao.selectLocalUserByUserNo(userNo);

	        if (localUser != null) {
	            userId = localUser.getUserId();
	        }
	    }
	    
	    Integer investmentTotalPoint =
	            userDao.selectInvestmentTotalPointByUserNo(userNo);

	    String investmentType = null;

	    if (investmentTotalPoint != null) {
	        investmentType = getInvestmentResultType(investmentTotalPoint);
	    }
	    
	    return new UserMeResponse(
	            user.getUserNo(),
	            userId,
	            user.getUserName(),
	            user.getEmail(),
	            user.getPhone(),
	            user.getProvider(),
	            user.getAuth(),
	            investmentTotalPoint,
	            investmentType
	    );
	}

	@Override
	@Transactional
	public UserMeResponse updateMyInfo(Long userNo, UserUpdateRequest updateRequest) {
	    if (userNo == null) {
	        throw new IllegalArgumentException("로그인이 필요합니다.");
	    }

	    if (updateRequest == null) {
	        throw new IllegalArgumentException("수정할 회원정보가 없습니다.");
	    }

	    if (updateRequest.getUserName() == null || updateRequest.getUserName().isBlank()) {
	        throw new IllegalArgumentException("이름은 필수입니다.");
	    }

	    User user = userDao.selectUserByUserNo(userNo);

	    if (user == null) {
	        throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
	    }

	    user.setUserName(updateRequest.getUserName().trim());
	    user.setEmail(toNullIfBlank(updateRequest.getEmail()));
	    user.setPhone(toNullIfBlank(updateRequest.getPhone()));

	    int result = userDao.updateUserInfo(user);

	    if (result == 0) {
	        throw new IllegalStateException("회원정보 수정에 실패했습니다.");
	    }

	    return getMyInfo(userNo);
	}

	private String toNullIfBlank(String value) {
	    if (value == null || value.isBlank()) {
	        return null;
	    }

	    return value.trim();
	}

    @Override
    @Transactional
    public void resetPassword(UserResetPasswordRequest request) {
    	
    	// 검사 로직
    	if (request == null) {
            throw new IllegalArgumentException("요청값이 없습니다.");
        }

        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (request.getUserName() == null || request.getUserName().isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        PasswordPolicyValidator.validate(request.getNewPassword());
        // 검사 로직 끝
        
        LocalUser localUser =
                userDao.selectLocalUserByUserIdAndUserName(
                        request.getUserId(),
                        request.getUserName()
                );

        if (localUser == null) {
            throw new RuntimeException("회원 정보가 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        localUser.setPassword(encodedPassword);

        userDao.updatePassword(localUser);
    }

    @Override
    @Transactional
    public InvestmentTypeSaveRequest insertInvestmentType(
            Long userNo,
            InvestmentTypeSaveRequest saveRequest
    ) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (saveRequest == null) {
            throw new IllegalArgumentException("투자성향 분석 결과가 없습니다.");
        }

        if (saveRequest.getAnswers() == null || saveRequest.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("투자성향 답변이 없습니다.");
        }

        if (saveRequest.getAnswers().size() != 7) {
            throw new IllegalArgumentException("투자성향 답변은 7개여야 합니다.");
        }

        int calculatedTotalPoint = 0;

        for (InvestmentTypeAnswerRequest answer : saveRequest.getAnswers()) {
            if (answer == null) {
                throw new IllegalArgumentException("잘못된 답변 정보가 포함되어 있습니다.");
            }

            if (answer.getQuestionNo() < 1 || answer.getQuestionNo() > 7) {
                throw new IllegalArgumentException("잘못된 질문 번호입니다.");
            }

            if (answer.getOptionNo() < 1 || answer.getOptionNo() > 5) {
                throw new IllegalArgumentException("잘못된 선택지 번호입니다.");
            }

            if (answer.getPoint() < 1 || answer.getPoint() > 5) {
                throw new IllegalArgumentException("잘못된 점수입니다.");
            }

            if (answer.getQuestionText() == null || answer.getQuestionText().isBlank()) {
                throw new IllegalArgumentException("질문 내용이 없습니다.");
            }

            if (answer.getOptionText() == null || answer.getOptionText().isBlank()) {
                throw new IllegalArgumentException("선택지 내용이 없습니다.");
            }

            calculatedTotalPoint += answer.getPoint();
        }

        if (saveRequest.getTotalPoint() != calculatedTotalPoint) {
            throw new IllegalArgumentException("투자성향 총점이 올바르지 않습니다.");
        }

        String calculatedResultType = getInvestmentResultType(calculatedTotalPoint);

        if (saveRequest.getResultType() == null || saveRequest.getResultType().isBlank()) {
            throw new IllegalArgumentException("투자성향 결과가 없습니다.");
        }

        if (!calculatedResultType.equals(saveRequest.getResultType())) {
            throw new IllegalArgumentException("투자성향 결과가 올바르지 않습니다.");
        }

        User user = userDao.selectUserByUserNo(userNo);

        if (user == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }

        String resultFile = saveInvestmentTypeFile(
                userNo,
                calculatedTotalPoint,
                calculatedResultType,
                saveRequest
        );

        userDao.deleteInvestmentType(userNo);

        int result = userDao.insertInvestmentType(
                userNo,
                calculatedTotalPoint,
                resultFile
        );

        if (result == 0) {
            throw new IllegalStateException("투자성향 결과 저장에 실패했습니다.");
        }

        return saveRequest;
    }
    
    private String getInvestmentResultType(int totalPoint) {
        if (totalPoint <= 11) {
            return "안정형";
        }

        if (totalPoint <= 18) {
            return "안정추구형";
        }

        if (totalPoint <= 24) {
            return "위험중립형";
        }

        if (totalPoint <= 31) {
            return "적극투자형";
        }

        return "공격투자형";
    }
    
    private String saveInvestmentTypeFile(
            Long userNo,
            int totalPoint,
            String resultType,
            InvestmentTypeSaveRequest saveRequest
    ) {
        try {
            Path dir = Paths.get("uploads", "investment-type");
            Files.createDirectories(dir);

            String fileName = "user-" + userNo + ".json";
            Path filePath = dir.resolve(fileName);

            Map<String, Object> data = new HashMap<>();
            data.put("userNo", userNo);
            data.put("totalPoint", totalPoint);
            data.put("resultType", resultType);
            data.put("answers", saveRequest.getAnswers());
            
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), data);

            return "uploads/investment-type/" + fileName;
//            return "http://192.168.10.25:8081/uploads/investment-type/" + fileName;
        } catch (Exception e) {
            throw new IllegalStateException("투자성향 결과 파일 저장에 실패했습니다.", e);
        }
    }
}