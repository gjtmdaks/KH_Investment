package com.kh.investSpring.global.config;

/**
 * 공용호스트. OAuth redirect·프론트 콜백 기본값과 동일하게 유지.
 */
public final class TeamDevHost {

	public static final String HOST = "192.168.10.25";
	public static final int BACKEND_PORT = 8081;
	public static final int FRONTEND_PORT = 3000;
	public static final String CONTEXT_PATH = "/final";

	public static final String BACKEND_ORIGIN =
			"http://" + HOST + ":" + BACKEND_PORT + CONTEXT_PATH;
	public static final String FRONTEND_ORIGIN = "http://" + HOST + ":" + FRONTEND_PORT;

	private TeamDevHost() {
	}
}
