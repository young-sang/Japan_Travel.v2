import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import IconButton from '../components/ui/IconButton.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import { SkeletonCard } from '../components/ui/Skeleton.jsx';
import styles from './Home.module.css';

const SLIDES = [
  { bg: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)', cat: '추천', title: "LET'S GO JAPAN", sub: '액티비티 편' },
  { bg: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)', cat: '계절', title: 'JAPAN IN WINTER', sub: '겨울 여행' },
  { bg: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)', cat: '봄', title: 'CHERRY BLOSSOM', sub: '봄 벚꽃 명소' },
  { bg: 'linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%)', cat: '문화', title: 'TRADITIONAL', sub: '전통 마츠리' },
  { bg: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)', cat: '미식', title: 'FOODIE JOURNEY', sub: '미식 여행' },
  { bg: 'linear-gradient(135deg, #cfd9df 0%, #e2ebf0 100%)', cat: '도시', title: 'CITY LIGHTS', sub: '도시 야경' },
];

const QUICK = [
  { emoji: '🗾', label: '일본 관광 100선', to: '/theme', badge: '100' },
  { emoji: '🔥', label: '핫플레이스', to: '/destination' },
  { emoji: '🎉', label: '축제', to: '/festival' },
  { emoji: '🗺️', label: '여행코스', to: '/course' },
  { emoji: '💡', label: '여행 팁', to: '/theme' },
];

export default function Home() {
  const [idx, setIdx] = useState(0);
  const [paused, setPaused] = useState(false);
  const [popular, setPopular] = useState(null);

  useEffect(() => {
    if (paused) return;
    const t = setInterval(() => setIdx((i) => (i + 1) % SLIDES.length), 5000);
    return () => clearInterval(t);
  }, [paused]);

  useEffect(() => {
    api.listCourses()
      .then((r) => setPopular((r || []).slice(0, 6)))
      .catch(() => setPopular([]));
  }, []);

  return (
    <>
      <section
        className={styles.hero}
        aria-roledescription="carousel"
        aria-label="추천 여행 슬라이드"
        onMouseEnter={() => setPaused(true)}
        onMouseLeave={() => setPaused(false)}
      >
        <div className={styles.slidesWrap} style={{ transform: `translateX(-${idx * 100}%)` }}>
          {SLIDES.map((s, i) => (
            <div key={i} className={styles.slide} style={{ background: s.bg }} aria-hidden={i !== idx}>
              <div className={styles.slideContent}>
                <p className={styles.slideCat}>{s.cat}</p>
                <h2 className={styles.slideTitle}>{s.title}</h2>
                <p className={styles.slideSub}>{s.sub}</p>
              </div>
            </div>
          ))}
        </div>
        <div className={styles.heroNav}>
          <IconButton label="이전 슬라이드" variant="overlay" onClick={() => setIdx((i) => (i - 1 + SLIDES.length) % SLIDES.length)}>‹</IconButton>
          <IconButton label="다음 슬라이드" variant="overlay" onClick={() => setIdx((i) => (i + 1) % SLIDES.length)}>›</IconButton>
        </div>
        <div className={styles.dots} role="tablist">
          {SLIDES.map((_, i) => (
            <button
              key={i}
              type="button"
              role="tab"
              aria-label={`${i + 1}번째 슬라이드`}
              aria-selected={i === idx}
              className={`${styles.dot} ${i === idx ? styles.dotActive : ''}`}
              onClick={() => setIdx(i)}
            />
          ))}
        </div>
      </section>

      <Container>
        <section className={styles.quickSection} aria-label="빠른 메뉴">
          <div className={styles.quickGrid}>
            {QUICK.map((q) => (
              <Link key={q.label} to={q.to} className={styles.quickItem}>
                <span className={styles.quickIcon}>{q.emoji}</span>
                <span className={styles.quickLabel}>{q.label}</span>
                {q.badge && <span className={styles.quickBadge}>{q.badge}</span>}
              </Link>
            ))}
          </div>
        </section>

        <section className={styles.section} aria-label="인기 여행코스">
          <header className={styles.sectionHead}>
            <h2 className={styles.sectionTitle}>인기 여행코스</h2>
            <Link to="/course" className={styles.sectionMore}>전체 보기 →</Link>
          </header>
          <div className={styles.cardGrid}>
            {popular === null && Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
            {popular !== null && popular.length === 0 && (
              <p className={styles.empty}>아직 인기 코스가 없어요.</p>
            )}
            {popular !== null && popular.map((c) => (
              <PlaceCard key={c.id} item={c} type="course" />
            ))}
          </div>
        </section>
      </Container>
    </>
  );
}
