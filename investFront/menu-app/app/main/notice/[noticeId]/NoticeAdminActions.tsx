"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import styles from "./NoticeDetailPage.module.css";
import {getCurrentUser, type LoginUser,} from "@/lib/auth-user";
import { apiClient } from "@/lib/api-client";

export default function NoticeAdminActions({
  noticeId,
}: {
  noticeId: number;
}) {
  const router = useRouter();
  const [user, setUser] = useState<LoginUser | null>(null);

  useEffect(() => {
    async function loadUser() {
      const me = await getCurrentUser();
      setUser(me);
    }
    loadUser();
  }, []);

  async function handleDelete() {
    const ok = confirm(
      "정말 삭제하시겠습니까?"
    );

    if (!ok) {
      return;
    }

    try {
      await apiClient.delete(
        `/notice/${noticeId}`
      );
      alert("삭제 완료");

      router.push("/main/notice");
    } catch (error: any) {
      console.error(error);
      alert(error?.response?.data?.message ?? "삭제 실패");
    }
  }

  if (!user || user.auth !== 1) {
    return null;
  }

  return (
    <div className={styles.actionBar}>
      <Link
        href={`/main/notice/edit/${noticeId}`}
        className={styles.editButton}
      >
        수정
      </Link>

      <button
        className={styles.deleteButton}
        onClick={handleDelete}
      >
        삭제
      </button>
    </div>
  );
}