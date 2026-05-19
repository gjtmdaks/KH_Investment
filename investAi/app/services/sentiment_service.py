from transformers import pipeline

sentiment_pipeline = pipeline(
    "text-classification",
    model="snunlp/KR-FinBert-SC"
)

def analyze_sentiment(text: str):

    result = sentiment_pipeline(text)[0]

    label = result["label"]
    score = float(result["score"])

    return {
        "label": label,
        "score": round(score, 4)
    }