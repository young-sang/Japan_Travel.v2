import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import styles from './News.module.css';
import usePageTitle from '../utils/usePageTitle.js';
import formatDate from '../utils/formatDate.js';

//  졸업작품 뉴스 레이아웃 — 최신 1건은 큰 카드, 나머지는 작은 카드 그리드.
//  내용은 관리자가 직접 쓴다 (D-075 · D-079)
export default function News() {
  usePageTitle('뉴스');
  const [items, setItems] = useState(null);

  useEffect(() => {
    api.listNews()
      .then((r) => setItems(r || []))
      .catch(() => setItems([]));
  }, []);

  if (items === null) return <p className={styles.empty}>뉴스를 불러오는 중…</p>;
  if (items.length === 0) return <p className={styles.empty}>아직 올라온 뉴스가 없어요.</p>;

  const [main, ...subs] = items;
  return (
    <div className={styles.container}>
      <Link to={`/news/${main.id}`} className={`${styles.card} ${styles.mainCard}`}>
        <div className={styles.cardImage}>
          <img src={main.imagePath || '/placeholder.svg'} alt="" />
        </div>
        <h2 className={styles.cardTitle}>{main.title}</h2>
        <p className={styles.cardDate}>{formatDate(main.createdAt)}</p>
        <p className={styles.cardDesc}>{main.excerpt}</p>
      </Link>

      {subs.length > 0 && (
        <div className={styles.subGrid}>
          {subs.map((n) => (
            <Link key={n.id} to={`/news/${n.id}`} className={`${styles.card} ${styles.subCard}`}>
              <div className={styles.cardImage}>
                <img src={n.imagePath || '/placeholder.svg'} alt="" loading="lazy" />
              </div>
              <h3 className={styles.cardTitle}>{n.title}</h3>
              <p className={styles.cardDate}>{formatDate(n.createdAt)}</p>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
