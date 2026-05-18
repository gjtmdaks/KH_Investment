import { notFound } from "next/navigation";

import styles from "./NoticePage.module.css";
import { API_BASE_URL } from "@/lib/api-base";

import NoticeWriteButton from "./NoticeWriteButton";
import NoticeTable from "./NoticeTable";
import Link from "next/link";

type NoticeItem = {
  noticeId: number;
  title: string;
  createdAt: string;
  updatedAt: string;
};

type NoticePageResponse = {
  notices: NoticeItem[];
  currentPage: number;
  totalPage: number;
  totalCount: number;
};

async function getNoticeList(
  page: number
): Promise<NoticePageResponse> {

  const response = await fetch(
    `${API_BASE_URL}/notice?page=${page}`,
    {
      cache: "no-store",
    }
  );

  if (!response.ok) {
    throw new Error("공지사항 조회 실패");
  }

  return response.json();
}

export default async function NoticePage({
  searchParams,
}: {
  searchParams: Promise<{
    page?: string;
  }>;
}) {

  const resolvedSearchParams = await searchParams;

  const currentPage = Number(
    resolvedSearchParams.page ?? "1"
  );

  if (
    Number.isNaN(currentPage) ||
    currentPage < 1
  ) {
    notFound();
  }

  const data = await getNoticeList(currentPage);

  return (
    <main className={styles.page}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h1 className={styles.title}>
            공지사항
          </h1>

          <NoticeWriteButton />
        </div>

        <NoticeTable
          notices={data.notices}
        />

        <div className={styles.pagination}>
          {Array.from({
            length: data.totalPage,
          }).map((_, index) => {

            const page = index + 1;

            return (
              <Link
                key={page}
                href={`/main/notice?page=${page}`}
                className={
                  page === currentPage
                    ? styles.activePage
                    : styles.pageButton
                }
              >
                {page}
              </Link>
            );
          })}
        </div>
      </div>
    </main>
  );
}