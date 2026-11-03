import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import IconButton from '../components/ui/IconButton.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import { SkeletonCard } from '../components/ui/Skeleton.jsx';
import styles from './Home.module.css';

//  배너 사진은 Wikimedia Commons 의 자유 라이선스 사진이다. CC BY · BY-SA 라 출처를 슬라이드에 표시한다 (D-064)
const COMMONS = 'https://thumb.wikimedia.org/wikipedia/commons/thumb';
const SLIDES = [
  { cat: '추천', title: "LET'S GO JAPAN", sub: '액티비티 편',
    img: `${COMMONS}/e/e9/Mount_Fuji_view_from_Lake_Kawaguchi_2024-12-01.jpg/1920px-Mount_Fuji_view_from_Lake_Kawaguchi_2024-12-01.jpg`,
    credit: 'LMP 2001 / CC BY-SA 4.0 / Wikimedia Commons',
    source: 'https://commons.wikimedia.org/wiki/File:Mount_Fuji_view_from_Lake_Kawaguchi_2024-12-01.jpg' },
  { cat: '계절', title: 'JAPAN IN WINTER', sub: '겨울 여행',
    img: `${COMMONS}/9/93/Shirakawa-go_20170202.jpg/1920px-Shirakawa-go_20170202.jpg`,
    credit: 'Suicasmo / CC BY-SA 4.0 / Wikimedia Commons',
    source: 'https://commons.wikimedia.org/wiki/File:Shirakawa-go_20170202.jpg' },
  { cat: '봄', title: 'CHERRY BLOSSOM', sub: '봄 벚꽃 명소',
    img: `${COMMONS}/2/24/Chuurei-tou_Fujiyoshida_33415029934_8bdb607294_o.jpg/1920px-Chuurei-tou_Fujiyoshida_33415029934_8bdb607294_o.jpg`,
    credit: 'bruchez (Flickr) / CC BY-SA 2.0 / Wikimedia Commons',
    source: 'https://commons.wikimedia.org/wiki/File:Chuurei-tou_Fujiyoshida_33415029934_8bdb607294_o.jpg' },
  { cat: '문화', title: 'TRADITIONAL', sub: '전통 마츠리',
    img: `${COMMONS}/0/0d/Aomori%2C_Nebuta-matsuri_34.jpg/1920px-Aomori%2C_Nebuta-matsuri_34.jpg`,
    credit: 'Marie-Sophie Mejan / CC BY 4.0 / Wikimedia Commons',
    source: 'https://commons.wikimedia.org/wiki/File:Aomori,_Nebuta-matsuri_34.jpg' },
  { cat: '미식', title: 'FOODIE JOURNEY', sub: '미식 여행',
    img: `${COMMONS}/8/8e/Osaka_Dotonbori_yoru_02.jpg/1920px-Osaka_Dotonbori_yoru_02.jpg`,
    credit: 'Sakai Yayoi / CC0 / Wikimedia Commons',
    source: 'https://commons.wikimedia.org/wiki/File:Osaka_Dotonbori_yoru_02.jpg' },
  { cat: '도시', title: 'CITY LIGHTS', sub: '도시 야경',
    img: `${COMMONS}/b/b0/Shibuya_and_Shinjuku_from_Yebisu_Garden_Place_Tower%2C_Ebisu%2C_Tokyo%2C_Japan%2C_2024_May.jpg/1920px-Shibuya_and_Shinjuku_from_Yebisu_Garden_Place_Tower%2C_Ebisu%2C_Tokyo%2C_Japan%2C_2024_May.jpg`,
    credit: 'Ximonic (Simo Räsänen) / CC BY-SA 4.0 / Wikimedia Commons',
    source: 'https://commons.wikimedia.org/wiki/File:Shibuya_and_Shinjuku_from_Yebisu_Garden_Place_Tower,_Ebisu,_Tokyo,_Japan,_2024_May.jpg' },
];

const QUICK = [
  { emoji: '🗾', label: '2025 일본 관광 100선', to: '/top100', badge: '100' },
  { emoji: '🔥', label: '핫플레이스', to: '/hotplace' },
  { emoji: '📰', label: '뉴스', to: '/news' },
  { emoji: '💡', label: '여행 팁', to: '/tip' },
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
            <div key={i} className={styles.slide} aria-hidden={i !== idx}
              style={{ backgroundImage: `linear-gradient(90deg, rgba(0,0,0,0.55) 0%, rgba(0,0,0,0.15) 60%, rgba(0,0,0,0) 100%), url("${s.img}")` }}>
              <div className={styles.slideContent}>
                <p className={styles.slideCat}>{s.cat}</p>
                <h2 className={styles.slideTitle}>{s.title}</h2>
                <p className={styles.slideSub}>{s.sub}</p>
              </div>
              <a className={styles.slideCredit} href={s.source} target="_blank" rel="noreferrer" tabIndex={i === idx ? 0 : -1}>
                사진: {s.credit}
              </a>
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
