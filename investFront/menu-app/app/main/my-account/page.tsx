"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import styles from "./myAccount.module.css";
import MyAccountSidebar, {
  type MyAccountMenu,
} from "./components/MyAccountSidebar";
import AssetPanel from "./components/AssetPanel";
import TradesPanel from "./components/TradesPanel";
import OrdersPanel from "./components/OrdersPanel";
import AccountManagePanel from "./components/AccountManagePanel";
import { getCurrentUser, type LoginUser } from "@/lib/auth-user";

export default function MyAccountPage() {
  const router = useRouter();

  const [activeMenu, setActiveMenu] = useState<MyAccountMenu>("asset");
  const [user, setUser] = useState<LoginUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function checkLogin() {
      try {
        const currentUser = await getCurrentUser();

        if (!currentUser) {
          alert("로그인이 필요한 페이지입니다.");
          router.replace("/sign-in");
          return;
        }

        setUser(currentUser);
      } finally {
        setLoading(false);
      }
    }

    checkLogin();
  }, [router]);

  if (loading) {
    return (
      <main className={styles.page}>
        <section className={styles.loadingCard}>
          <h1>내 계좌 정보를 불러오는 중입니다.</h1>
          <p>잠시만 기다려주세요.</p>
        </section>
      </main>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <main className={styles.page}>
      <MyAccountSidebar
        activeMenu={activeMenu}
        onChangeMenu={setActiveMenu}
      />

      <section className={styles.content}>
        <div className={styles.pageTitleArea}>
          <p className={styles.pageLabel}>MY ACCOUNT</p>
          <h1 className={styles.pageTitle}>내 계좌</h1>
          <p className={styles.pageDesc}>
            보유 자산, 주문 내역, 체결 내역을 확인할 수 있습니다.
          </p>
        </div>

        {activeMenu === "asset" && <AssetPanel />}
        {activeMenu === "trades" && <TradesPanel />}
        {activeMenu === "orders" && <OrdersPanel />}
        {activeMenu === "manage" && <AccountManagePanel user={user} />}
      </section>
    </main>
  );
}