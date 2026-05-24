import { Link } from 'react-router-dom';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import styles from './Theme.module.css';

const THEMES = [
  { tag: '온천',  title: '일본의 온천 탐방',     image: '/image/onsen1.png',     desc: '료칸과 자연 노천' },
  { tag: '등산',  title: '일본 명산 등산 코스',  image: '/image/mountain1.png',  desc: '후지·다테야마·야쿠시마' },
  { tag: '축제',  title: '전통 마츠리',           image: '/image/festival1.png',  desc: '계절마다 다른 풍경' },
  { tag: '도시',  title: '도쿄·오사카 도시 여행', image: '/image/tokyotower1.png', desc: '야경과 미식' },
  { tag: '자연',  title: '자연과 풍경',           image: '/image/fuji_main.PNG',  desc: '사진 명소' },
];

export default function Theme() {
  return (
    <Container>
      <PageHeader
        eyebrow="테마별"
        title="좋아하는 테마로 일본 발견하기"
        subtitle="관심 주제로 큐레이션된 여행지를 모아봤습니다"
      />
      <div className={styles.grid}>
        {THEMES.map((t) => (
          <Link key={t.tag} to={`/destination?tag=${encodeURIComponent(t.tag)}`} className={styles.card}>
            <div className={styles.media}>
              <img src={t.image} alt={t.title} loading="lazy"
                   onError={(e) => { if (e.currentTarget.dataset.fb === '1') return; e.currentTarget.dataset.fb = '1'; e.currentTarget.src = '/placeholder.svg'; }} />
            </div>
            <div className={styles.body}>
              <h3 className={styles.title}>{t.title}</h3>
              <p className={styles.desc}>{t.desc}</p>
              <span className={styles.cta}>#{t.tag} 보기 →</span>
            </div>
          </Link>
        ))}
      </div>
    </Container>
  );
}
