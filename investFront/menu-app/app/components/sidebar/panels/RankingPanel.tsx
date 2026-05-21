"use client";

import { useState } from "react";
import styles from "../MainSidebar.module.css";
import SidebarUserItem from "../components/SidebarUserItem";
import useRankingStocks from "../hooks/useRankingStocks";

type RankingSortKey =
  | "total"
  | "oneday";

const SORT_OPTIONS: {
  key: RankingSortKey;
  label: string;
}[] = [
  {
    key: "total",
    label: "총합",
  },
  {
    key: "oneday",
    label: "하루",
  },
];

export default function RankingPanel() {

  const [sortKey, setSortKey] = useState<RankingSortKey>("total");

  const {
    loading,
    rankings,
  } = useRankingStocks(sortKey);

  if (loading) {
    return (
      <div className={styles.panelContent}>
        로딩중...
      </div>
    );
  }

  return (
    <div className={styles.panelContent}>

      <div className={styles.rankingTab}>
        {SORT_OPTIONS.map(option => (
          <button
            key={option.key}
            type="button"
            onClick={() =>
              setSortKey(option.key)
            }
            className={
              sortKey === option.key
                ? styles.activeTab
                : styles.tabButton
            }
          >
            {option.label}
          </button>
        ))}
      </div>

      <div className={styles.sectionTitle}>
        <h3>
          {sortKey === "total"
            ? "총 수익률 랭킹"
            : "하루 수익률 랭킹"}
        </h3>

        <p>
          {sortKey === "total"
            ? "시드머니 기준"
            : "전일 기준"}
        </p>
      </div>

      {rankings.length === 0 ? (
        <div>
          데이터가 없습니다.
        </div>
      ) : (
        rankings?.map((item, index) => (
          <SidebarUserItem
            key={item.userNo}
            rank={index + 1}
            userName={item.userName}
            profitRate={item.profitRate}
            evaluationAmount={
              item.evaluationAmount
            }
          />
        ))
      )}
    </div>
  );
}