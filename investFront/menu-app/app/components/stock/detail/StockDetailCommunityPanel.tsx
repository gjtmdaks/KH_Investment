"use client";

import { useEffect, useState } from "react";

import {
  createStockBoardPost,
  deleteBoardPost,
  getStockBoardPosts,
  type BoardPost,
} from "@/lib/board";

import styles from "./stockDetail.module.css";

type Props = {
  stockCode: string;
};

function formatDate(value: string) {
  if (!value) return "-";

  return new Date(value).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function StockDetailCommunityPanel({ stockCode }: Props) {
  const [posts, setPosts] = useState<BoardPost[]>([]);
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  async function fetchPosts() {
    try {
      setLoading(true);
      setMessage(null);

      const data = await getStockBoardPosts(stockCode);
      setPosts(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error(error);
      setMessage("게시글을 불러오지 못했습니다.");
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }

  async function handleCreatePost() {
    const trimmedContent = content.trim();

    if (!trimmedContent) {
      setMessage("내용을 입력해주세요.");
      return;
    }

    if (trimmedContent.length > 2000) {
      setMessage("게시글은 2,000자 이하로 입력해주세요.");
      return;
    }

    try {
      setSubmitting(true);
      setMessage(null);

      const createdPost = await createStockBoardPost(stockCode, {
        content: trimmedContent,
        parentId: null,
      });

      setPosts((prev) => [createdPost, ...prev]);
      setContent("");
    } catch (error) {
      console.error(error);
      setMessage("게시글 작성에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDeletePost(boardNo: number) {
    const confirmed = window.confirm("게시글을 삭제하시겠습니까?");

    if (!confirmed) return;

    try {
      await deleteBoardPost(boardNo);
      setPosts((prev) => prev.filter((post) => post.boardNo !== boardNo));
    } catch (error) {
      console.error(error);
      alert("게시글 삭제에 실패했습니다.");
    }
  }

  useEffect(() => {
    fetchPosts();
  }, [stockCode]);

  return (
    <section className={styles.communityPanel}>
      <div className={styles.communityHeader}>
        <div>
          <h2>종목 커뮤니티</h2>
          <p>이 종목에 대한 의견을 자유롭게 남겨보세요.</p>
        </div>

        <button
          type="button"
          className={styles.communityRefreshButton}
          onClick={() => void fetchPosts()}
          disabled={loading}
        >
          새로고침
        </button>
      </div>

      <div className={styles.communityWriteBox}>
        <textarea
          value={content}
          onChange={(event) => setContent(event.target.value)}
          placeholder="이 종목에 대한 의견을 작성해보세요."
          maxLength={2000}
        />

        <div className={styles.communityWriteFooter}>
          <span>{content.length.toLocaleString()} / 2,000</span>

          <button
            type="button"
            onClick={() => void handleCreatePost()}
            disabled={submitting}
          >
            {submitting ? "등록 중..." : "등록"}
          </button>
        </div>
      </div>

      {message ? <p className={styles.communityMessage}>{message}</p> : null}

      {loading ? (
        <div className={styles.communityEmpty}>게시글을 불러오는 중입니다.</div>
      ) : posts.length === 0 ? (
        <div className={styles.communityEmpty}>
          아직 작성된 게시글이 없습니다.
        </div>
      ) : (
        <div className={styles.communityList}>
          {posts.map((post) => (
            <article key={post.boardNo} className={styles.communityItem}>
              <div className={styles.communityItemHeader}>
                <div>
                  <strong>{post.userName ?? `회원 ${post.userNo}`}</strong>
                  <span>{formatDate(post.createdAt)}</span>
                </div>

                <button
                  type="button"
                  onClick={() => void handleDeletePost(post.boardNo)}
                >
                  삭제
                </button>
              </div>

              <p>{post.content}</p>

              <div className={styles.communityItemFooter}>
                <span>좋아요 {post.likeCount ?? 0}</span>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}