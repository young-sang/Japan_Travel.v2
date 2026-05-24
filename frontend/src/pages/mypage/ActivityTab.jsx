import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';

const BADGES = [
  { code: 'first_favorite',  label: '첫 즐겨찾기', emoji: '⭐', need: 1, source: 'favorites' },
  { code: 'favorites_10',    label: '즐겨찾기 10개', emoji: '🔥', need: 10, source: 'favorites' },
  { code: 'first_course',    label: '첫 코스 작성', emoji: '🗺', need: 1, source: 'courses' },
  { code: 'courses_5',       label: '코스 5개 작성', emoji: '✨', need: 5, source: 'courses' },
  { code: 'first_review',    label: '첫 리뷰',    emoji: '📝', need: 1, source: 'reviews' },
  { code: 'reviews_10',      label: '리뷰 10개',  emoji: '💬', need: 10, source: 'reviews' },
  { code: 'prefectures_5',   label: '5개 도도부현 방문', emoji: '🚄', need: 5, source: 'prefectures' },
  { code: 'prefectures_10',  label: '10개 도도부현 방문', emoji: '🗾', need: 10, source: 'prefectures' },
];

export default function ActivityTab() {
  const [counts, setCounts] = useState({ favorites: 0, courses: 0, reviews: 0, prefectures: 0 });

  useEffect(() => {
    Promise.all([api.listFavorites(), api.listCourses({ ownerUserId: 1 }), api.myReviews(), api.history()])
      .then(([fav, courses, revs, hist]) => {
        // 도도부현 카운트: history + favorites의 detail을 다 불러와야 정확하지만 비용 큼.
        // 단순화: history에서 추출 — detail 조회로 prefecture 받기
        Promise.all(hist.map(async (h) => {
          try {
            const fn = h.targetType === 'destination' ? api.getDestination : h.targetType === 'festival' ? api.getFestival : api.getCourse;
            const d = await fn(h.targetId);
            return d.prefecture;
          } catch { return null; }
        })).then((prefs) => {
          const unique = new Set(prefs.filter(Boolean));
          setCounts({ favorites: fav.length, courses: courses.length, reviews: revs.length, prefectures: unique.size });
        });
      });
  }, []);

  return (
    <div>
      <h2 style={{ fontSize: 'var(--fs-xl)', fontWeight: 700, marginBottom: 'var(--space-4)' }}>배지</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: 'var(--space-3)' }}>
        {BADGES.map((b) => {
          const progress = counts[b.source] || 0;
          const unlocked = progress >= b.need;
          return (
            <div key={b.code} style={{
              padding: 'var(--space-4)', borderRadius: 'var(--radius-lg)', textAlign: 'center',
              background: unlocked ? 'var(--color-brand-100)' : 'var(--color-surface-1)',
              border: `2px solid ${unlocked ? 'var(--color-brand-500)' : 'var(--color-border)'}`,
              opacity: unlocked ? 1 : 0.7
            }}>
              <div style={{ fontSize: 40, marginBottom: 8, filter: unlocked ? 'none' : 'grayscale(1)' }}>{b.emoji}</div>
              <div style={{ fontWeight: 600 }}>{b.label}</div>
              <div style={{ fontSize: 12, color: '#888', marginTop: 4 }}>
                {unlocked ? '획득' : `${progress} / ${b.need}`}
              </div>
            </div>
          );
        })}
      </div>

      <h3 style={{ margin: '40px 0 20px' }}>활동 요약</h3>
      <div style={{ background: '#f9f9f9', padding: 20, borderRadius: 8 }}>
        <p>방문한 도도부현: <strong>{counts.prefectures}</strong> / 47</p>
        <p>즐겨찾기: <strong>{counts.favorites}</strong></p>
        <p>작성한 코스: <strong>{counts.courses}</strong></p>
        <p>작성한 리뷰: <strong>{counts.reviews}</strong></p>
      </div>
    </div>
  );
}
