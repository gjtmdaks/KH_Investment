import Link from "next/link";
import styles from "./signUp.module.css";
import Image from "next/image";
export default function SignUpPage() {
  return (
    <>  
      <div>
        <Link href="/main" className={styles.logoArea}>
          <Image
            src="/logo-full.png"
            alt="KH 증권 로고"
            width={132}
            height={33}
            className={styles.logoImage}
            priority
          />
        </Link>
      </div>
        <div>회원가입 페이지</div>
    </>
    );
}