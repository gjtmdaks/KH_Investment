import json

def build_user_profile_prompt(data: dict) -> str:

    return f"""
당신은 투자 성향 분석 AI입니다.

사용자의 설문 투자 성향과
실제 포트폴리오 데이터를 비교하여
실제 투자 성향을 분석하세요.

반드시 아래 JSON 형식으로만 응답하세요.

{{
  "survey_type": "위험중립형",

  "actual_investment_type":
    "실제 투자 패턴은 공격투자 성향에 가깝습니다.",

  "portfolio_risk_analysis": [
    "반도체 업종 비중 62%",
    "KOSDAQ 성장주 비중 높음",
    "현금 보유 비중 낮음"
  ],

  "ai_recommendations": [
    "ETF 비중 확대 고려",
    "업종 분산 필요",
    "손절 기준 설정 권장"
  ]
}}

사용자 데이터:
{json.dumps(data, ensure_ascii=False, indent=2)}
"""