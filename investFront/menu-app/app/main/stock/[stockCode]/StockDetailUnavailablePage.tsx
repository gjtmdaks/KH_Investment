"use client";

import { useCallback } from "react";
import { useRouter } from "next/navigation";

import StockDetailUnavailableModal from "@/app/components/stock/detail/StockDetailUnavailableModal";

export default function StockDetailUnavailablePage() {
  const router = useRouter();

  const handleConfirm = useCallback(() => {
    router.replace("/main");
  }, [router]);

  return <StockDetailUnavailableModal open onConfirm={handleConfirm} />;
}
