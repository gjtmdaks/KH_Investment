import layoutStyles from "../myPage.module.css";
import styles from "./guide.module.css";
import MyPageSidebar from "../components/MyPageSidebar";

function MobileUIScreen({ children }: { children: any }) {
  return (
    <div className={styles.mobileUIScreen}>
      <div className={styles.mobileUIHeader}><div className={styles.mobileUIStatusBar} />{children}</div>
      <div className={styles.mobileUIBody}></div>
    </div>
  );
}
function Step1UI() {
  return (
    <MobileUIScreen>
      <div className={styles.ui_SearchBar}><span style={{color: '#888'}}>🔍</span><span style={{color: '#bbb'}}>삼성전자</span></div>
      <div className={styles.ui_StockItem}>
        <div><div style={{fontWeight: 'bold'}}>삼성전자</div><div style={{fontSize: '11px', color: '#888'}}>005930</div></div>
        <div><div>72,500</div><div style={{fontSize: '11px', color: '#f04e4e'}}>+1.25%</div></div>
      </div>
    </MobileUIScreen>
  );
}
function Step2UI() {
  return (
    <MobileUIScreen>
      <div style={{padding: '5px 10px', fontWeight: 'bold'}}>매수 주문</div>
      <div style={{borderBottom: '1px solid #ddd', margin: '5px 0'}}/>
      <div className={styles.ui_OrderItem}><span>주문유형</span><div className={styles.ui_Dropdown}>시장가</div></div>
      <div className={styles.ui_OrderItem}><span>수량</span><div className={styles.ui_Input}>10</div></div>
      <div className={styles.ui_OrderButton}>매수 주문하기</div>
    </MobileUIScreen>
  );
}
function Step3UI() {
  return (
    <MobileUIScreen>
      <div style={{padding: '5px 10px', fontWeight: 'bold'}}>내 자산 현황</div>
      <div style={{background: '#f1f6ff', padding: '10px', borderRadius: '4px', margin: '10px'}}><div style={{fontWeight: 'bold', fontSize: '15px'}}>10,250,000 원</div></div>
      <div className={styles.ui_StockItem}><span>삼성전자 (10주)</span><span style={{color: '#f04e4e'}}>+10.5%</span></div>
    </MobileUIScreen>
  );
}

export default function Guide() {
  const steps = [
    { id: 1, title: "종목 검색하기", desc: "상단 검색창에 원하는 종목명을 입력해보세요. 예) 삼성전자", ui: <Step1UI /> },
    { id: 2, title: "주문하기 (매수 또는 매도)", desc: "매수/매도 선택 후 수량과 주문유형을 설정하고 주문 버튼을 눌러주세요.", ui: <Step2UI /> },
    { id: 3, title: "결과 확인하기", desc: "내 정보에서 보유 종목과 수익률을 확인 할 수 있어요.", ui: <Step3UI /> },
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
          <div className={styles.sectionWrapper}>
            <h3 className={styles.sectionTitle}>빠른 시작 가이드 (3단계로 시작하기)</h3>
            <div className={styles.stepWrapper}>
              
              {steps.map((step) => (
                <div key={step.id} className={styles.stepCard}>
                  <div className={styles.stepHeader}>
                    <div className={styles.stepBadge}>{step.id}</div>
                    <h4 className={styles.stepTitleText}>{step.title}</h4>
                  </div>
                  
                  <div className={styles.stepImagePlaceholder}>
                    {step.ui}
                  </div>
                  
                  <p className={styles.stepDescText}>{step.desc}</p>
                </div>
              ))}

            </div>
          </div>

          <div className={styles.sectionWrapper}>
            <h3 className={styles.sectionTitle}>주요 기능 한눈에 보기</h3>
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