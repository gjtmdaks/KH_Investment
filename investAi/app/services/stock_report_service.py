from app.clients.openai_client import request_chat_completion
from app.prompts.stock_report_prompt import build_stock_report_prompt
from app.utils.json_parser import parse_json_response

def generate_stock_report(data):

    prompt = build_stock_report_prompt(data)

    raw_response = request_chat_completion(prompt)

    parsed = parse_json_response(raw_response)

    return parsed