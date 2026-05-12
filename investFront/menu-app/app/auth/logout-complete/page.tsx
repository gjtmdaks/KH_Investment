"use client";

import { useEffect } from "react";

import { clearLoginStorage } from "@/lib/auth-user";

export default function LogoutCompleteRedirectPage() {
  useEffect(() => {
    clearLoginStorage();
    window.location.replace("/main?auth=logged_out");
  }, []);

  return null;
}
