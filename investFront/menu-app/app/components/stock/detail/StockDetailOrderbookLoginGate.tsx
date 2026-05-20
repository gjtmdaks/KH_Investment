"use client";

import { usePathname, useRouter } from "next/navigation";

import {
  buildSignInUrl,
  storePostLoginRedirect,
} from "@/lib/auth-redirect";

import styles from "./css/stockDetailOrderbookLoginGate.module.css";

export function StockDetailOrderbookLoginGate({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();

  function handleLoginClick() {
    const returnPath = `${pathname}${window.location.search}`;
    storePostLoginRedirect(returnPath);
    router.push(buildSignInUrl(returnPath));
  }

  return (
    <div className={styles.wrap}>
      <div className={styles.blurred} aria-hidden>
        {children}
      </div>

      <div className={styles.overlay}>
        <div className={styles.message}>
          <p className={styles.titleLine}>호가를 보려면</p>
          <p className={styles.titleLine}>로그인이 필요해요</p>
        </div>

        <button
          type="button"
          className={styles.loginButton}
          onClick={handleLoginClick}
        >
          로그인하기
        </button>
      </div>
    </div>
  );
}
