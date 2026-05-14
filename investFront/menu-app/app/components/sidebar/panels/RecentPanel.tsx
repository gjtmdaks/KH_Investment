import styles from "../MainSidebar.module.css";

export default function RecentPanel({
  data,
}: any) {

  const list = data?.sidebar?.recentView || [];

  return (
    <div className={styles.panelContent}>
      {list.length === 0 ? (
        <div>
          최근 본 종목 없음
        </div>
      ) : (
        list.map((s: any) => (
          <div key={s.stockCode}>
            {s.stockName}
          </div>
        ))
      )}
    </div>
  );
}