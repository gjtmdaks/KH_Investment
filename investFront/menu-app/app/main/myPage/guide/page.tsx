import layoutStyles from "../myPage.module.css";
import styles from "./guide.module.css"; // 새로 만든 스타일
import MyPageSidebar from "../components/MyPageSidebar";

export default function Guide() {
  const steps = [
    { id: 1, title: "종목 검색하기", desc: "상단 검색창에 원하는 종목명을 입력해보세요." },
    { id: 2, title: "주문하기 (매수/매도)", desc: "수량과 주문유형을 설정하고 주문 버튼을 눌러주세요." },
    { id: 3, title: "결과 확인하기", desc: "내 정보에서 보유 종목과 수익률을 확인하세요." },
  ];

  const features = [
    { title: "종목 검색", desc: "실시간 시세 확인", icon: "🔍" },
    { title: "주문", desc: "간편한 매수/매도", icon: "⚖️" },
    { title: "내 정보", desc: "자산 현황 및 내역", icon: "👤" },
    { title: "차트 분석", desc: "다양한 지표 제공", icon: "📊" },
    { title: "랭킹", desc: "수익률 순위 확인", icon: "🏆" },
  ];

  return (
    <main className={layoutStyles.page}>
      <MyPageSidebar />

      <section className={layoutStyles.content}>
        <div className={layoutStyles.pageTitleArea}>
          <p className={layoutStyles.pageLabel}>INVEST GUIDE</p>
          <h1 className={layoutStyles.pageTitle}>투자 가이드</h1>
        </div>

        <div className={styles.guideContainer}>
          {/* 빠른 시작 가이드 영역 */}
          <div>
            <h3 style={{ marginBottom: "15px" }}>빠른 시작 가이드 (3단계로 시작하기)</h3>
            <div className={styles.stepWrapper}>
              {steps.map((step) => (
                <div key={step.id} className={styles.stepCard}>
                  <div className={styles.stepBadge}>{step.id}</div>
                  <h4 className={styles.stepTitle}>{step.title}</h4>
                  <div className={styles.stepImagePlaceholder}>
                    이미지 영역 (UI 스크린샷)
                  </div>
                  <p style={{ marginTop: "10px", fontSize: "14px", color: "#555" }}>{step.desc}</p>
                </div>
              ))}
            </div>
          </div>

          {/* 주요 기능 요약 영역 */}
          <div>
            <h3 style={{ marginBottom: "15px" }}>주요 기능 한눈에 보기</h3>
            <div className={styles.featureGrid}>
              {features.map((f, idx) => (
                <div key={idx} className={styles.featureCard}>
                  <div className={styles.featureIcon}>{f.icon}</div>
                  <div className={styles.featureTitle}>{f.title}</div>
                  <p className={styles.featureDesc}>{f.desc}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}