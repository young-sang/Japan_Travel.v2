import { Link } from 'react-router-dom';
import styles from './Footer.module.css';

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.inner}>
        <div className={styles.brand}>
          <span className={styles.logo}>일본 구석구석</span>
          <span className={styles.tagline}>일본 여행 가이드 · 학습용 프로젝트</span>
        </div>
        <nav className={styles.links} aria-label="푸터 네비게이션">
          <Link to="/destination">여행지</Link>
          <Link to="/festival">축제</Link>
          <Link to="/course">여행코스</Link>
        </nav>
        <div className={styles.meta}>
          <span>데이터 출처: Wikipedia / 공개 자료</span>
          <span className={styles.copy}>© {new Date().getFullYear()} 일본 구석구석</span>
        </div>
      </div>
    </footer>
  );
}
