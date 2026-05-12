"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";

import layoutStyles from "../../myPage.module.css";
import styles from "./investmentType.module.css";
import MyPageSidebar from "../../components/MyPageSidebar";

type Option = {
  optionNo: number;
  text: string;
  point: number;
};

type Question = {
  questionNo: number;
  questionText: string;
  options: Option[];
};

type Answer = {
  questionNo: number;
  questionText: string;
  optionNo: number;
  optionText: string;
  point: number;
};

const questions: Question[] = [
  {
    questionNo: 1,
    questionText: "투자에서 가장 중요하게 생각하는 것은 무엇인가요?",
    options: [
      { optionNo: 1, text: "원금 손실을 최대한 피하고 싶어요", point: 1 },
      { optionNo: 2, text: "안정적인 수익을 더 중요하게 생각해요", point: 2 },
      { optionNo: 3, text: "안정성과 수익의 균형이 중요해요", point: 3 },
      { optionNo: 4, text: "높은 수익 가능성이 더 중요해요", point: 4 },
      { optionNo: 5, text: "큰 수익을 위해 높은 위험도 감수할 수 있어요", point: 5 },
    ],
  },
  {
    questionNo: 2,
    questionText: "보유한 주식이 10% 하락하면 어떻게 하실 건가요?",
    options: [
      { optionNo: 1, text: "불안해서 바로 매도할 것 같아요", point: 1 },
      { optionNo: 2, text: "일부를 매도해서 손실을 줄일 것 같아요", point: 2 },
      { optionNo: 3, text: "상황을 보고 보유할 것 같아요", point: 3 },
      { optionNo: 4, text: "좋은 종목이라면 추가 매수를 고려할 수 있어요", point: 4 },
      { optionNo: 5, text: "하락을 기회로 보고 적극적으로 추가 매수할 수 있어요", point: 5 },
    ],
  },
  {
    questionNo: 3,
    questionText: "투자 기간은 어느 정도로 생각하고 있나요?",
    options: [
      { optionNo: 1, text: "3개월 이내의 짧은 기간", point: 1 },
      { optionNo: 2, text: "6개월 정도", point: 2 },
      { optionNo: 3, text: "1년 정도", point: 3 },
      { optionNo: 4, text: "1년에서 3년 정도", point: 4 },
      { optionNo: 5, text: "3년 이상 장기 투자", point: 5 },
    ],
  },
  {
    questionNo: 4,
    questionText: "투자 경험은 어느 정도인가요?",
    options: [
      { optionNo: 1, text: "거의 없어요", point: 1 },
      { optionNo: 2, text: "예금, 적금 위주로만 해봤어요", point: 2 },
      { optionNo: 3, text: "주식 매수/매도 경험은 있어요", point: 3 },
      { optionNo: 4, text: "여러 종목에 투자해본 경험이 있어요", point: 4 },
      { optionNo: 5, text: "직접 분석하고 적극적으로 매매해본 경험이 있어요", point: 5 },
    ],
  },
  {
    questionNo: 5,
    questionText: "감당할 수 있는 손실 수준은 어느 정도인가요?",
    options: [
      { optionNo: 1, text: "원금 손실은 거의 감당하기 어려워요", point: 1 },
      { optionNo: 2, text: "10% 이내 손실은 감당할 수 있어요", point: 2 },
      { optionNo: 3, text: "30% 정도 손실은 감당할 수 있어요", point: 3 },
      { optionNo: 4, text: "50% 정도 손실도 감당할 수 있어요", point: 4 },
      { optionNo: 5, text: "매우 큰 손실 가능성도 감수할 수 있어요", point: 5 },
    ],
  },
  {
    questionNo: 6,
    questionText: "선호하는 종목 유형은 무엇인가요?",
    options: [
      { optionNo: 1, text: "배당주나 안정적인 우량주", point: 1 },
      { optionNo: 2, text: "대형주 중심의 안정적인 종목", point: 2 },
      { optionNo: 3, text: "실적과 성장성을 함께 보는 종목", point: 3 },
      { optionNo: 4, text: "성장주나 기술주", point: 4 },
      { optionNo: 5, text: "테마주나 변동성이 큰 종목", point: 5 },
    ],
  },
  {
    questionNo: 7,
    questionText: "투자 판단은 주로 어떻게 하고 싶나요?",
    options: [
      { optionNo: 1, text: "전문가 추천이나 안정적인 정보만 참고하고 싶어요", point: 1 },
      { optionNo: 2, text: "뉴스와 기본 정보를 보고 신중하게 판단하고 싶어요", point: 2 },
      { optionNo: 3, text: "뉴스, 재무정보, 차트를 함께 보고 싶어요", point: 3 },
      { optionNo: 4, text: "시장 흐름과 차트를 적극적으로 활용하고 싶어요", point: 4 },
      { optionNo: 5, text: "직접 분석하고 빠르게 매매 판단을 하고 싶어요", point: 5 },
    ],
  },
];

