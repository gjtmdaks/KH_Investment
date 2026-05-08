import layoutStyles from "../myPage.module.css";
import MyPageSidebar from "../components/MyPageSidebar";

export default function Guide() {
  return (
    <main className={layoutStyles.page}>
      <MyPageSidebar />

      <section className={layoutStyles.content}>
        <div className={layoutStyles.pageTitleArea}>
          <p className={layoutStyles.pageLabel}>INVEST GUIDE</p>
          <h1 className={layoutStyles.pageTitle}>투자 가이드</h1>
        </div>

        <div>
          <p>투자 가이드 내용이 들어갈 자리</p>
        </div>
      </section>
    </main>
  );
}