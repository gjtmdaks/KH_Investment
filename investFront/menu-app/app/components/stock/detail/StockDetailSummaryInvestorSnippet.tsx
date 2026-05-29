"use client";

import {
  formatInvestorTradeDate,
  formatSignedKoreanLargeShares,
} from "@/lib/stock/stockDetailFormat";
import { isInvestorTradeDateToday } from "@/lib/stock/stockDetailInvestorTrend";
import type {
  InvestorTrendResponse,
  InvestorTrendSummary,
} from "@/lib/stock/stockDetailTypes";

import styles from "./css/stockDetailSummaryPanel.module.css";

type InvestorGroup = {
  key: string;
  label: string;
  value: number | null;
};

function buildGroups(source: InvestorTrendSummary): InvestorGroup[] {
  return [
    { key: "individual", label: "개인", value: source.individualNetQty },
    { key: "foreign", label: "외국인", value: source.foreignNetQty },
    { key: "institution", label: "기관", value: source.institutionNetQty },
  ];
}

function calcBarWidthPercent(value: number | null, maxAbs: number) {
  if (value === null || maxAbs <= 0 || value === 0) {
    return 0;
  }

  return Math.min(50, (Math.abs(value) / maxAbs) * 50);
}

function getLabelToneClass(value: number | null) {
  if (value === null || value === 0) {
    return "";
  }

  return value > 0 ? styles.investorLabelUp : styles.investorLabelDown;
}

function MiniBar({ group, maxAbs }: { group: InvestorGroup; maxAbs: number }) {
  const widthPercent = calcBarWidthPercent(group.value, maxAbs);
  const isPositive = (group.value ?? 0) > 0;
  const isNegative = (group.value ?? 0) < 0;

  return (
    <div className={styles.investorRow}>
      <span className={`${styles.investorLabel} ${getLabelToneClass(group.value)}`}>
        {group.label} {formatSignedKoreanLargeShares(group.value)}
      </span>
      <div className={styles.investorBarTrack} aria-hidden>
        <span className={styles.investorBarCenter} />
        {isNegative ? (
          <span
            className={styles.investorBarFillLeft}
            style={{ width: `${widthPercent}%` }}
          />
        ) : null}
        {isPositive ? (
          <span
            className={styles.investorBarFillRight}
            style={{ width: `${widthPercent}%` }}
          />
        ) : null}
      </div>
    </div>
  );
}

export function StockDetailSummaryInvestorSnippet({
  data,
  loading,
  onOpenInvestorTab,
}: {
  data: InvestorTrendResponse | null;
  loading: boolean;
  onOpenInvestorTab: () => void;
}) {
  if (loading) {
    return <p className={styles.sectionHint}>매매동향을 불러오는 중입니다.</p>;
  }

  const summarySource = data?.summary ?? data?.rows?.[0];

  if (!summarySource) {
    return (
      <>
        <p className={styles.sectionHint}>
          {data?.notice ?? "매매동향 데이터가 없습니다."}
        </p>
        <button type="button" className={styles.investorLink} onClick={onOpenInvestorTab}>
          개인·외국인·기관 탭 열기
        </button>
      </>
    );
  }

  const groups = buildGroups(summarySource);
  const maxAbs = Math.max(
    ...groups.map((group) => Math.abs(group.value ?? 0)),
    1
  );
  const tradeDateLabel = isInvestorTradeDateToday(summarySource.tradeDate)
    ? "오늘"
    : formatInvestorTradeDate(summarySource.tradeDate);

  return (
    <>
      <p className={styles.investorDateMeta}>
        기준 일자 <strong>{tradeDateLabel}</strong>
        {data?.notice ? <span className={styles.investorNotice}>{data.notice}</span> : null}
      </p>
      <div className={styles.investorBars} aria-label="투자자별 순매수 요약">
        {groups.map((group) => (
          <MiniBar key={group.key} group={group} maxAbs={maxAbs} />
        ))}
      </div>
      <button type="button" className={styles.investorLink} onClick={onOpenInvestorTab}>
        개인·외국인·기관 더 보기
      </button>
    </>
  );
}
