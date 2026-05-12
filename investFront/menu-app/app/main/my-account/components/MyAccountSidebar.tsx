"use client";

import styles from "../myAccount.module.css";

export type MyAccountMenu = "asset" | "trades" | "orders" | "manage";

type Props = {
  activeMenu: MyAccountMenu;
  onChangeMenu: (menu: MyAccountMenu) => void;
};

const menus: {
  id: MyAccountMenu;
  label: string;
  description: string;
}[] = [
  {
    id: "asset",
    label: "자산",
    description: "보유 현금과 주식",
  },
  {
    id: "trades",
    label: "거래내역",
    description: "체결된 매매 내역",
  },
  {
    id: "orders",
    label: "주문내역",
    description: "요청한 주문 상태",
  },
  {
    id: "manage",
    label: "계좌관리",
    description: "계좌 상태 및 설정",
  },
];

export default function MyAccountSidebar({
  activeMenu,
  onChangeMenu,
}: Props) {
  return (
    <aside className={styles.sidebar}>
      <div className={styles.sidebarTitleArea}>
        <p>MY ACCOUNT</p>
        <h2>내 계좌</h2>
      </div>

      <nav className={styles.sideNav}>
        {menus.map((menu) => (
          <button
            key={menu.id}
            type="button"
            className={`${styles.sideButton} ${
              activeMenu === menu.id ? styles.active : ""
            }`}
            onClick={() => onChangeMenu(menu.id)}
          >
            <strong>{menu.label}</strong>
            <span>{menu.description}</span>
          </button>
        ))}
      </nav>
    </aside>
  );
}