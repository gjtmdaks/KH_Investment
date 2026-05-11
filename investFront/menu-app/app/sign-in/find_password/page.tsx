"use client";

import { useState } from "react";

export default function ResetPasswordPage() {

  const [userId, setUserId] = useState("");
  const [userName, setUserName] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const handleReset = async () => {

    try {

      const response = await fetch(
        "http://localhost:8080/users/find_password",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            userId,
            userName,
            newPassword,
          }),
        }
      );

      console.log("응답 상태:", response.status);

      const data = await response.json();

      console.log(data);

      if (data.success) {
        alert("비밀번호 변경 완료");
      } else {
        alert(data.message || "실패");
      }

    } catch (error) {

      console.error("fetch 에러:", error);

      alert("서버 연결 실패");
    }
  };

  return (
    <div>
      <h1>비밀번호 찾기</h1>

      <input
        placeholder="아이디"
        value={userId}
        onChange={(e) => setUserId(e.target.value)}
      />

      <input
        placeholder="이름"
        value={userName}
        onChange={(e) => setUserName(e.target.value)}
      />

      <input
        type="password"
        placeholder="새 비밀번호"
        value={newPassword}
        onChange={(e) => setNewPassword(e.target.value)}
      />

      <button type="button" onClick={handleReset}>
        비밀번호 변경
      </button>
    </div>
  );
}