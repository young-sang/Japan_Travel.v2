import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, typeSlug } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import Container from '../components/ui/Container.jsx';
import IconButton from '../components/ui/IconButton.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import { SkeletonCard } from '../components/ui/Skeleton.jsx';
import styles from './Home.module.css';
import usePageTitle from '../utils/usePageTitle.js';

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
  { emoji: '🗾', label: '일본 여행지 TOP 25', to: '/top25', badge: '25' },
  { emoji: '📰', label: '뉴스', to: '/news' },
  { emoji: '💡', label: '여행 팁', to: '/tip' },
];

//  홈 추천 코스 — 지역이 고루 퍼지도록 기본 제공 코스 6개를 제목으로 고른다 (D-081).
//  제목이 바뀌어 못 찾으면 나머지 기본 제공 코스로 6칸을 채운다
const FEATURED_TITLES = [
  '도쿄 첫 방문 2박 3일',
  '교토 핵심 2박 3일',
  '오사카 먹방 1박 2일',
  '삿포로·오타루 2박 3일',
  '히로시마·미야지마 1박 2일',
  '오키나와 본섬 2박 3일',
];

function pickFeatured(courses) {
  const builtIn = courses.filter((c) => c.ownerId === null);
  const featured = FEATURED_TITLES.map((t) => builtIn.find((c) => c.title === t)).filter(Boolean);
  const rest = builtIn.filter((c) => !featured.includes(c));
  return [...featured, ...rest].slice(0, 6);
}

//  맞춤 추천 한 곳 → PlaceCard 모양. 유사도 · 근거 장소는 화면에 보이지 않는다 (M3-1)
function fromRecommendation(x) {
  return {
    id: x.targetId,
    type: typeSlug(x.type),
    name: x.name,
    prefecture: x.prefecture,
    imagePath: x.imagePath,
    description: x.description,
    avgRating: x.avgRating,
    reviewCount: x.reviewCount,
  };
}

//  인기 = TOP 25 순위(D-074 · D-076)의 1~5위. 두 목록이 같도록 순위를 그대로 쓴다 (M3-2)
function loadPopular() {
  return api.destinationRanking().then((r) => ({
    kind: 'popular',
    items: (r || []).slice(0, 5).map((x) => ({ ...x.destination, type: 'destination' })),
  }));
}

const SECTION_TEXT = {
  personal: { badge: '맞춤', label: '맞춤 추천 여행지' },
  popular: { title: '지금 인기 있는 여행지', badge: '인기', label: '인기 여행지' },
};

export default function Home() {
  usePageTitle(null);
  const [idx, setIdx] = useState(0);
  const [paused, setPaused] = useState(false);
  const [popular, setPopular] = useState(null);
  const { user, isLoading } = useAuth();
  //  추천 칸 (M3-1 · M3-2). kind: 'personal'(맞춤) | 'popular'(인기) | null(숨김), items: null = 불러오는 중
  const [section, setSection] = useState({ kind: null, items: null });

  useEffect(() => {
    //  로그인 확인이 끝나기 전에 인기 → 맞춤으로 깜빡이며 두 번 부르지 않는다
    if (isLoading) return;
    let cancelled = false;
    setSection({ kind: user ? 'personal' : 'popular', items: null });
    const load = user
      //  활동이 없으면 맞춤이 빈 배열 → 인기로 대신한다
      ? api.recommendations().then((r) => (r && r.length > 0
          ? { kind: 'personal', items: r.map(fromRecommendation) }
          : loadPopular()))
      : loadPopular();
    load
      .then((s) => { if (!cancelled) setSection(s.items.length > 0 ? s : { kind: null, items: [] }); })
      .catch(() => { if (!cancelled) setSection({ kind: null, items: [] }); });
    return () => { cancelled = true; };
  }, [user, isLoading]);

  useEffect(() => {
    if (paused) return;
    const t = setInterval(() => setIdx((i) => (i + 1) % SLIDES.length), 5000);
    return () => clearInterval(t);
  }, [paused]);

  useEffect(() => {
    api.listCourses()
      //  코스는 인기를 셀 근거(즐겨찾기 · 조회수)가 없어 기본 제공 코스만 추천으로 싣는다 (D-067)
      .then((r) => setPopular(pickFeatured(r || [])))
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

        {section.kind && (section.items === null || section.items.length > 0) && (
          <section className={styles.section} aria-label={SECTION_TEXT[section.kind].label}>
            <header className={styles.sectionHead}>
              <h2 className={styles.sectionTitle}>
                {section.kind === 'personal' ? `${user?.nickname ?? ''}님을 위한 추천` : SECTION_TEXT.popular.title}
                <span className={styles.forYouBadge}>{SECTION_TEXT[section.kind].badge}</span>
              </h2>
            </header>
            <div className={styles.cardGrid}>
              {section.items === null && Array.from({ length: 5 }).map((_, i) => <SkeletonCard key={i} />)}
              {section.items !== null && section.items.map((p) => (
                <PlaceCard key={`${p.type}-${p.id}`} item={p} type={p.type} />
              ))}
            </div>
          </section>
        )}

        <section className={styles.section} aria-label="추천 여행코스">
          <header className={styles.sectionHead}>
            <h2 className={styles.sectionTitle}>추천 여행코스</h2>
            <Link to="/course" className={styles.sectionMore}>전체 보기 →</Link>
          </header>
          <div className={styles.cardGrid}>
            {popular === null && Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
            {popular !== null && popular.length === 0 && (
              <p className={styles.empty}>아직 추천 코스가 없어요.</p>
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
