from fastapi import APIRouter
from pydantic import BaseModel

from app.services.sentiment_service import analyze_sentiment

router = APIRouter()

class NewsRequest(BaseModel):
    title: str
    description: str | None = None

@router.post("/sentiment")
def sentiment(request: NewsRequest):

    full_text = request.title

    if request.description:
        full_text += " " + request.description

    result = analyze_sentiment(full_text)

    return result