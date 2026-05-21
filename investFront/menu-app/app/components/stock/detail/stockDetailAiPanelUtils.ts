import panelStyles from "./css/stockDetailAiPanel.module.css";

export function getAiSignalMeta(aiSignal: string) {
  if (aiSignal === "POSITIVE") {
    return {
      signalClass: panelStyles.positive,
      signalLabel: "🟢 긍정",
    };
  }

  if (aiSignal === "NEGATIVE") {
    return {
      signalClass: panelStyles.negative,
      signalLabel: "🔴 부정",
    };
  }

  return {
    signalClass: panelStyles.neutral,
    signalLabel: "🟡 중립",
  };
}

export function getInvestmentOpinionLabel(opinion: string) {
  if (opinion === "BUY") {
    return "매수";
  }

  if (opinion === "SELL") {
    return "매도";
  }

  return "관망";
}

export function formatAiReportDate(date?: string | null) {
  if (!date) {
    return "-";
  }

  const parsed = new Date(date);

  if (isNaN(parsed.getTime())) {
    return "-";
  }

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(parsed);
}
