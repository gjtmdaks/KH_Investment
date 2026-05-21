from fastapi import APIRouter

from app.schemas.user_profile_schema import (
    UserProfileRequest,
    UserProfileResponse
)

from app.services.user_profile_service import (
    generate_user_profile_analysis
)

router = APIRouter()


@router.post(
    "/user-profile",
    response_model=UserProfileResponse
)
async def analyze_user_profile(
    request: UserProfileRequest
):

    result = generate_user_profile_analysis(request)

    return result