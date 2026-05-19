import json

def parse_json_response(content: str):

    try:
        return json.loads(content)

    except Exception:
        return {
            "investment_opinion": "HOLD",
            "confidence_score": 50,
            "summary": "AI 분석 실패",
            "risk_factors": "",
            "positive_factors": "",
            "ai_signal": "NEUTRAL"
        }