"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import {
  getAdminUsers,
  updateAdminUserAccountStatus,
  updateAdminUserStatus,
  type AdminUser,
  type AdminUserListResponse,
} from "@/lib/admin-users";

import styles from "./UserManagement.module.css";

type LoginUser = {
  userNo?: number;
  userName?: string;
  email?: string;
  auth?: number;
};

const initialData: AdminUserListResponse = {
  users: [],
  totalCount: 0,
  activeCount: 0,
  stopCount: 0,
  deleteCount: 0,
};

function getLoginUser(): LoginUser | null {
  if (typeof window === "undefined") {
    return null;
  }
  
  const userText =
    localStorage.getItem("kh_user") ||
    localStorage.getItem("loginUser") ||
    localStorage.getItem("user") ||
    localStorage.getItem("authUser");

  if (!userText) {
    return null;
  }

  try {
    return JSON.parse(userText);
  } catch {
    return null;
  }
}

function formatDate(value: string) {
  if (!value) return "-";

  return new Date(value).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

function getDateAfterDays(days: number) {
  const date = new Date();
  date.setDate(date.getDate() + days);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

export default function UserManagementPage() {
  const router = useRouter();

  const [authChecked, setAuthChecked] = useState(false);
  const [data, setData] = useState<AdminUserListResponse>(initialData);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("ALL");
  const [loading, setLoading] = useState(false);
  const [selectedUser, setSelectedUser] = useState<AdminUser | null>(null);
  const [stopEndAt, setStopEndAt] = useState("");
  const [customStopDays, setCustomStopDays] = useState("");

  const users = data.users ?? [];

  useEffect(() => {
    const user = getLoginUser();

    if (!user) {
      alert("잘못된 url입니다.");
      router.replace("/main/stock");
      return;
    }

    if (user.auth !== 1) {
      alert("잘못된 url입니다.");
      router.replace("/main/stock");
      return;
    }

    setAuthChecked(true);
  }, [router]);

  useEffect(() => {
    if (authChecked) {
      loadUsers();
    }
  }, [authChecked]);

  async function loadUsers() {
    try {
      setLoading(true);

      const result = await getAdminUsers({
        keyword,
        status,
      });

      setData({
        users: result.users ?? [],
        totalCount: result.totalCount ?? 0,
        activeCount: result.activeCount ?? 0,
        stopCount: result.stopCount ?? 0,
        deleteCount: result.deleteCount ?? 0,
      });
    } catch (error) {
      console.error(error);
      alert("회원 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }

  function handleSearch() {
    loadUsers();
  }

  function handleSelectStopDays(days: number) {
    setStopEndAt(getDateAfterDays(days));
    setCustomStopDays("");
  }

  function handleCustomStopDaysChange(value: string) {
    setCustomStopDays(value);

    const days = Number(value);

    if (!Number.isInteger(days) || days <= 0) {
      setStopEndAt("");
      return;
    }

    setStopEndAt(getDateAfterDays(days));
  }

  async function handleUpdateAccountStatus(
    userNo: number,
    status: "ACTIVE" | "STOP" | "CLOSE"
  ) {
    if (status === "STOP" && !stopEndAt) {
      alert("거래 정지 기간을 선택해주세요.");
      return;
    }

    const message =
      status === "STOP"
        ? `${stopEndAt}까지 해당 회원의 거래를 정지하시겠습니까?`
        : "해당 회원의 거래 정지를 해제하시겠습니까?";

    if (!confirm(message)) {
      return;
    }

    try {
      await updateAdminUserAccountStatus(
        userNo,
        status,
        status === "STOP" ? stopEndAt : undefined
      );

      alert("계좌 상태가 변경되었습니다.");
      setSelectedUser(null);
      setStopEndAt("");
      setCustomStopDays("");
      loadUsers();
    } catch (error) {
      console.error(error);
      alert("계좌 상태 변경에 실패했습니다.");
    }
  }

  async function handleUpdateUserStatus(
    userNo: number,
    status: "ACTIVE" | "DELETE"
  ) {
    const message =
      status === "DELETE"
        ? "정말 해당 회원을 삭제 처리하시겠습니까?"
        : "해당 회원을 복구하시겠습니까?";

    if (!confirm(message)) {
      return;
    }

    try {
      await updateAdminUserStatus(userNo, status);

      alert("회원 상태가 변경되었습니다.");
      setSelectedUser(null);
      loadUsers();
    } catch (error) {
      console.error(error);
      alert("회원 상태 변경에 실패했습니다.");
    }
  }

  if (!authChecked) {
    return null;
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
            </tr>
          </thead>

          <tbody>
            {users.length === 0 ? (
              <tr>
                <td colSpan={8} className={styles.emptyCell}>
                  {loading
                    ? "회원 목록을 불러오는 중입니다."
                    : "조회된 회원이 없습니다."}
                </td>
              </tr>
            ) : (
              users.map((user) => (
                <tr
                  key={user.userNo}
                  className={styles.clickableRow}
                  onClick={() => setSelectedUser(user)}
                >
                  <td>{user.userNo}</td>
                  <td>{user.email ?? "-"}</td>
                  <td>{user.userName ?? "-"}</td>
                  <td>{user.phone ?? "-"}</td>
                  <td>{user.provider ?? "-"}</td>
                  <td>{user.authName ?? "-"}</td>
                  <td>
                    <span
                      className={`${styles.statusBadge} ${
                        styles[`status${user.status}`] ?? ""
                      }`}
                    >
                      {user.statusName ?? user.status}
                    </span>
                  </td>
                  <td>{formatDate(user.createdAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>
      {selectedUser && (
        <div className={styles.modalOverlay} onClick={() => setSelectedUser(null)}>
          <section
            className={styles.userModal}
            onClick={(e) => e.stopPropagation()}
          >
            <div className={styles.modalHeader}>
              <div>
                <p className={styles.modalSubTitle}>회원 상태 관리</p>
                <h2 className={styles.modalTitle}>
                  {selectedUser.userName ?? "이름 없음"}
                </h2>
              </div>

              <button
                className={styles.closeButton}
                onClick={() => setSelectedUser(null)}
              >
                ×
              </button>
            </div>

            <div className={styles.modalInfoGrid}>
              <div>
                <span>회원번호</span>
                <strong>{selectedUser.userNo}</strong>
              </div>

              <div>
                <span>이메일</span>
                <strong>{selectedUser.email ?? "-"}</strong>
              </div>

              <div>
                <span>전화번호</span>
                <strong>{selectedUser.phone ?? "-"}</strong>
              </div>

              <div>
                <span>현재 상태</span>
                <strong>{selectedUser.statusName ?? selectedUser.status}</strong>
              </div>
            </div>

                        <p className={styles.warningText}>
              회원 상태를 변경하면 해당 회원의 서비스 이용 가능 여부가 변경됩니다.
            </p>

            <div className={styles.modalActions}>
              {selectedUser.status === "ACTIVE" && (
                <>
                  <div className={styles.stopPeriodBox}>
                  <p className={styles.stopPeriodTitle}>거래 정지 기간</p>

                  <div className={styles.stopPeriodButtons}>
                    <button
                      type="button"
                      className={styles.periodButton}
                      onClick={() => handleSelectStopDays(1)}
                    >
                      1일
                    </button>

                    <button
                      type="button"
                      className={styles.periodButton}
                      onClick={() => handleSelectStopDays(7)}
                    >
                      7일
                    </button>

                    <button
                      type="button"
                      className={styles.periodButton}
                      onClick={() => handleSelectStopDays(30)}
                    >
                      30일
                    </button>

                    <input
                      className={styles.periodInput}
                      type="number"
                      min="1"
                      placeholder="직접 입력"
                      value={customStopDays}
                      onChange={(e) => handleCustomStopDaysChange(e.target.value)}
                    />
                  </div>

                  <p className={styles.stopEndText}>
                    {stopEndAt
                      ? `정지 종료일: ${stopEndAt}`
                      : "정지 기간을 선택해주세요."}
                  </p>
                </div>

                  <button
                    className={styles.warningButton}
                    onClick={() =>
                      handleUpdateAccountStatus(selectedUser.userNo, "STOP")
                    }
                  >
                    거래 정지
                  </button>

                  <button
                    className={styles.dangerButton}
                    onClick={() =>
                      handleUpdateUserStatus(selectedUser.userNo, "DELETE")
                    }
                  >
                    삭제
                  </button>
                </>
              )}

              {selectedUser.status === "STOP" && (
                <>
                  <button
                    className={styles.primaryButton}
                    onClick={() =>
                      handleUpdateAccountStatus(selectedUser.userNo, "ACTIVE")
                    }
                  >
                    거래 정지 해제
                  </button>

                  <button
                    className={styles.dangerButton}
                    onClick={() =>
                      handleUpdateUserStatus(selectedUser.userNo, "DELETE")
                    }
                  >
                    삭제
                  </button>
                </>
              )}

              {selectedUser.status === "DELETE" && (
                <button
                  className={styles.primaryButton}
                  onClick={() =>
                    handleUpdateUserStatus(selectedUser.userNo, "ACTIVE")
                  }
                >
                  복구
                </button>
              )}

              <button
                className={styles.cancelButton}
                onClick={() => {
                  setSelectedUser(null);
                  setStopEndAt("");
                }}
              >
                닫기
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  );
}