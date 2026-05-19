from transformers import pipeline

sentiment_pipeline = pipeline(
    "text-classification",
    model="snunlp/KR-FinBert-SC"
)

POSITIVE_KEYWORDS = [
    "상승",
    "급등",
    "호재",
    "수혜",
    "실적 개선",
    "흑자",
    "매수",
    "최고가",
]

NEGATIVE_KEYWORDS = [
    "하락",
    "급락",
    "악재",
    "적자",
    "우려",
    "전쟁",
    "긴장",
    "매도",
]

def analyze_sentiment(text: str):

    # 1. 룰 기반 우선
    for keyword in POSITIVE_KEYWORDS:
        if keyword in text:
            return {
                "sentiment": "POSITIVE",
                "score": 0.9
            }

    for keyword in NEGATIVE_KEYWORDS:
        if keyword in text:
            return {
                "sentiment": "NEGATIVE",
                "score": 0.9
            }

    # 2. AI fallback
    result = sentiment_pipeline(text)[0]

    label = result["label"].lower()

    if label == "positive":
        sentiment = "POSITIVE"

    elif label == "negative":
        sentiment = "NEGATIVE"

    else:
        sentiment = "NEUTRAL"

    return {
        "sentiment": sentiment,
        "score": round(float(result["score"]), 4)
    }