import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import PlaceCard from '../../components/PlaceCard.jsx';
import Tag from '../../components/ui/Tag.jsx';
import Button from '../../components/ui/Button.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import { SkeletonGrid } from '../../components/ui/Skeleton.jsx';

const TYPES = [
  { key: '', label: '전체' },
  { key: 'destination', label: '여행지' },
  { key: 'festival', label: '축제' },
  { key: 'course', label: '여행코스' },
];

export default function FavoritesTab() {
  const [enriched, setEnriched] = useState(null);
  const [filter, setFilter] = useState('');

  async function reload() {
    try {
      const list = await api.listFavorites();
      const items = await Promise.all(list.map(async (f) => {
        try {
          const fn = f.targetType === 'destination' ? api.getDestination
                   : f.targetType === 'festival' ? api.getFestival
                   : api.getCourse;
          const detail = await fn(f.targetId);
          return { ...f, detail };
        } catch { return { ...f, detail: null }; }
      }));
      setEnriched(items.filter((x) => x.detail));
    } catch {
      setEnriched([]);
    }
  }

  useEffect(() => { reload(); }, []);

  if (enriched === null) return <SkeletonGrid count={6} />;

  const shown = filter ? enriched.filter((x) => x.targetType === filter) : enriched;

  return (
    <div>
      <div style={{ display: 'flex', gap: 8, marginBottom: 'var(--space-5)', flexWrap: 'wrap' }}>
        {TYPES.map((t) => (
          <Tag key={t.key} size="md" selected={filter === t.key} onClick={() => setFilter(t.key)}>{t.label}</Tag>
        ))}
      </div>
      {shown.length === 0 ? (
        <EmptyState
          icon="♡"
          title="아직 즐겨찾기가 없어요"
          description="마음에 드는 장소나 코스를 즐겨찾기 해보세요."
          primaryAction={<Link to="/destination"><Button variant="primary">여행지 둘러보기</Button></Link>}
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 'var(--space-4)' }}>
          {shown.map((f) => (
            <PlaceCard key={`${f.targetType}-${f.targetId}`} item={f.detail} type={f.targetType} />
          ))}
        </div>
      )}
    </div>
  );
}
