from fastapi import FastAPI

from app.routes.analysis import router as analysis_router
from app.routes.user_profile import router as user_profile_router

app = FastAPI()

app.include_router(
    analysis_router,
    prefix="/analysis",
    tags=["analysis"]
)

app.include_router(
    user_profile_router,
    prefix="/analysis",
    tags=["user-profile"]
)

@app.get("/")
def home():
    return {
        "message": "AI Server Running"
    }