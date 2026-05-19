"use client";

import { useEffect, useState } from "react";

import {
  getAdminUsers,
  type AdminUser,
  type AdminUserListResponse,
} from "@/lib/admin-users";

import styles from "./UserManagement.module.css";

const initialData: AdminUserListResponse = {
  users: [],
  totalCount: 0,
  activeCount: 0,
  stopCount: 0,
  deleteCount: 0,
};

export default function UserManagementPage() {
  const [data, setData] = useState<AdminUserListResponse>(initialData);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("ALL");
  const [loading, setLoading] = useState(false);
  const users = data.users ?? [];

  async function loadUsers() {
    try {
      setLoading(true);

      const result = await getAdminUsers({
        keyword,
        status,
      });

      setData(result);
    } catch (error) {
      console.error(error);
      alert("회원 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  function handleSearch() {
    loadUsers();
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.subTitle}>관리자 페이지</p>
          <h1 className={styles.title}>회원 관리</h1>
        </div>

        <button
          className={styles.primaryButton}
          onClick={loadUsers}
          disabled={loading}
        >
          {loading ? "조회 중..." : "회원 새로고침"}
        </button>
      </section>

      <section className={styles.searchSection}>
        <input
          className={styles.searchInput}
          placeholder="이메일, 이름, 전화번호로 검색"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              handleSearch();
            }
          }}
        />

        <select
          className={styles.selectBox}
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          <option value="ALL">전체 상태</option>
          <option value="ACTIVE">정상</option>
          <option value="STOP">정지</option>
          <option value="DELETE">탈퇴</option>
        </select>

        <button
          className={styles.primaryButton}
          onClick={handleSearch}
          disabled={loading}
        >
          검색
        </button>
      </section>

      <section className={styles.summaryGrid}>
        <div className={styles.summaryCard}>
          <span>전체 회원</span>
          <strong>{data.totalCount}</strong>
        </div>

        <div className={styles.summaryCard}>
          <span>정상 회원</span>
          <strong>{data.activeCount}</strong>
        </div>

        <div className={styles.summaryCard}>
          <span>정지 회원</span>
          <strong>{data.stopCount}</strong>
        </div>
      </section>

      <section className={styles.tableSection}>
        <table className={styles.userTable}>
          <thead>
            <tr>
              <th>회원번호</th>
              <th>이메일</th>
              <th>이름</th>
              <th>전화번호</th>
              <th>가입방식</th>
              <th>권한</th>
              <th>상태</th>
              <th>가입일</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            {users.length === 0 ? (
              <tr>
                <td colSpan={9} className={styles.emptyCell}>
                  {loading ? "회원 목록을 불러오는 중입니다." : "조회된 회원이 없습니다."}
                </td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.userNo}>
                  <td>{user.userNo}</td>
                  <td>{user.email ?? "-"}</td>
                  <td>{user.userName ?? "-"}</td>
                  <td>{user.phone ?? "-"}</td>
                  <td>{user.provider ?? "-"}</td>
                  <td>{user.authName}</td>
                  <td>
                    <span
                      className={`${styles.statusBadge} ${
                        styles[`status${user.status}`]
                      }`}
                    >
                      {user.statusName}
                    </span>
                  </td>
                  <td>{formatDate(user.createdAt)}</td>
                  <td>
                    <button className={styles.tableButton}>상세</button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>
    </main>
  );
}

function formatDate(value: string) {
  if (!value) return "-";

  return new Date(value).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}