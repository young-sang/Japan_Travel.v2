import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import styles from './Top25.module.css';
import usePageTitle from '../utils/usePageTitle.js';

//  졸업작품 Top100 디자인(순번 · 이름 · 설명 상자)에 사진과 상세 링크를 더했다.
//  순위는 하드코딩하지 않고 백엔드 종합 점수로 받는다 (D-074 · D-076)
export default function Top25() {
  usePageTitle('일본 여행지 TOP 25');
  const [items, setItems] = useState(null);

  useEffect(() => {
    api.destinationRanking()
      .then((r) => setItems(r || []))
      .catch(() => setItems([]));
  }, []);

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <div className={styles.titleBox}>
          <h1>일본 여행지 TOP 25</h1>
        </div>
        <p className={styles.subtitle}>
          여행자들이 많이 둘러보고, 찜하고, 리뷰를 남긴 여행지 순입니다. (본 사람 1점 · 찜 2점 · 리뷰 3점)
        </p>

        <div className={styles.scrollArea}>
          {items === null && <p className={styles.empty}>순위를 불러오는 중…</p>}
          {items !== null && items.length === 0 && (
            <p className={styles.empty}>아직 순위를 매길 만큼 기록이 없어요.</p>
          )}
          {items !== null && items.map(({ rank, favoriteCount, destination: d }) => (
            <Link key={d.id} to={`/detail/destination/${d.id}`} className={styles.item}>
              <div className={styles.row}>
                <div className={styles.thumb}>{rank}</div>
                <img
                  className={styles.photo}
                  src={d.imagePath || '/placeholder.svg'}
                  alt={d.name}
                  title={d.imageCredit || undefined}
                  loading="lazy"
                />
                <div className={styles.content}>
                  <div className={styles.name}>{d.name}</div>
                  <div className={styles.meta}>
                    {d.prefecture}
                    {d.avgRating != null && <> · ★ {d.avgRating} ({d.reviewCount})</>}
                    {favoriteCount > 0 && <> · ♥ {favoriteCount}</>}
                  </div>
                  {d.description && <div className={styles.descBox}>{d.description}</div>}
                </div>
              </div>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}
