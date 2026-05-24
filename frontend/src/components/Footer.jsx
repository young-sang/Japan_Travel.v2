import styles from './Footer.module.css';

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.inner}>
        <div className={styles.brand}>
          <span className={styles.logo}>Japan Travel</span>
          <span className={styles.tagline}>일본 여행 가이드 · 학습용 프로젝트</span>
        </div>
        <nav className={styles.links} aria-label="푸터 네비게이션">
          <a href="/theme">테마</a>
          <a href="/destination">여행지</a>
          <a href="/festival">축제</a>
          <a href="/course">코스</a>
        </nav>
        <div className={styles.meta}>
          <span>데이터 출처: Wikipedia / 공개 자료</span>
          <span className={styles.copy}>© {new Date().getFullYear()} Japan Travel v2</span>
        </div>
      </div>
    </footer>
  );
}
