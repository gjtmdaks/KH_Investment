from transformers import pipeline

pipe = pipeline(
    "text-classification",
    model="snunlp/KR-FinBert-SC"
)

result = pipe(
    "삼성전자가 AI 반도체 수혜 기대감으로 상승했다."
)

print(result)