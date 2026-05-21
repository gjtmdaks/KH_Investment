"use client";

import {
  formatInvestorTradeDate,
  formatSignedKoreanLargeShares,
  formatSignedNumber,
} from "@/lib/stock/stockDetailFormat";
import type {
  InvestorTrendDailyRow,
  InvestorTrendResponse,
  InvestorTrendSummary,
} from "@/lib/stock/stockDetailTypes";

import styles from "./css/stockDetailInvestorTrendPanel.module.css";
import { StockDetailEmptyState } from "./StockDetailEmptyState";

type InvestorGroup = {
  key: "individual" | "foreign" | "institution";
  label: string;
  value: number | null;
};

function getQtyToneClass(value: number | null) {
  if (value === null || value === 0) {
    return styles.cellNeutral;
  }

  return value > 0 ? styles.cellUp : styles.cellDown;
}

function getLabelToneClass(value: number | null) {
  if (value === null || value === 0) {
    return "";
  }

  return value > 0 ? styles.summaryLabelUp : styles.summaryLabelDown;
}

function buildGroups(source: InvestorTrendSummary | InvestorTrendDailyRow): InvestorGroup[] {
  return [
    {
      key: "individual",
      label: "개인",
      value: source.individualNetQty,
    },
    {
      key: "foreign",
      label: "외국인",
      value: source.foreignNetQty,
    },
    {
      key: "institution",
      label: "기관",
      value: source.institutionNetQty,
    },
  ];
}

function calcBarWidthPercent(value: number | null, maxAbs: number) {
  if (value === null || maxAbs <= 0 || value === 0) {
    return 0;
  }

  return Math.min(50, (Math.abs(value) / maxAbs) * 50);
}

function SummaryBar({ group, maxAbs }: { group: InvestorGroup; maxAbs: number }) {
  const widthPercent = calcBarWidthPercent(group.value, maxAbs);
  const isPositive = (group.value ?? 0) > 0;
  const isNegative = (group.value ?? 0) < 0;

  return (
    <div className={styles.summaryRow}>
      <span className={`${styles.summaryLabel} ${getLabelToneClass(group.value)}`}>
        {group.label} {formatSignedKoreanLargeShares(group.value)}
      </span>
      <div className={styles.barTrack} aria-hidden>
        <span className={styles.barCenter} />
        {isNegative ? (
          <span
            className={styles.barFillLeft}
            style={{ width: `${widthPercent}%` }}
          />
        ) : null}
        {isPositive ? (
          <span
            className={styles.barFillRight}
            style={{ width: `${widthPercent}%` }}
          />
        ) : null}
      </div>
    </div>
  );
}

export function StockDetailInvestorTrendPanel({
  data,
}: {
  data: InvestorTrendResponse | null;
}) {
  if (!data?.rows?.length) {
    return <StockDetailEmptyState title="매매동향 데이터가 없습니다." />;
  }

  const summarySource = data.summary ?? data.rows[0];
  const groups = buildGroups(summarySource);
  const maxAbs = Math.max(
    ...groups.map((group) => Math.abs(group.value ?? 0)),
    1
  );

  return (
    <div className={styles.panel}>
      <section className={styles.summarySection} aria-label="투자자별 순매수 요약">
        {groups.map((group) => (
          <SummaryBar key={group.key} group={group} maxAbs={maxAbs} />
        ))}
      </section>

      <section className={styles.tableSection} aria-label="일별 투자자 매매동향">
        <div className={styles.tableHeader}>
          <span className={styles.tableHeaderDate}>일자</span>
          <span className={styles.tableHeaderCell}>개인</span>
          <span className={styles.tableHeaderCell}>외국인</span>
          <span className={styles.tableHeaderCell}>기관</span>
        </div>

        <div className={styles.tableBody}>
          {data.rows.map((row) => (
            <div key={row.tradeDate} className={styles.tableRow}>
              <span className={styles.tableDate}>
                {formatInvestorTradeDate(row.tradeDate)}
              </span>
              <span
                className={`${styles.tableCell} ${getQtyToneClass(row.individualNetQty)}`}
              >
                {formatSignedNumber(row.individualNetQty)}
              </span>
              <span
                className={`${styles.tableCell} ${getQtyToneClass(row.foreignNetQty)}`}
              >
                {formatSignedNumber(row.foreignNetQty)}
              </span>
              <span
                className={`${styles.tableCell} ${getQtyToneClass(row.institutionNetQty)}`}
              >
                {formatSignedNumber(row.institutionNetQty)}
              </span>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
