import Link from "next/link";
import { notFound } from "next/navigation";
import styles from "./NoticeDetailPage.module.css";
import { API_BASE_URL } from "@/lib/api-base";
import NoticeAdminActions from "./NoticeAdminActions";

type NoticeDetail = {
  noticeId: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

async function getNoticeDetail(
  noticeId: string
): Promise<NoticeDetail> {

  const response = await fetch(
    `${API_BASE_URL}/notice/${noticeId}`,
    {
      cache: "no-store",
    }
  );

  if (response.status === 404) {
    notFound();
  }

  if (!response.ok) {
    throw new Error("공지 조회 실패");
  }

  return response.json();
}

function formatDate(dateString: string) {
  return new Intl.DateTimeFormat(
    "ko-KR",
    {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    }
  ).format(new Date(dateString));
}

export default async function NoticeDetailPage({
  params,
}: {
  params: Promise<{
    noticeId: string;
  }>;
}) {
  const resolvedParams = await params;
  const notice = await getNoticeDetail(resolvedParams.noticeId);

  return (
    <main className={styles.page}>
      <div className={styles.container}>
        <div className={styles.header}>
          <div className={styles.breadcrumb}>
            공지사항
          </div>
          <h1 className={styles.title}>
            {notice.title}
          </h1>

          <div className={styles.date}>
            작성일 : 
            {formatDate(notice.createdAt)}
            <br/>
            수정일 : 
            {formatDate(notice.updatedAt)}
          </div>
        </div>

        <div className={styles.content}>
          {notice.content}
        </div>

        <NoticeAdminActions noticeId={notice.noticeId}/>
      </div>
    </main>
  );
}