from typing import Optional
from pydantic import BaseModel

class StockReportRequest(BaseModel):
    stock_name: str
    sector: str
    market_type: str

    issued_stock: Optional[int] = None
    declined_stock: Optional[int] = None
    treasury_stock: Optional[int] = None
    outstanding_shares: Optional[int] = None

    minority_shareholder_ratio: Optional[float] = None
    minority_ownership_ratio: Optional[float] = None

    recent_news: list[str]


class StockReportResponse(BaseModel):

    investment_opinion: str
    confidence_score: float

    summary: str

    risk_factors: str
    positive_factors: str

    ai_signal: str