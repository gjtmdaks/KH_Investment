package com.kh.investSpring.global.config;

/**
 * 요청 경로 화이트리스트. 컨트롤러를 나눌 때 {@code /oauth2}, {@code /api/public} 등과 맞추면 정리하기 쉽습니다.
 */
public final class SecurityPathPatterns {

	private SecurityPathPatterns() {
	}

	/**
	 * 로그인·회원가입·토큰 재발급 등 인증 없이 허용할 경로
	 */
	public static final String[] AUTH_WHITELIST = {
			"/",
			"/oauth2/**",
			"/login/oauth2/**",
			"/logout/oauth2/**",
			"/api/stocks/**",
			"/stock/**",
			"/api/news",
			"/users/login",
			"/users/signin/**",
			"/users/signup/**",
			"/users/logout",
			"/users/logout/**",
			"/users/find_id",
			"/users/find_password",
			"/_next/**",
			"/watchlist",
			"/watchlist/**",
			"/recent-view/**",
			"/notice",
			"/notice/{noticeId}",
			"/search/**",
			"/admin/**" // 개발단계에서만
	};

	/**
	 * 헬스체크·공개 조회 API 등
	 */
	public static final String[] PUBLIC_WHITELIST = {
			"/",
			"/api/public/**",
			"/api/main",
			"/error"
	};
}
