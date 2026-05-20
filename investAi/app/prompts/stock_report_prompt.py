def build_stock_report_prompt(request):

    news_text = "\n".join(
        [
            f"{idx + 1}. {news}"
            for idx, news in enumerate(request.recent_news)
        ]
    )

    return f"""
너는 한국 주식시장 전문 애널리스트다.

반드시 객관적으로 분석해라.
과장된 표현을 금지한다.
투자 권유가 아닌 참고용 분석만 제공한다.

[종목 정보]

종목명: {request.stock_name}
업종: {request.sector}
시장: {request.market_type}

발행한 총 주식: {request.issued_stock or '정보 없음'}
감소한 총 주식: {request.declined_stock or '정보 없음'}
자기주식수: {request.treasury_stock or '정보 없음'}
유통주식수: {request.outstanding_shares or '정보 없음'}

주주 비율: {request.minority_shareholder_ratio or '정보 없음'}
보유 주식 비율: {request.minority_ownership_ratio or '정보 없음'}

[최근 뉴스]
{news_text}

반드시 아래 JSON 형식만 반환해라.

{{
  "investment_opinion": "BUY 또는 HOLD 또는 SELL",
  "confidence_score": 0~100 숫자,
  "summary": "3줄 요약",
  "risk_factors": "리스크 요약",
  "positive_factors": "긍정 요약",
  "ai_signal": "POSITIVE 또는 NEUTRAL 또는 NEGATIVE"
}}
"""