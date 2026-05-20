import os

from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

client = OpenAI(
    api_key=os.getenv("OPENAI_API_KEY")
)


def request_chat_completion(prompt: str):

    response = client.chat.completions.create(
        model="gpt-5-mini",

        response_format={
            "type": "json_object"
        },

        messages=[
            {
                "role": "system",
                "content": (
                    "너는 한국 주식시장 전문 애널리스트다."
                )
            },
            {
                "role": "user",
                "content": prompt
            }
        ]
    )

    return response.choices[0].message.content