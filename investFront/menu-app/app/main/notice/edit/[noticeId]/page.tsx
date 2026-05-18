"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import styles from "./EditPage.module.css";
import { apiClient } from "@/lib/api-client";
import {getCurrentUser, type LoginUser,} from "@/lib/auth-user";

type NoticeDetail = {
  noticeId: number;
  title: string;
  content: string;
  createdAt: string;
};

export default function NoticeEditPage() {
  const params = useParams();
  const router = useRouter();
  const noticeId = params.noticeId as string;
  const [user, setUser] = useState<LoginUser | null>(null);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitLoading, setSubmitLoading] = useState(false);

  useEffect(() => {
    async function initialize() {
      try {
        const me = await getCurrentUser();

        if (!me || me.auth !== 1) {
          alert("관리자만 접근 가능합니다.");
          router.replace("/main/notice");
          return;
        }

        setUser(me);

        const response =
          await apiClient.get<NoticeDetail>(
            `/notice/${noticeId}`
          );

        setTitle(response.data.title);
        setContent(response.data.content);

      } catch {
        alert("공지 조회 실패");
        router.replace("/main/notice");

      } finally {
        setLoading(false);
      }
    }
    initialize();
  }, [noticeId, router]);

  async function handleSubmit() {
    try {
      setSubmitLoading(true);

      await apiClient.put(
        `/notice/${noticeId}`,
        {
          title,
          content,
        }
      );
      alert("수정 완료");

      router.push(`/main/notice/${noticeId}`);

    } catch (error: any) {
      console.error(error);
      alert(error?.response?.data?.message ?? "수정 실패");
    } finally {
      setSubmitLoading(false);
    }
  }

  if (loading) {
    return (
      <main className={styles.page}>
        <div className={styles.loading}>
          불러오는 중...
        </div>
      </main>
    );
  }

  if (!user || user.auth !== 1) {
    return null;
  }

  return (
    <main className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.title}>
          공지 수정
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
            submitLoading ||
            !title.trim() ||
            !content.trim()
          }
          className={styles.submitButton}
        >
          수정 완료
        </button>
      </div>
    </main>
  );
}