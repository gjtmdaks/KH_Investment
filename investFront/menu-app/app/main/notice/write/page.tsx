"use client";

import { useEffect, useState } from "react";
import {getCurrentUser,} from "@/lib/auth-user";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import styles from "./WritePage.module.css";

export default function NoticeWritePage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function validateAdmin() {
      const me = await getCurrentUser();

      if (!me || me.auth !== 1) {
        alert("관리자만 접근 가능합니다.");
        router.replace("/main/notice");
      }
    }
    validateAdmin();
  }, [router]);

  async function handleSubmit() {
    try {
      setLoading(true);

      await apiClient.post("/notice", {
        title,
        content,
      });

      router.push("/main/notice");
    } catch (error: any) {
      console.error(error);
      alert(error?.response?.data?.message ?? "공지 작성 실패");
      
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.title}>
          공지 작성
        </h1>

        <input
          type="text"
          placeholder="제목 입력"
          value={title}
          onChange={(e) =>
            setTitle(e.target.value)
          }
          className={styles.input}
        />

        <textarea
          placeholder="내용 입력"
          value={content}
          onChange={(e) =>
            setContent(e.target.value)
          }
          className={styles.textarea}
        />

        <button
          onClick={handleSubmit}
          disabled={
            loading ||
            !title.trim() ||
            !content.trim()
          }
          className={styles.submitButton}
        >
          등록하기
        </button>
      </div>
    </main>
  );
}