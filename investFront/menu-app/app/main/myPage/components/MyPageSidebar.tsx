"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./MyPageSidebar.module.css";

export default function MyPageSidebar() {
  const pathname = usePathname();

  return (
    <aside className={styles.sidebar}>
      <nav className={styles.menuList}>
        <Link
          href="/main/myPage/member"
          className={`${styles.menuButton} ${
            pathname === "/main/myPage/member" ? styles.active : ""
          }`}
        >
          내 정보
        </Link>

        <Link
          href="/main/myPage/guide"
          className={`${styles.menuButton} ${
            pathname === "/main/myPage/guide" ? styles.active : ""
          }`}
        >
          투자 가이드
        </Link>
      </nav>
    </aside>
  );
}