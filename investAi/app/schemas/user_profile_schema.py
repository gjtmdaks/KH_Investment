from pydantic import BaseModel
from typing import List


class SurveyAnswer(BaseModel):
    question: str
    answer: str


class SectorAllocation(BaseModel):
    sector: str
    ratio: float


class MarketAllocation(BaseModel):
    KOSPI: float
    KOSDAQ: float


class PortfolioInfo(BaseModel):
    total_asset: int
    cash_ratio: float

    stock_count: int

    sector_allocation: List[SectorAllocation]

    market_allocation: MarketAllocation


class UserProfileRequest(BaseModel):
    survey_result: str

    survey_answers: List[SurveyAnswer]

    portfolio: PortfolioInfo


class UserProfileResponse(BaseModel):
    survey_type: str

    actual_investment_type: str

    portfolio_risk_analysis: List[str]

    ai_recommendations: List[str]