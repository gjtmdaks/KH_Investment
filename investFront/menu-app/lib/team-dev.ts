/** 팀 LAN 공용 호스트 — 카카오 OAuth·백엔드 API와 동일하게 유지 */
export const TEAM_DEV_HOST =
  process.env.NEXT_PUBLIC_TEAM_DEV_HOST?.trim() || "192.168.10.25";

export const TEAM_API_BASE = `http://${TEAM_DEV_HOST}:8081/final`;
