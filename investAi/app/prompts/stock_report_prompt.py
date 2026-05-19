def build_stock_report_prompt(data):

    news_text = "\n".join(
        [
            f"{idx + 1}. {news}"
            for idx, news in enumerate(data.recent_news)
        ]
    )

    return f"""
너는 한국 주식 애널리스트다.

아래 종목 데이터를 분석해라.

종목명: {data.stock_name}
업종: {data.sector}
시장: {data.market_type}

발행한 총 주식: {data.issued_stock}
감소한 총 주식: {data.declined_stock}
자기주식수: {data.treasury_stock}
유통주식수: {data.outstanding_shares}

주주 비율: {data.minority_shareholder_ratio}
보유 주식 비율: {data.minority_ownership_ratio}

최근 뉴스:
{news_text}

반드시 아래 JSON 형식으로만 응답해라.

{{
  "investment_opinion": "BUY/HOLD/SELL",
  "confidence_score": 0~100 숫자,
  "summary": "요약",
  "risk_factors": "리스크",
  "positive_factors": "긍정 요소",
  "ai_signal": "POSITIVE/NEUTRAL/NEGATIVE"
}}
"""