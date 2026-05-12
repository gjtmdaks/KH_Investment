import { NextResponse } from "next/server";

import { resolveKisBaseUrl } from "@/lib/kis-config";

type KisTokenResponse = {
  access_token?: string;
  token_type?: string;
  expires_in?: number;
  access_token_token_expired?: string;
  error_code?: string;
  error_description?: string;
};

export async function GET() {
  const appKey = process.env.KIS_APP_KEY;
  const appSecret = process.env.KIS_APP_SECRET;
  const baseUrl = resolveKisBaseUrl();

  if (!appKey || !appSecret) {
    return NextResponse.json(
      {
        message: "KIS_APP_KEY 또는 KIS_APP_SECRET이 없습니다.",
      },
      { status: 500 }
    );
  }

  const response = await fetch(`${baseUrl}/oauth2/tokenP`, {
    method: "POST",
    headers: {
      "content-type": "application/json; charset=utf-8",
    },
    body: JSON.stringify({
      grant_type: "client_credentials",
      appkey: appKey,
      appsecret: appSecret,
    }),
    cache: "no-store",
  });

  const data = (await response.json()) as KisTokenResponse;

  if (!response.ok || !data.access_token) {
    return NextResponse.json(
      {
        message: "토큰 발급 실패",
        status: response.status,
        data,
      },
      { status: response.status || 500 }
    );
  }

  return NextResponse.json({
    accessToken: data.access_token,
    tokenType: data.token_type,
    expiresIn: data.expires_in,
    expiredAt: data.access_token_token_expired,
  });
}