import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import PlaceCard from '../../components/PlaceCard.jsx';
import Tag from '../../components/ui/Tag.jsx';
import Button from '../../components/ui/Button.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import { SkeletonGrid } from '../../components/ui/Skeleton.jsx';

// "전체" 탭은 없다 — 여행지 / 축제를 따로 보인다 (D-039). 응답이 장소 DTO 그대로라 상세를 다시 부르지 않는다.
const TYPES = [
  { key: 'destination', label: '여행지' },
  { key: 'festival', label: '축제' },
];

export default function FavoritesTab({ onChange }) {
  const [fav, setFav] = useState(null);
  const [type, setType] = useState('destination');

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

  if (fav === null) return <SkeletonGrid count={6} />;

  const shown = fav[`${type}s`] || [];

  return (
    <div>
      <div style={{ display: 'flex', gap: 8, marginBottom: 'var(--space-5)', flexWrap: 'wrap' }}>
        {TYPES.map((t) => (
          <Tag key={t.key} size="md" selected={type === t.key} onClick={() => setType(t.key)}>
            {t.label} {(fav[`${t.key}s`] || []).length}
          </Tag>
        ))}
      </div>
      {shown.length === 0 ? (
        <EmptyState
          icon="♡"
          title="아직 즐겨찾기가 없어요"
          description="마음에 드는 장소를 즐겨찾기 해보세요."
          primaryAction={<Link to={`/${type}`}><Button variant="primary">{type === 'festival' ? '축제' : '여행지'} 둘러보기</Button></Link>}
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 'var(--space-4)' }}>
          {shown.map((it) => (
            <PlaceCard key={`${type}-${it.id}`} item={it} type={type} onFavChange={(on) => onFavChange(type, it.id, on)} />
          ))}
        </div>
      )}
    </div>
  );
}
