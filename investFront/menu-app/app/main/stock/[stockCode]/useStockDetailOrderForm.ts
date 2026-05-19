"use client";

import { useCallback, useEffect, useState } from "react";

import {
  createOrder,
  type OrderKind,
  type OrderType,
} from "@/lib/order";
import { snapPriceToTick } from "@/lib/stock/koreanStockPriceTick";
import { parseNumeric } from "@/lib/stock/stockDetailFormat";
import type { PriceResponse } from "@/lib/stock/stockDetailTypes";

export function useStockDetailOrderForm(
  stockCode: string,
  price: PriceResponse | null
) {
  const [orderKind, setOrderKind] = useState<OrderKind>("BUY");
  const [orderType, setOrderType] = useState<OrderType>("LIMIT");
  const [quantity, setQuantity] = useState("");
  const [orderPrice, setOrderPrice] = useState("");
  const [orderLoading, setOrderLoading] = useState(false);
  const [orderMessage, setOrderMessage] = useState<string | null>(null);

  useEffect(() => {
    const current = parseNumeric(price?.currentPrice);

    if (orderType !== "LIMIT" || current === null || current <= 0) {
      return;
    }

    setOrderPrice((prev) => {
      if (prev.trim()) {
        return prev;
      }

      return Math.round(snapPriceToTick(current)).toLocaleString("ko-KR");
    });
  }, [orderType, price?.currentPrice]);

  const handleCreateOrder = useCallback(async () => {
    setOrderMessage(null);

    const currentPrice = Number(price?.currentPrice ?? 0);

    const requestPrice =
      orderType === "MARKET"
        ? currentPrice
        : Number(orderPrice.replaceAll(",", ""));

    const orderQuantity = Number(quantity.replaceAll(",", ""));

    if (!requestPrice || requestPrice <= 0) {
      setOrderMessage("주문 가격을 입력해주세요.");
      return;
    }

    if (!orderQuantity || orderQuantity <= 0) {
      setOrderMessage("주문 수량을 입력해주세요.");
      return;
    }

    try {
      setOrderLoading(true);

      const response = await createOrder({
        stockCode,
        orderKind,
        orderType,
        price: requestPrice,
        quantity: orderQuantity,
      });

      setOrderMessage(
        response.status === "PENDING"
          ? `${response.orderKind === "BUY" ? "매수" : "매도"} 예약이 완료되었습니다.`
          : `${response.orderKind === "BUY" ? "매수" : "매도"} 주문이 완료되었습니다.`
      );

      setQuantity("");

      if (orderType === "LIMIT") {
        const current = parseNumeric(price?.currentPrice);
        setOrderPrice(
          current !== null && current > 0
            ? Math.round(snapPriceToTick(current)).toLocaleString("ko-KR")
            : ""
        );
      }
    } catch (error) {
      console.error(error);
      setOrderMessage("주문 처리에 실패했습니다.");
    } finally {
      setOrderLoading(false);
    }
  }, [
    orderKind,
    orderType,
    orderPrice,
    price?.currentPrice,
    quantity,
    stockCode,
  ]);

  return {
    orderKind,
    setOrderKind,
    orderType,
    setOrderType,
    quantity,
    setQuantity,
    orderPrice,
    setOrderPrice,
    orderLoading,
    orderMessage,
    handleCreateOrder,
  };
}