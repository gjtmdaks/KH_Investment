/** KOSPI/KOSDAQ 지정가 호가 단위(원) */
export function getKoreanStockPriceTick(price: number): number {
    const absPrice = Math.abs(price);
  
    if (absPrice < 2_000) {
      return 1;
    }
  
    if (absPrice < 5_000) {
      return 5;
    }
  
    if (absPrice < 20_000) {
      return 10;
    }
  
    if (absPrice < 50_000) {
      return 50;
    }
  
    if (absPrice < 200_000) {
      return 100;
    }
  
    if (absPrice < 500_000) {
      return 500;
    }
  
    return 1_000;
  }
  
  export function snapPriceToTick(price: number): number {
    const tick = getKoreanStockPriceTick(price);
  
    if (tick <= 0) {
      return price;
    }
  
    return Math.round(price / tick) * tick;
  }
  
  export function stepPriceByTick(price: number, direction: 1 | -1): number {
    const tick = getKoreanStockPriceTick(price);
    const next = price + direction * tick;
  
    if (direction < 0) {
      return Math.max(tick, next);
    }
  
    return next;
  }
  