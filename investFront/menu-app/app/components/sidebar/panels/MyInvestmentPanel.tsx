import styles from "../MainSidebar.module.css";
import SidebarEmpty from "../components/SidebarEmpty";
import { useRouter } from "next/navigation";
import type {
  MyInvestmentHolding,
  MyInvestmentSidebarData,
} from "../types";

type Props = {
  data: {
    sidebar?: Partial<MyInvestmentSidebarData>;
  };
  isLogin: boolean;
};

function formatWon(value?: number | null) {
  return `${(value ?? 0).toLocaleString()}원`;
}

function formatQuantity(value?: number | null) {
  return `${(value ?? 0).toLocaleString()}주`;
}

function formatSignedWon(value?: number | null) {
  const numberValue = value ?? 0;
  const sign = numberValue > 0 ? "+" : "";

  return `${sign}${numberValue.toLocaleString()}원`;
}

function formatRate(value?: number | null) {
  const numberValue = value ?? 0;
  const sign = numberValue > 0 ? "+" : "";

  return `${sign}${numberValue.toFixed(2)}%`;
}

function getProfitClass(value?: number | null) {
  const numberValue = value ?? 0;

  if (numberValue > 0) return styles.profitUp;
  if (numberValue < 0) return styles.profitDown;
  return styles.profitFlat;
}

export default function MyInvestmentPanel({ data, isLogin }: Props) {
  const router = useRouter();

  if (!isLogin) {
    return <SidebarEmpty text="로그인이 필요해요" />;
  }

  const account = data.sidebar?.account ?? null;
  const holdings: MyInvestmentHolding[] = data.sidebar?.holdings ?? [];

  return (
    <div className={styles.panelContent}>
      <section className={styles.investSummaryBox}>
        <div className={styles.investSummaryRow}>
          <span>가용 가능 금액</span>
          <strong>{formatWon(account?.availableCash)}</strong>
        </div>

        <div className={styles.investSummaryRow}>
          <span>내가 투자한 금액</span>
          <strong>{formatWon(account?.investedAmount)}</strong>
        </div>
      </section>

      <section className={styles.sidebarSection}>
        <div className={styles.sidebarSectionHeader}>
          <h4>보유 주식</h4>
          <span>{holdings.length.toLocaleString()}개</span>
        </div>

        {holdings.length === 0 ? (
          <SidebarEmpty text="보유 주식이 없어요" />
        ) : (
          <div className={styles.investHoldingList}>
            {holdings.map((holding) => (
              <article
                key={holding.stockCode}
                className={styles.investHoldingItem}
                role="button"
                tabIndex={0}
                onClick={() => router.push(`/main/stock/${holding.stockCode}`)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    router.push(`/main/stock/${holding.stockCode}`);
                  }
                }}
              >
                <div className={styles.investHoldingTop}>
                  <div>
                    <strong>{holding.stockName}</strong>
                    <span>{holding.stockCode}</span>
                  </div>

                  <b>{formatQuantity(holding.quantity)}</b>
                </div>

                <div className={styles.investHoldingValue}>
                  <span>평가금액</span>
                  <strong>{formatWon(holding.stockValue)}</strong>
                </div>

                <div className={styles.investHoldingProfit}>
                  <span className={getProfitClass(holding.profitAmount)}>
                    {formatSignedWon(holding.profitAmount)}
                  </span>

                  <span className={getProfitClass(holding.profitRate)}>
                    {formatRate(holding.profitRate)}
                  </span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}