function getResultType(totalPoint: number) {
  if (totalPoint <= 11) return "안정형";
  if (totalPoint <= 18) return "안정추구형";
  if (totalPoint <= 24) return "위험중립형";
  if (totalPoint <= 31) return "적극투자형";
  return "공격투자형";
}

function getResultDescription(resultType: string) {
  switch (resultType) {
    case "안정형":
      return "투자 수익률보다 원금 보존을 중요하게 생각하는 유형입니다. 변동성이 낮은 자산이나 안정적인 종목이 잘 맞을 수 있습니다.";
    case "안정추구형":
      return "안정성을 중요하게 생각하지만, 일정 수준의 수익도 기대하는 유형입니다. 우량주나 배당주 중심의 투자가 잘 맞을 수 있습니다.";
    case "위험중립형":
      return "위험과 수익의 균형을 고려하며 투자하는 유형입니다. 안정적인 종목과 성장 가능성이 있는 종목을 함께 볼 수 있습니다.";
    case "적극투자형":
      return "높은 수익을 위해 어느 정도의 위험을 감수할 수 있는 유형입니다. 성장주나 기술주에도 관심을 가질 수 있습니다.";
    case "공격투자형":
      return "높은 수익을 목표로 변동성이 큰 투자도 감수할 수 있는 유형입니다. 다만 리스크 관리가 꼭 필요합니다.";
    default:
      return "";
  }
}

