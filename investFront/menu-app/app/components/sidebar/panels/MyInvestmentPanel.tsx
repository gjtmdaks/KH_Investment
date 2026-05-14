import styles from "../MainSidebar.module.css";
import SidebarEmpty from "../components/SidebarEmpty";

export default function MyInvestmentPanel({
  data,
  isLogin,
}: any) {

  if (!isLogin) {
    return (
      <SidebarEmpty
        text="로그인이 필요해요"
      />
    );
  }

  const account = data.sidebar?.account;
  const holdings = data.sidebar?.holdings || [];

  return (
    <div className={styles.panelContent}>
      <div>
        잔액 : {account?.balance ?? 0}
      </div>

      <h4>보유 주식</h4>
      {holdings.length === 0 ? (
        <div>보유 주식 없음</div>
      ) : (
        holdings.map((h: any) => (
          <div key={h.stockCode}>
            {h.stockName}
          </div>
        ))
      )}
    </div>
  );
}