import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import FavoritesTab from './mypage/FavoritesTab.jsx';
import CoursesTab from './mypage/CoursesTab.jsx';
import HistoryTab from './mypage/HistoryTab.jsx';
import ReviewsTab from './mypage/ReviewsTab.jsx';
import ActivityTab from './mypage/ActivityTab.jsx';
import SettingsTab from './mypage/SettingsTab.jsx';
import Container from '../components/ui/Container.jsx';
import Tabs from '../components/ui/Tabs.jsx';
import styles from './MyPage.module.css';
import usePageTitle from '../utils/usePageTitle.js';

const TABS = [
  { key: 'favorites', label: '즐겨찾기' },
  { key: 'courses',   label: '내 코스' },
  { key: 'history',   label: '최근 본 장소' },
  { key: 'reviews',   label: '내 리뷰' },
  { key: 'activity',  label: '활동 / 배지' },
  { key: 'settings',  label: '설정' },
];

export default function MyPage() {
  const { tab } = useParams();
  usePageTitle(`${TABS.find((t) => t.key === tab)?.label ?? '마이페이지'} - 마이페이지`);
  const navigate = useNavigate();
  const { user } = useAuth();
  const [stats, setStats] = useState({ favorites: 0, courses: 0, history: 0, reviews: 0 });

  //  위쪽 숫자. 탭이 바뀔 때와, 탭 안에서 찜 · 기록 · 코스 · 리뷰가 바뀔 때 다시 센다 (D-081)
  function loadStats() {
    Promise.all([
      api.listFavorites().catch(() => ({ destinations: [], festivals: [] })),
      api.myCourses().catch(() => []),
      api.history().catch(() => []),
      api.myReviews().catch(() => []),
    ]).then(([fav, courses, hist, revs]) => setStats({
      favorites: fav.destinations.length + fav.festivals.length,
      courses: courses.length, history: hist.length, reviews: revs.length,
    }));
  }
  useEffect(() => { loadStats(); }, [tab]);

  return (
    <>
      <section className={styles.hero}>
        <Container>
          <div className={styles.profile}>
            <div className={styles.avatar} aria-hidden="true">🙂</div>
            <div className={styles.profileInfo}>
              <h1 className={styles.name}>{user?.nickname || '게스트'}</h1>
              <p className={styles.subtitle}>@{user?.username}</p>
              <div className={styles.statRow}>
                <Stat label="즐겨찾기" value={stats.favorites} to="/mypage/favorites" />
                <Stat label="내 코스" value={stats.courses} to="/mypage/courses" />
                <Stat label="최근 본" value={stats.history} to="/mypage/history" />
                <Stat label="리뷰" value={stats.reviews} to="/mypage/reviews" />
              </div>
            </div>
          </div>
        </Container>
      </section>

      <Tabs
        items={TABS.map((t) => ({
          ...t,
          count: t.key === 'favorites' ? stats.favorites
               : t.key === 'courses' ? stats.courses
               : t.key === 'history' ? stats.history
               : t.key === 'reviews' ? stats.reviews
               : undefined,
        }))}
        value={tab}
        onChange={(k) => navigate(`/mypage/${k}`)}
        sticky
        ariaLabel="마이페이지 메뉴"
      />

      <Container>
        <section className={styles.content}>
          {tab === 'favorites' && <FavoritesTab onChange={loadStats} />}
          {tab === 'courses' && <CoursesTab onChange={loadStats} />}
          {tab === 'history' && <HistoryTab onChange={loadStats} />}
          {tab === 'reviews' && <ReviewsTab onChange={loadStats} />}
          {tab === 'activity' && <ActivityTab />}
          {tab === 'settings' && <SettingsTab onChange={loadStats} />}
        </section>
      </Container>
    </>
  );
}

function Stat({ label, value, to }) {
  return (
    <Link to={to} className={styles.stat}>
      <span className={styles.statValue}>{value}</span>
      <span className={styles.statLabel}>{label}</span>
    </Link>
  );
}
