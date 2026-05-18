const STORAGE_KEY = "memberEditAuth";
const TTL_MS = 10 * 60 * 1000;

type MemberEditAuth = {
  userNo: number;
  editToken: string;
  expiresAt: number;
};

function readAuth(): MemberEditAuth | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.sessionStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as MemberEditAuth;

    if (
      typeof parsed.userNo !== "number" ||
      typeof parsed.editToken !== "string" ||
      typeof parsed.expiresAt !== "number"
    ) {
      return null;
    }

    if (Date.now() > parsed.expiresAt) {
      window.sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }

    return parsed;
  } catch {
    window.sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function saveMemberEditAuth(userNo: number, editToken: string): void {
  if (typeof window === "undefined") {
    return;
  }

  const payload: MemberEditAuth = {
    userNo,
    editToken,
    expiresAt: Date.now() + TTL_MS,
  };

  window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
}

export function getMemberEditToken(userNo: number): string | null {
  const auth = readAuth();

  if (!auth || auth.userNo !== userNo) {
    return null;
  }

  return auth.editToken;
}

export function hasValidMemberEditAuth(userNo: number): boolean {
  return getMemberEditToken(userNo) !== null;
}

export function clearMemberEditAuth(): void {
  if (typeof window === "undefined") {
    return;
  }

  window.sessionStorage.removeItem(STORAGE_KEY);
}
