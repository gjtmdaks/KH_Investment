export function formatRelativeTimeKo(iso: string): string {
  if (!iso) return "";
  const t = Date.parse(iso);
  if (Number.isNaN(t)) return "";
  const diff = Date.now() - t;
  if (diff < 60_000) return "방금 전";
  const mins = Math.floor(diff / 60_000);
  if (mins < 60) return `${mins}분 전`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}시간 전`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}일 전`;
  return new Date(t).toLocaleDateString("ko-KR", {
    month: "short",
    day: "numeric",
  });
}

export function shortenHost(publisher: string): string {
  if (!publisher || publisher === "-") return "출처 미상";
  return publisher.replace(/^www\./, "");
}

export function thumbLetter(title: string): string {
  const t = title?.trim();
  if (!t) return "·";
  const ch = t[0];
  if (/[a-zA-Z]/.test(ch)) return ch.toUpperCase();
  return ch;
}

/**
 * 모달 미리보기 등에서 말줄임 표시를 통일
 * 이미 "..." 또는 "…"로 끝나거나, 완결 문장(., ?, !)으로 끝나면 추가X.
 */
export function ensurePreviewTrailingEllipsis(text: string): string {
  const t = text.trim();
  if (!t) return "";
  if (t.endsWith("...") || t.endsWith("\u2026")) {
    return t;
  }
  const last = t[t.length - 1];
  if (last === "." || last === "!" || last === "?" || last === "…") {
    return t;
  }
  return `${t}...`;
}
