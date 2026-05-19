from fastapi import APIRouter
from pydantic import BaseModel

from app.services.sentiment_service import analyze_sentiment
from app.services.stock_report_service import generate_stock_report
from app.schemas.stock_report_schema import StockReportRequest

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

@router.post("/analysis/stock-report")
async def analyze_stock_report(request: StockReportRequest):
    result = generate_stock_report(request)

    return result