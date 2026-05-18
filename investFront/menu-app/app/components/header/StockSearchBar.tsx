"use client";

import Image from "next/image";
import Link from "next/link";
import {
  useEffect,
  useRef,
  useState,
} from "react";
import { useRouter } from "next/navigation";
import styles from "./StockSearchBar.module.css";
import { apiClient } from "@/lib/api-client";

type SuggestItem = {
  stockCode: string;
  stockName: string;
  marketType: string;
};

export default function StockSearchBar() {
  const router = useRouter();
  const wrapRef = useRef<HTMLDivElement | null>(null);
  const [keyword, setKeyword] = useState("");
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<SuggestItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const q = keyword.trim();

    if (!q) {
      setItems([]);
      return;
    }

    const timer = setTimeout(async () => {

      try {
        setLoading(true);

        const response =
          await apiClient.get<SuggestItem[]>(
            `/search/suggest`,
            {
              params: {
                keyword: q,
              },
            }
          );
        setItems(response?.data);

      } catch (error) {
        console.error(error);
        setItems([]);

      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [keyword]);

  useEffect(() => {
    function handleOutside(
      event: MouseEvent
    ) {
      if (
        wrapRef.current &&
        !wrapRef.current.contains(
          event.target as Node
        )
      ) {
        setOpen(false);
      }
    }

    document.addEventListener(
      "mousedown",
      handleOutside
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleOutside
      );
    };
  }, []);

  function handleSubmit() {
    const q = keyword.trim();

    if (!q) {
      return;
    }
    setOpen(false);

    router.push(
      `/main/search?q=${encodeURIComponent(q)}`
    );
  }

  return (
    <div
      ref={wrapRef}
      className={styles.searchWrap}
    >
      <div className={styles.searchBox}>
        <Image
          src="/search.png"
          alt="검색"
          width={18}
          height={18}
          className={styles.searchIcon}
        />

        <input
          type="text"
          value={keyword}
          placeholder="종목명 또는 종목코드 검색"
          className={styles.searchInput}
          onFocus={() => setOpen(true)}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              handleSubmit();
            }
          }}
        />
      </div>

      {open && keyword.trim() && (
        <div className={styles.dropdown}>
          {loading ? (
            <div className={styles.loading}>
              검색 중...
            </div>
          ) : items?.length === 0 ? (
            <div className={styles.empty}>
              검색 결과가 없습니다.
            </div>
          ) : (
            <div className={styles.resultList}>
              {items?.map((item) => (
                <Link
                  key={item.stockCode}
                  href={`/main/stock/${item.stockCode}`}
                  className={styles.resultItem}
                  onClick={() => setOpen(false)}
                >
                  <div className={styles.leftArea}>
                    <div className={styles.stockName}>
                      {item.stockName}
                    </div>

                    <div className={styles.stockCode}>
                      {item.stockCode}
                    </div>
                  </div>

                  <div className={styles.market}>
                    {item.marketType}
                  </div>
                </Link>
              ))}

            </div>
          )}
        </div>
      )}
    </div>
  );
}