"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import styles from "./NoticePage.module.css";
import {getCurrentUser, type LoginUser,} from "@/lib/auth-user";

export default function NoticeWriteButton() {

  const [user, setUser] = useState<LoginUser | null>(null);

  useEffect(() => {
    async function loadUser() {
      const me = await getCurrentUser();
      setUser(me);
    }
    loadUser();
  }, []);

  if (!user || user.auth !== 1) {
    return null;
  }

  return (
    <Link
      href="/main/notice/write"
      className={styles.writeButton}
    >
      공지 작성
    </Link>
  );
}