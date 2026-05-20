"use client";

import { useEffect } from "react";

import {
  buildStockDetailDocumentTitle,
  DEFAULT_APP_DOCUMENT_TITLE,
} from "@/lib/stock/stockDetailDocumentTitle";
import type { PriceResponse } from "@/lib/stock/stockDetailTypes";

export function useStockDetailDocumentTitle(
  price: PriceResponse | null,
  displayName: string
) {
  useEffect(() => {
    const nextTitle = buildStockDetailDocumentTitle(price, displayName);

    if (nextTitle) {
      document.title = nextTitle;
    }

    return () => {
      document.title = DEFAULT_APP_DOCUMENT_TITLE;
    };
  }, [price, displayName]);
}
