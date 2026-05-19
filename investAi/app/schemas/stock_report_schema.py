from pydantic import BaseModel
from typing import List


class StockReportRequest(BaseModel):

    stock_name: str
    sector: str
    market_type: str

    issued_stock: int
    declined_stock: int
    treasury_stock: int
    outstanding_shares: int

    minority_shareholder_ratio: float
    minority_ownership_ratio: float

    recent_news: List[str]