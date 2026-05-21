"use client";



import Image from "next/image";

import Link from "next/link";

import {

  useCallback,

  useEffect,

  useRef,

  useState,

} from "react";

import { useRouter } from "next/navigation";

import styles from "./StockSearchBar.module.css";

import { apiClient } from "@/lib/api-client";

import {

  addRecentStockSearch,

  getRecentStockSearches,

  removeRecentStockSearch,

} from "@/lib/stock/recentSearchStorage";



type SuggestItem = {

  stockCode: string;

  stockName: string;

  marketType: string;

};



function isEditableElement(target: EventTarget | null): boolean {

  if (!(target instanceof HTMLElement)) {

    return false;

  }

  const tag = target.tagName;

  if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT") {

    return true;

  }

  return target.isContentEditable;

}



export default function StockSearchBar() {

  const router = useRouter();

  const wrapRef = useRef<HTMLDivElement | null>(null);

  const inputRef = useRef<HTMLInputElement | null>(null);

  const [keyword, setKeyword] = useState("");

  const [open, setOpen] = useState(false);

  const [focused, setFocused] = useState(false);

  const [items, setItems] = useState<SuggestItem[]>([]);

  const [loading, setLoading] = useState(false);

  const [recentSearches, setRecentSearches] = useState<string[]>([]);



  const refreshRecentSearches = useCallback(() => {

    setRecentSearches(getRecentStockSearches());

  }, []);



  const rememberSearch = useCallback((value: string) => {

    addRecentStockSearch(value);

    refreshRecentSearches();

  }, [refreshRecentSearches]);



  useEffect(() => {

    const q = keyword.trim();



    if (!q) {

      setItems([]);

      return;

    }



    const timer = setTimeout(async () => {

      try {

        setLoading(true);



        const response = await apiClient.get<SuggestItem[]>(

          `/search/suggest`,

          {

            params: {

              keyword: q,

            },

          }

        );

        setItems(response?.data ?? []);

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

    function handleSlashFocus(event: KeyboardEvent) {

      if (event.key !== "/" || event.ctrlKey || event.metaKey || event.altKey) {

        return;

      }

      if (isEditableElement(event.target)) {

        return;

      }



      event.preventDefault();

      inputRef.current?.focus();

      setOpen(true);

      refreshRecentSearches();

    }



    window.addEventListener("keydown", handleSlashFocus);



    return () => {

      window.removeEventListener("keydown", handleSlashFocus);

    };

  }, [refreshRecentSearches]);



  function clearSearch() {

    setKeyword("");

    setItems([]);

    setOpen(false);

    setFocused(false);

    inputRef.current?.blur();

  }



  useEffect(() => {

    function handleOutside(event: MouseEvent) {

      if (

        wrapRef.current &&

        !wrapRef.current.contains(event.target as Node)

      ) {

        clearSearch();

      }

    }



    document.addEventListener("mousedown", handleOutside);



    return () => {

      document.removeEventListener("mousedown", handleOutside);

    };

  }, []);



  function handleSubmit() {

    const q = keyword.trim();



    if (!q) {

      return;

    }



    rememberSearch(q);

    router.push(`/main/search?q=${encodeURIComponent(q)}`);

    clearSearch();

  }



  function handleRecentSelect(term: string) {

    setKeyword(term);

    setOpen(true);

    inputRef.current?.focus();

  }



  function handleRecentRemove(event: React.MouseEvent, term: string) {

    event.preventDefault();

    event.stopPropagation();

    removeRecentStockSearch(term);

    refreshRecentSearches();

  }



  const trimmedKeyword = keyword.trim();

  const showRecentPanel = open && !trimmedKeyword && recentSearches.length > 0;

  const showSuggestPanel = open && !!trimmedKeyword;



  return (

    <div ref={wrapRef} className={styles.searchWrap}>

      <div className={styles.searchBox}>

        <Image

          src="/search.png"

          alt="검색"

          width={18}

          height={18}

          className={styles.searchIcon}

        />



        <input

          ref={inputRef}

          type="text"

          value={keyword}

          aria-label="종목 검색"

          className={styles.searchInput}

          onFocus={() => {

            setFocused(true);

            setOpen(true);

            refreshRecentSearches();

          }}

          onBlur={() => setFocused(false)}

          onChange={(e) => setKeyword(e.target.value)}

          onKeyDown={(e) => {

            if (e.key === "Enter") {

              handleSubmit();

            }

            if (e.key === "Escape") {

              clearSearch();

            }

          }}

        />



        {!keyword && !focused && (

          <div className={styles.inputHint} aria-hidden>

            <kbd className={styles.shortcutKey}>/</kbd>

            <span>를 눌러 검색하세요</span>

          </div>

        )}

      </div>


      {showRecentPanel && (

        <div className={styles.dropdown}>

          <div className={styles.recentHeader}>최근 검색</div>

          <div className={styles.recentChips}>

            {recentSearches.map((term) => (

              <div key={term} className={styles.recentChip}>

                <button

                  type="button"

                  className={styles.recentChipLabel}

                  onClick={() => handleRecentSelect(term)}

                >

                  {term}

                </button>

                <button

                  type="button"

                  className={styles.recentChipRemove}

                  aria-label={`${term} 삭제`}

                  onClick={(event) => handleRecentRemove(event, term)}

                >

                  ×

                </button>

              </div>

            ))}

          </div>

        </div>

      )}



      {showSuggestPanel && (

        <div className={styles.dropdown}>

          {loading ? (

            <div className={styles.loading}>검색 중...</div>

          ) : items.length === 0 ? (

            <div className={styles.empty}>검색 결과가 없습니다.</div>

          ) : (

            <div className={styles.resultList}>

              {items.map((item) => (

                <Link

                  key={item.stockCode}

                  href={`/main/stock/${item.stockCode}`}

                  className={styles.resultItem}

                  onClick={() => {

                    rememberSearch(trimmedKeyword);

                    clearSearch();

                  }}

                >

                  <div className={styles.leftArea}>

                    <div className={styles.stockName}>{item.stockName}</div>

                    <div className={styles.stockCode}>{item.stockCode}</div>

                  </div>



                  <div className={styles.market}>{item.marketType}</div>

                </Link>

              ))}

            </div>

          )}

        </div>

      )}

    </div>

  );

}