export default function InvestmentType() {
  const router = useRouter();

  const [started, setStarted] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, Answer>>({});
  const [showResult, setShowResult] = useState(false);

  const currentQuestion = questions[currentIndex];
  const currentAnswer = answers[currentQuestion.questionNo];
  const isLastQuestion = currentIndex === questions.length - 1;

  const totalPoint = useMemo(() => {
    return Object.values(answers).reduce((sum, answer) => sum + answer.point, 0);
  }, [answers]);

  const answeredCount = Object.keys(answers).length;
  const isComplete = answeredCount === questions.length;
  const resultType = isComplete ? getResultType(totalPoint) : "";
  const resultDescription = resultType ? getResultDescription(resultType) : "";

  function handleSelect(question: Question, option: Option) {
    setAnswers((prev) => ({
      ...prev,
      [question.questionNo]: {
        questionNo: question.questionNo,
        questionText: question.questionText,
        optionNo: option.optionNo,
        optionText: option.text,
        point: option.point,
      },
    }));
  }

  function handleNext() {
    if (!currentAnswer) {
      alert("답변을 선택해주세요.");
      return;
    }

    if (isLastQuestion) {
      handleSubmit();
      return;
    }

    setCurrentIndex((prev) => prev + 1);
  }

  function handlePrev() {
    setCurrentIndex((prev) => Math.max(prev - 1, 0));
  }

  async function handleSubmit() {
    if (!isComplete) {
      alert("모든 질문에 답변해주세요.");
      return;
    }

    const payload = {
      totalPoint,
      resultType,
      answers: Object.values(answers).sort(
        (a, b) => a.questionNo - b.questionNo
      ),
    };

    console.log("투자성향 저장 payload:", payload);

    try {
        const { data } = await apiClient.post(
        "/users/me/investment-type",
        payload
        );

        console.log("투자성향 저장 응답:", data);

        window.localStorage.setItem(
        "investmentTypeResult",
        JSON.stringify(data.data)
        );

        setStarted(false);
        setShowResult(true);
    } catch (error: any) {
        console.error("투자성향 저장 실패:", error);
        console.error("응답 상태:", error.response?.status);
        console.error("응답 데이터:", error.response?.data);

        alert(error.response?.data?.message || "투자성향 저장에 실패했습니다.");
    }
  }

  function handleRestart() {
    setAnswers({});
    setCurrentIndex(0);
    setShowResult(false);
    setStarted(true);
  }

  function handleGoMyPage() {
    router.push("/main/myPage/member");
  }

  return (
    <main className={layoutStyles.page}>
      <MyPageSidebar />

      <section className={layoutStyles.content}>
        <div className={layoutStyles.pageTitleArea}>
          <p className={layoutStyles.pageLabel}>MY PAGE</p>
          <h1 className={layoutStyles.pageTitle}>투자성향 분석</h1>
        </div>

        {!started && !showResult && (
          <section className={styles.startCard}>
            <p className={styles.badge}>투자 성향 테스트</p>

            <h2>나에게 맞는 투자 성향을 확인해보세요</h2>

            <p>
              총 7개의 질문에 답변하면 투자 성향 점수를 계산하고,
              향후 AI 종목 추천에 활용할 수 있습니다.
            </p>

            <button
              type="button"
              className={styles.submitButton}
              onClick={() => setStarted(true)}
            >
              분석 시작하기
            </button>
          </section>
        )}

        {started && !showResult && (
          <section className={styles.surveyCard}>
            <div className={styles.surveyTop}>
            <h2>
            <span className={styles.badge}>
                Q{currentQuestion.questionNo} 
            </span>
            </h2>
            <span className={styles.progressText}>
                {currentIndex + 1}번째 질문 / 총 {questions.length}개
            </span>
            </div>

            <div className={styles.progressTrack}>
              <div
                className={styles.progressBar}
                style={{
                  width: `${((currentIndex + 1) / questions.length) * 100}%`,
                }}
              />
            </div>

            <h2 className={styles.questionTitle}>
              {currentQuestion.questionText}
            </h2>

            <div className={styles.optionList}>
              {currentQuestion.options.map((option) => {
                const selected = currentAnswer?.optionNo === option.optionNo;

                return (
                  <button
                    key={option.optionNo}
                    type="button"
                    className={`${styles.optionButton} ${
                      selected ? styles.selected : ""
                    }`}
                    onClick={() => handleSelect(currentQuestion, option)}
                  >
                    <span>{option.text}</span>
                    <strong>{option.point}점</strong>
                  </button>
                );
              })}
            </div>
            <br />
            <div className={styles.surveyActions}>
              <button
                type="button"
                className={styles.secondaryButton}
                onClick={handlePrev}
                disabled={currentIndex === 0}
              >
                이전
              </button>
              {" "}
              <button
                type="button"
                className={styles.submitButton}
                onClick={handleNext}
                disabled={!currentAnswer}
              >
                {isLastQuestion ? "결과 확인하기" : "다음"}
              </button>
            </div>
          </section>
        )}

        {showResult && (
          <section className={styles.resultCard}>
            <p className={styles.resultLabel}>분석 결과</p>

            <h2>{resultType}</h2>

            <strong>{totalPoint}점</strong>

            <p>{resultDescription}</p>

            <div className={styles.resultActions}>
              <button
                type="button"
                className={styles.secondaryButton}
                onClick={handleRestart}
              >
                다시 분석하기
              </button>

              <button
                type="button"
                className={styles.submitButton}
                onClick={handleGoMyPage}
              >
                마이페이지로 이동
              </button>
            </div>
          </section>
        )}
      </section>
    </main>
  );
}