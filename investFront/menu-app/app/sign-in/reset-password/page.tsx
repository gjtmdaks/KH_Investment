"use client";

import { useState } from "react";

export default function ResetPasswordPage() {

  const [userId, setUserId] = useState("");
  const [userName, setUserName] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const handleReset = async () => {

    const response = await fetch(
  "http://localhost:8081/final/api/users/reset-password",
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

    const data = await response.json();

    if(data.success){
      alert("비밀번호 변경 완료");
    } else {
      alert("회원 정보가 일치하지 않습니다.");
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

      <button onClick={handleReset}>
        비밀번호 변경
      </button>
    </div>
  );
}