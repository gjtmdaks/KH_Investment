"use client";

import {
  formatNumber,
  formatPlainPercent,
  formatWon,
} from "@/lib/stock/stockDetailFormat";
import type {
  PriceResponse,
  StaticProfileResponse,
} from "@/lib/stock/stockDetailTypes";

import styles from "./stockDetail.module.css";
import { StockDetailEmptyState } from "./StockDetailEmptyState";
import { StockDetailStat } from "./StockDetailStat";

export function StockDetailSummaryPanel({
  profile,
  price,
}: {
  profile: StaticProfileResponse | null;
  price: PriceResponse | null;
}) {
  if (!profile && !price) {
    return <StockDetailEmptyState title="종목 요약 정보가 없습니다." />;
  }

  return (
    <div className={styles.summaryGrid}>
      <StockDetailStat label="시장" value={profile?.marketType || "-"} />
      <StockDetailStat label="상장일" value={profile?.listedDate || "-"} />
      <StockDetailStat label="업종" value={profile?.sector || "-"} />
      <StockDetailStat label="회사명" value={profile?.coName || "-"} />
      <StockDetailStat label="공시 코드" value={profile?.corpCode || "-"} />
      <StockDetailStat label="상태" value={profile?.status || "-"} />
      <StockDetailStat
        label="발행주식수"
        value={formatNumber(
          profile?.issuedStock === null || profile?.issuedStock === undefined
            ? null
            : String(profile.issuedStock)
        )}
      />
      <StockDetailStat
        label="유통주식수"
        value={formatNumber(
          profile?.outstandingShares === null || profile?.outstandingShares === undefined
            ? null
            : String(profile.outstandingShares)
        )}
      />
      <StockDetailStat
        label="자기주식수"
        value={formatNumber(
          profile?.treasuryStock === null || profile?.treasuryStock === undefined
            ? null
            : String(profile.treasuryStock)
        )}
      />
      <StockDetailStat
        label="소액주주 지분율"
        value={formatPlainPercent(profile?.shareholdingRatio)}
      />
      <StockDetailStat
        label="소액주주 소유율"
        value={formatPlainPercent(profile?.ownershipPercentage)}
      />
      <StockDetailStat label="시가" value={formatWon(price?.openPrice)} />
      <StockDetailStat
        label="고가 / 저가"
        value={`${formatWon(price?.highPrice)} / ${formatWon(price?.lowPrice)}`}
      />
    </div>
  );
}
