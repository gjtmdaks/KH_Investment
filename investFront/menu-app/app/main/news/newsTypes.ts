export type RelatedStock = {
  stockCode: string;
  stockName: string;
  changeRate?: number | null;
};

export type NewsItem = {
  newsInfoId: number | null;
  title: string;
  description: string;
  publisher: string;
  primaryLabel?: string | null;
  keywordKind?: "STOCK" | "SECTOR" | "MACRO" | "ISSUE" | null;
  articleLink: string;
  publishedAt: string;
  relatedStocks?: RelatedStock[] | null;
};
