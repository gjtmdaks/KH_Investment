"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import {
  createStockBoardPost,
  deleteBoardPost,
  getStockBoardPosts,
  likeBoardPost,
  unlikeBoardPost,
  type BoardPost,
} from "@/lib/board";

import styles from "./css/StockDetailCommunityPanel.module.css";

type Props = {
  stockCode: string;
};

function formatDate(value: string) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getWriterName(post: BoardPost) {
  return post.userName || `회원 ${post.userNo}`;
}

function replacePost(posts: BoardPost[], updatedPost: BoardPost) {
  return posts.map((post) =>
    post.boardNo === updatedPost.boardNo ? updatedPost : post
  );
}

export function StockDetailCommunityPanel({ stockCode }: Props) {
  const [posts, setPosts] = useState<BoardPost[]>([]);
  const [content, setContent] = useState("");
  const [replyContent, setReplyContent] = useState("");
  const [replyTargetId, setReplyTargetId] = useState<number | null>(null);

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const parentComments = useMemo(() => {
    return posts.filter((post) => post.parentId === null);
  }, [posts]);

  const repliesByParentId = useMemo(() => {
    const result = new Map<number, BoardPost[]>();

    posts.forEach((post) => {
      if (post.parentId === null) {
        return;
      }

      const replies = result.get(post.parentId) ?? [];
      replies.push(post);
      result.set(post.parentId, replies);
    });

    return result;
  }, [posts]);

  const fetchPosts = useCallback(async () => {
    try {
      setLoading(true);
      setMessage(null);

      const data = await getStockBoardPosts(stockCode);
      setPosts(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error(error);
      setMessage("댓글을 불러오지 못했습니다.");
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [stockCode]);

  async function handleCreateComment() {
    const trimmedContent = content.trim();

    if (!trimmedContent) {
      setMessage("댓글 내용을 입력해주세요.");
      return;
    }

    if (trimmedContent.length > 500) {
      setMessage("댓글은 500자 이하로 입력해주세요.");
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
      setMessage("댓글 작성에 실패했습니다. 로그인이 필요할 수 있습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleCreateReply(parentId: number) {
    const trimmedContent = replyContent.trim();

    if (!trimmedContent) {
      setMessage("답글 내용을 입력해주세요.");
      return;
    }

    if (trimmedContent.length > 2000) {
      setMessage("답글은 500자 이하로 입력해주세요.");
      return;
    }

    try {
      setSubmitting(true);
      setMessage(null);

      const createdReply = await createStockBoardPost(stockCode, {
        content: trimmedContent,
        parentId,
      });

      setPosts((prev) => [...prev, createdReply]);
      setReplyContent("");
      setReplyTargetId(null);
    } catch (error) {
      console.error(error);
      setMessage("답글 작성에 실패했습니다. 로그인이 필요할 수 있습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDeletePost(boardNo: number) {
    const confirmed = window.confirm("댓글을 삭제하시겠습니까?");

    if (!confirmed) {
      return;
    }

    try {
      await deleteBoardPost(boardNo);

      setPosts((prev) =>
        prev.filter((post) => post.boardNo !== boardNo && post.parentId !== boardNo)
      );
    } catch (error) {
      console.error(error);
      setMessage("댓글 삭제에 실패했습니다.");
    }
  }

  async function handleToggleLike(post: BoardPost) {
    try {
      setMessage(null);

      const updatedPost = post.likedByMe
        ? await unlikeBoardPost(post.boardNo)
        : await likeBoardPost(post.boardNo);

      setPosts((prev) => replacePost(prev, updatedPost));
    } catch (error) {
      console.error(error);
      setMessage("좋아요 처리에 실패했습니다. 로그인이 필요할 수 있습니다.");
    }
  }

  function openReplyForm(parentId: number) {
    setMessage(null);

    if (replyTargetId === parentId) {
      setReplyTargetId(null);
      setReplyContent("");
      return;
    }

    setReplyTargetId(parentId);
    setReplyContent("");
  }

  useEffect(() => {
    void fetchPosts();
  }, [fetchPosts]);

  return (
    <section className={styles.communityPanel}>
      <div className={styles.communityHeader}>
        <div>
          <h2>종목 커뮤니티</h2>
          <p>이 종목에 대한 의견을 댓글로 남겨보세요.</p>
        </div>

        <button
          type="button"
          className={styles.communityRefreshButton}
          onClick={() => void fetchPosts()}
          disabled={loading}
        >
          {loading ? "불러오는 중" : "새로고침"}
        </button>
      </div>

      <div className={styles.communityWriteBox}>
        <textarea
          value={content}
          onChange={(event) => setContent(event.target.value)}
          placeholder="이 종목에 대한 의견을 작성해보세요."
          maxLength={500}
          rows={2}
        />

        <div className={styles.communityWriteFooter}>
          <span>{content.length.toLocaleString()} / 500</span>

          <button
            type="button"
            onClick={() => void handleCreateComment()}
            disabled={submitting || content.trim().length === 0}
          >
            {submitting ? "등록 중..." : "댓글 등록"}
          </button>
        </div>
      </div>

      {message ? <p className={styles.communityMessage}>{message}</p> : null}

      {loading ? (
        <div className={styles.communityEmpty}>댓글을 불러오는 중입니다.</div>
      ) : parentComments.length === 0 ? (
        <div className={styles.communityEmpty}>
          아직 작성된 댓글이 없습니다.
        </div>
      ) : (
        <div className={styles.communityList}>
          {parentComments.map((comment) => {
            const replies = repliesByParentId.get(comment.boardNo) ?? [];

            return (
              <article key={comment.boardNo} className={styles.commentBlock}>
                <div className={styles.communityItem}>
                  <div className={styles.communityItemHeader}>
                    <div>
                      <strong>{getWriterName(comment)}</strong>
                      <span>{formatDate(comment.createdAt)}</span>
                    </div>

                    <button
                      type="button"
                      onClick={() => void handleDeletePost(comment.boardNo)}
                    >
                      삭제
                    </button>
                  </div>

                  <p>{comment.content}</p>

                  <div className={styles.communityItemFooter}>
                    <button
                      type="button"
                      className={comment.likedByMe ? styles.likedButton : ""}
                      onClick={() => void handleToggleLike(comment)}
                    >
                      좋아요 {comment.likeCount ?? 0}
                    </button>

                    <button
                      type="button"
                      onClick={() => openReplyForm(comment.boardNo)}
                    >
                      답글
                    </button>
                  </div>
                </div>

                {replyTargetId === comment.boardNo ? (
                  <div className={styles.replyWriteBox}>
                    <textarea
                      value={replyContent}
                      onChange={(event) => setReplyContent(event.target.value)}
                      placeholder="답글을 작성해보세요."
                      maxLength={500}
                      rows={2}
                    />

                    <div className={styles.communityWriteFooter}>
                      <span>{replyContent.length.toLocaleString()} / 500</span>

                      <div className={styles.replyButtonGroup}>
                        <button
                          type="button"
                          className={styles.cancelButton}
                          onClick={() => {
                            setReplyTargetId(null);
                            setReplyContent("");
                          }}
                        >
                          취소
                        </button>

                        <button
                          type="button"
                          onClick={() => void handleCreateReply(comment.boardNo)}
                          disabled={
                            submitting || replyContent.trim().length === 0
                          }
                        >
                          답글 등록
                        </button>
                      </div>
                    </div>
                  </div>
                ) : null}

                {replies.length > 0 ? (
                  <div className={styles.replyList}>
                    {replies.map((reply) => (
                      <article key={reply.boardNo} className={styles.replyItem}>
                        <div className={styles.communityItemHeader}>
                          <div>
                            <strong>{getWriterName(reply)}</strong>
                            <span>{formatDate(reply.createdAt)}</span>
                          </div>

                          <button
                            type="button"
                            onClick={() => void handleDeletePost(reply.boardNo)}
                          >
                            삭제
                          </button>
                        </div>

                        <p>{reply.content}</p>

                        <div className={styles.communityItemFooter}>
                          <button
                            type="button"
                            className={reply.likedByMe ? styles.likedButton : ""}
                            onClick={() => void handleToggleLike(reply)}
                          >
                            좋아요 {reply.likeCount ?? 0}
                          </button>
                        </div>
                      </article>
                    ))}
                  </div>
                ) : null}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}