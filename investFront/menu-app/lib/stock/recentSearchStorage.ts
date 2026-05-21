const STORAGE_KEY = "kh_stock_recent_searches";
const MAX_ITEMS = 10;

function readRaw(): string[] {
  if (typeof window === "undefined") {
    return [];
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed.filter(
      (item): item is string =>
        typeof item === "string" && item.trim().length > 0
    );
  } catch {
    return [];
  }
}

function writeRaw(items: string[]): void {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
}

export function getRecentStockSearches(): string[] {
  return readRaw();
}

export function addRecentStockSearch(keyword: string): void {
  const trimmed = keyword.trim();
  if (!trimmed) {
    return;
  }
  const next = [
    trimmed,
    ...readRaw().filter(
      (item) => item.toLowerCase() !== trimmed.toLowerCase()
    ),
  ].slice(0, MAX_ITEMS);
  writeRaw(next);
}

export function removeRecentStockSearch(keyword: string): void {
  const trimmed = keyword.trim();
  if (!trimmed) {
    return;
  }
  writeRaw(
    readRaw().filter(
      (item) => item.toLowerCase() !== trimmed.toLowerCase()
    )
  );
}
