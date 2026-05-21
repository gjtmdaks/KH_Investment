import json

from app.clients.openai_client import request_chat_completion
from app.prompts.user_profile_prompt import build_user_profile_prompt


def generate_user_profile_analysis(request):

    prompt = build_user_profile_prompt(
        request.model_dump()
    )

    raw_response = request_chat_completion(prompt)

    return json.loads(raw_response)