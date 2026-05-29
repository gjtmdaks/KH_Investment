"use client";

import type { ReactNode } from "react";

import {
  formatNumber,
  formatWon,
} from "@/lib/stock/stockDetailFormat";
import type {
  InvestorTrendResponse,
  PriceResponse,
  StaticProfileResponse,
} from "@/lib/stock/stockDetailTypes";
import {
  buildDartCorpUrl,
  calcShareRatio,
  formatDayPriceRange,
  formatListingDuration,
  hasAnyStockStructureField,
  parsePercentValue,
} from "@/lib/stock/stockDetailSummaryMetrics";

import styles from "./css/stockDetailSummaryPanel.module.css";
import { StockDetailEmptyState } from "./StockDetailEmptyState";
import { StockDetailStat } from "./StockDetailStat";
import { StockDetailSummaryInvestorSnippet } from "./StockDetailSummaryInvestorSnippet";

function SummarySection({
  title,
  hint,
  children,
}: {
  title: string;
  hint?: string;
  children: ReactNode;
}) {
  return (
    <section className={styles.section}>
      <header className={styles.sectionHeader}>
        <h3>{title}</h3>
        {hint ? <p className={styles.sectionHint}>{hint}</p> : null}
      </header>
      {children}
    </section>
  );
}

function RatioMeter({
  label,
  percent,
}: {
  label: string;
  percent: number | null;
}) {
  const width =
    percent === null ? 0 : Math.min(100, Math.max(0, percent));

  return (
    <div className={styles.ratioRow}>
      <div className={styles.ratioHead}>
        <span>{label}</span>
        <strong>{percent === null ? "-" : `${percent.toFixed(2)}%`}</strong>
      </div>
      <div className={styles.ratioTrack} aria-hidden>
        <span className={styles.ratioFill} style={{ width: `${width}%` }} />
      </div>
    </div>
  );
}

function formatProfileNumber(value?: string | null) {
  if (value === null || value === undefined) {
    return null;
  }

  return String(value);
}

export function StockDetailSummaryPanel({
  profile,
  price,
  investorTrend,
  investorLoading,
  onOpenInvestorTab,
}: {
  profile: StaticProfileResponse | null;
  price: PriceResponse | null;
  investorTrend: InvestorTrendResponse | null;
  investorLoading: boolean;
  onOpenInvestorTab: () => void;
}) {
  if (!profile && !price) {
    return <StockDetailEmptyState title="종목 요약 정보가 없습니다." />;
  }

  const dartUrl = buildDartCorpUrl(profile?.corpCode);
  const circulationRatio = calcShareRatio(
    profile?.outstandingShares,
    profile?.issuedStock
  );
  const treasuryRatio = calcShareRatio(profile?.treasuryStock, profile?.issuedStock);
  const minorityStake = parsePercentValue(profile?.shareholdingRatio);
  const minorityOwnership = parsePercentValue(profile?.ownershipPercentage);
  const listingDuration = formatListingDuration(profile?.listedDate);
  const dayRange = formatDayPriceRange(
    price?.openPrice,
    price?.highPrice,
    price?.lowPrice
  );
  const hasStockStructure = hasAnyStockStructureField(profile);
  const hasMinorityData =
    minorityStake !== null || minorityOwnership !== null;

  return (
    <div className={styles.panel}>
      <SummarySection title="기업 개요">
        <div className={styles.summaryGrid}>
          <StockDetailStat label="상장일" value={profile?.listedDate || "-"} />
          <StockDetailStat label="상장 기간" value={listingDuration} />
          <StockDetailStat label="업종" value={profile?.sector || "-"} />
          <StockDetailStat label="회사명" value={profile?.coName || "-"} />
        </div>
        <div className={styles.corpCodeRow}>
          <StockDetailStat
            className={styles.corpCodeStat}
            label="공시 코드"
            value={profile?.corpCode || "-"}
          />
          {dartUrl ? (
            <a
              className={styles.dartLink}
              href={dartUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              DART 전자공시 열기
            </a>
          ) : null}
        </div>
      </SummarySection>

      <SummarySection
        title="주식 구조"
        hint={
          hasStockStructure
            ? undefined
            : "Open DART 동기화 후 발행·유통 주식 수 등이 표시됩니다."
        }
      >
        <div className={styles.summaryGrid}>
          <StockDetailStat
            label="발행주식수"
            value={formatNumber(formatProfileNumber(profile?.issuedStock))}
          />
          <StockDetailStat
            label="유통주식수"
            value={formatNumber(formatProfileNumber(profile?.outstandingShares))}
          />
          <StockDetailStat
            label="자기주식수"
            value={formatNumber(formatProfileNumber(profile?.treasuryStock))}
          />
          <StockDetailStat
            label="감소주식수"
            value={formatNumber(formatProfileNumber(profile?.declinedStock))}
          />
          <StockDetailStat
            label="유통비율"
            value={
              circulationRatio === null ? "-" : `${circulationRatio.toFixed(2)}%`
            }
          />
          <StockDetailStat
            label="자기주식 비율"
            value={treasuryRatio === null ? "-" : `${treasuryRatio.toFixed(2)}%`}
          />
        </div>
      </SummarySection>

      <SummarySection
        title="소액주주"
        hint={
          hasMinorityData
            ? undefined
            : "Open DART 소액주주 동기화 후 지분율·소유율이 표시됩니다."
        }
      >
        <div className={styles.ratioList}>
          <RatioMeter label="소액주주 지분율" percent={minorityStake} />
          <RatioMeter label="소액주주 소유율" percent={minorityOwnership} />
        </div>
      </SummarySection>

      <SummarySection title="당일 가격">
        <div className={styles.summaryGrid}>
          <StockDetailStat label="시가" value={formatWon(price?.openPrice)} />
          <StockDetailStat label="고가" value={formatWon(price?.highPrice)} />
          <StockDetailStat label="저가" value={formatWon(price?.lowPrice)} />
          <StockDetailStat label="당일 변동폭" value={dayRange} />
        </div>
      </SummarySection>

      <SummarySection title="매매동향 요약">
        <StockDetailSummaryInvestorSnippet
          data={investorTrend}
          loading={investorLoading}
          onOpenInvestorTab={onOpenInvestorTab}
        />
      </SummarySection>
    </div>
  );
}
