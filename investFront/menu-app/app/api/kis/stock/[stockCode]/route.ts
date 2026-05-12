import { NextResponse } from "next/server";

import { resolveKisBaseUrl } from "@/lib/kis-config";

type KisStockPriceResponse = {
  rt_cd: string;
  msg_cd: string;
  msg1: string;
  output?: {
    stck_prpr?: string; // 현재가
    prdy_vrss?: string; // 전일 대비
    prdy_vrss_sign?: string; // 전일 대비 부호
    prdy_ctrt?: string; // 전일 대비율
    acml_vol?: string; // 누적 거래량
    acml_tr_pbmn?: string; // 누적 거래 대금
    hts_kor_isnm?: string; // 종목명
    stck_oprc?: string; // 시가
    stck_hgpr?: string; // 고가
    stck_lwpr?: string; // 저가
  };
};

export async function GET(
  request: Request,
  { params }: { params: Promise<{ stockCode: string }> }
) {
  const { stockCode } = await params;

  const appKey = process.env.KIS_APP_KEY;
  const appSecret = process.env.KIS_APP_SECRET;
  const accessToken = process.env.KIS_ACCESS_TOKEN;
  const baseUrl = resolveKisBaseUrl();

  if (!appKey || !appSecret || !accessToken) {
    return NextResponse.json(
      {
        message: "KIS 환경변수가 설정되지 않았습니다.",
      },
      { status: 500 }
    );
  }

  const url = new URL(
    "/uapi/domestic-stock/v1/quotations/inquire-price",
    baseUrl
  );

  url.searchParams.set("FID_COND_MRKT_DIV_CODE", "J");
  url.searchParams.set("FID_INPUT_ISCD", stockCode);

  const response = await fetch(url, {
    method: "GET",
    headers: {
      "content-type": "application/json; charset=utf-8",
      authorization: `Bearer ${accessToken}`,
      appkey: appKey,
      appsecret: appSecret,
      tr_id: "FHKST01010100",
    },
    cache: "no-store",
  });

  const data = (await response.json()) as KisStockPriceResponse;

  if (!response.ok || data.rt_cd !== "0") {
    return NextResponse.json(
      {
        message: "한국투자증권 API 호출 실패",
        status: response.status,
        kisMessage: data.msg1,
        raw: data,
      },
      { status: response.status || 500 }
    );
  }

  return NextResponse.json({
    stockCode,
    stockName: data.output?.hts_kor_isnm,
    currentPrice: data.output?.stck_prpr,
    changePrice: data.output?.prdy_vrss,
    changeRate: data.output?.prdy_ctrt,
    volume: data.output?.acml_vol,
    tradingValue: data.output?.acml_tr_pbmn,
    openPrice: data.output?.stck_oprc,
    highPrice: data.output?.stck_hgpr,
    lowPrice: data.output?.stck_lwpr,
    raw: data.output,
  });
}