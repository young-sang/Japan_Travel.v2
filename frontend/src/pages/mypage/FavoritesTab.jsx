import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../../api/client.js';
import PlaceCard from '../../components/PlaceCard.jsx';
import Tag from '../../components/ui/Tag.jsx';
import Button from '../../components/ui/Button.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import { SkeletonGrid } from '../../components/ui/Skeleton.jsx';
import styles from './FavoritesTab.module.css';

// "전체" 탭은 없다 — 여행지 / 축제를 따로 보인다 (D-039). 응답이 장소 DTO 그대로라 상세를 다시 부르지 않는다.
const TYPES = [
  { key: 'destination', label: '여행지' },
  { key: 'festival', label: '축제' },
];

export default function FavoritesTab({ onChange }) {
  const [fav, setFav] = useState(null);
  const [type, setType] = useState('destination');
  // 골라서 새 코스 만들기 (D-094). 고른 순서가 곧 정류장 순서다. 여행지 · 축제 탭을 오가도 고른 것은 남는다
  const [picking, setPicking] = useState(false);
  const [picked, setPicked] = useState([]);   // [{type, id}]
  const navigate = useNavigate();

  useEffect(() => {
    api.listFavorites()
      .then(setFav)
      .catch(() => setFav({ destinations: [], festivals: [] }));
  }, []);

  //  찜 탭에서는 하트를 해제하면 카드를 바로 뺀다 (D-081)
  function onFavChange(key, id, on) {
    if (!on) setFav((f) => ({ ...f, [`${key}s`]: f[`${key}s`].filter((it) => it.id !== id) }));
    onChange?.();
  }

  function togglePick(key, id) {
    setPicked((p) => (p.some((x) => x.type === key && x.id === id)
      ? p.filter((x) => !(x.type === key && x.id === id))
      : [...p, { type: key, id }]));
  }

  function stopPicking() {
    setPicking(false);
    setPicked([]);
  }

  if (fav === null) return <SkeletonGrid count={6} />;

  const shown = fav[`${type}s`] || [];
  const total = fav.destinations.length + fav.festivals.length;

  return (
    <div>
      <div className={styles.toolbar}>
        {TYPES.map((t) => (
          <Tag key={t.key} size="md" selected={type === t.key} onClick={() => setType(t.key)}>
            {t.label} {(fav[`${t.key}s`] || []).length}
          </Tag>
        ))}
        {total > 0 && (
          <span className={styles.right}>
            {picking
              ? <Button variant="ghost" size="sm" onClick={stopPicking}>취소</Button>
              : <Button variant="tint" size="sm" onClick={() => setPicking(true)}>골라서 코스 만들기</Button>}
          </span>
        )}
      </div>
      {picking && (
        <div className={styles.pickBar}>
          <span>{picked.length ? `${picked.length}곳 골랐어요 — 고른 순서대로 1일차 일정이 됩니다` : '코스에 넣을 장소를 순서대로 골라 주세요'}</span>
          <Button variant="primary" size="sm" disabled={!picked.length}
            onClick={() => navigate(`/mypage/courses/new?add=${picked.map((x) => `${x.type}-${x.id}`).join(',')}`)}>
            선택한 곳으로 코스 만들기
          </Button>
        </div>
      )}
      {shown.length === 0 ? (
        <EmptyState
          icon="♡"
          title="아직 즐겨찾기가 없어요"
          description="마음에 드는 장소를 즐겨찾기 해보세요."
          primaryAction={<Link to={`/${type}`}><Button variant="primary">{type === 'festival' ? '축제' : '여행지'} 둘러보기</Button></Link>}
        />
      ) : (
        <div className={styles.grid}>
          {shown.map((it) => {
            const order = picked.findIndex((x) => x.type === type && x.id === it.id);
            return (
              <div key={`${type}-${it.id}`} className={styles.cell}>
                <PlaceCard item={it} type={type} onFavChange={(on) => onFavChange(type, it.id, on)} />
                {picking && (
                  <button type="button" className={`${styles.pick} ${order >= 0 ? styles.picked : ''}`}
                    aria-pressed={order >= 0} aria-label={`${it.name} ${order >= 0 ? '선택 해제' : '선택'}`}
                    onClick={() => togglePick(type, it.id)}>
                    <span className={styles.badge}>{order >= 0 ? order + 1 : ''}</span>
                  </button>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
