import styles from "../MainSidebar.module.css";

interface Props {
  title: string;
  description: string;
}

export default function SidebarSectionTitle({
  title,
  description,
}: Props) {
  return (
    <div className={styles.sectionTitle}>
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}