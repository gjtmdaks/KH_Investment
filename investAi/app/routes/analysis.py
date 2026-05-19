from fastapi import APIRouter
from pydantic import BaseModel

from app.services.sentiment_service import analyze_sentiment

router = APIRouter()

class NewsRequest(BaseModel):
    text: str

@router.post("/sentiment")
def sentiment(request: NewsRequest):

    result = analyze_sentiment(request.text)

    return result