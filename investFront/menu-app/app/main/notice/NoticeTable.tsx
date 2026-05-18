"use client";

import { useRouter } from "next/navigation";
import styles from "./NoticePage.module.css";

type NoticeItem = {
  noticeId: number;
  title: string;
  createdAt: string;
  updatedAt: string;
};

function formatDate(
  dateString?: string | null
) {

  if (!dateString) {
    return "-";
  }

  const date = new Date(dateString);

  if (Number.isNaN(date.getTime())) {
    return "-";
  }

  return new Intl.DateTimeFormat(
    "ko-KR",
    {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }
  ).format(date);
}

export default function NoticeTable({
  notices,
}: {
  notices: NoticeItem[];
}) {

  const router = useRouter();

  return (
    <table className={styles.table}>
      <thead>
        <tr>
          <th className={styles.numberCol}>
            번호
          </th>

          <th className={styles.titleCol}>
            제목
          </th>

          <th className={styles.dateCol}>
            작성일
          </th>

          <th className={styles.dateCol}>
            수정일
          </th>
        </tr>
      </thead>

      <tbody>
        {notices.map((notice) => (
          <tr
            key={notice.noticeId}
            className={styles.row}
            onClick={() =>
              router.push(
                `/main/notice/${notice.noticeId}`
              )
            }
            tabIndex={0}
            onKeyDown={(e) => {
              if (
                e.key === "Enter" ||
                e.key === " "
              ) {
                router.push(
                  `/main/notice/${notice.noticeId}`
                );
              }
            }}
          >
            <td>
              {notice.noticeId}
            </td>

            <td className={styles.titleCell}>
              {notice.title}
            </td>

            <td>
              {formatDate(
                notice.createdAt
              )}
            </td>

            <td>
              {formatDate(
                notice.updatedAt
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}