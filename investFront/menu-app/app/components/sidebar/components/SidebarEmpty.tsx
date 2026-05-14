import styles from "../MainSidebar.module.css";

interface Props {
  text: string;
}

export default function SidebarEmpty({
  text,
}: Props) {
  return (
    <div className={styles.emptyBox}>
      <div className={styles.emptyIcon}>
        📊
      </div>
      <p>{text}</p>
    </div>
  );
}