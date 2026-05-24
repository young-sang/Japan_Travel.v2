import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import FavoritesTab from './mypage/FavoritesTab.jsx';
import CoursesTab from './mypage/CoursesTab.jsx';
import HistoryTab from './mypage/HistoryTab.jsx';
import ReviewsTab from './mypage/ReviewsTab.jsx';
import ActivityTab from './mypage/ActivityTab.jsx';
import SettingsTab from './mypage/SettingsTab.jsx';
import Container from '../components/ui/Container.jsx';
import Tabs from '../components/ui/Tabs.jsx';
import styles from './MyPage.module.css';

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
  const navigate = useNavigate();
  const [stats, setStats] = useState({ favorites: 0, courses: 0, history: 0, reviews: 0 });

  useEffect(() => {
    Promise.all([
      api.listFavorites().catch(() => []),
      api.listCourses({ ownerUserId: 1 }).catch(() => []),
      api.history().catch(() => []),
      api.myReviews().catch(() => []),
    ]).then(([fav, courses, hist, revs]) => setStats({
      favorites: fav.length, courses: courses.length, history: hist.length, reviews: revs.length,
    }));
  }, [tab]);

  return (
    <>
      <section className={styles.hero}>
        <Container>
          <div className={styles.profile}>
            <div className={styles.avatar} aria-hidden="true">🛠</div>
            <div className={styles.profileInfo}>
              <h1 className={styles.name}>관리자</h1>
              <p className={styles.subtitle}>사내 프로토타입 단일 사용자</p>
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
          {tab === 'favorites' && <FavoritesTab />}
          {tab === 'courses' && <CoursesTab />}
          {tab === 'history' && <HistoryTab />}
          {tab === 'reviews' && <ReviewsTab />}
          {tab === 'activity' && <ActivityTab />}
          {tab === 'settings' && <SettingsTab />}
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
