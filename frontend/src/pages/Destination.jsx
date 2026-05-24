import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import PrefectureGrid from '../components/PrefectureGrid.jsx';
import TagGrid from '../components/TagGrid.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import EmptyStateCollector from '../components/EmptyStateCollector.jsx';
import { DEST_TAGS } from '../data/prefectures.js';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import Tag from '../components/ui/Tag.jsx';
import { SkeletonGrid } from '../components/ui/Skeleton.jsx';
import ListPageLayout, { ResultsGrid } from '../layouts/ListPageLayout.jsx';
import { useToast } from '../components/ui/Toast.jsx';

export default function Destination() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const toast = useToast();
  const [prefecture, setPrefecture] = useState('');
  const [tag, setTag] = useState(params.get('tag') || '');
  const [items, setItems] = useState(null);
  const [sort, setSort] = useState('default');

  async function load() {
    setItems(null);
    try {
      const list = await api.listDestinations({ prefecture: prefecture || undefined, tag: tag || undefined });
      setItems(list);
    } catch (e) {
      toast.error('여행지 목록을 불러오지 못했습니다');
      setItems([]);
    }
  }

  useEffect(() => { load(); }, [prefecture, tag]);

  const sortedItems = sortItems(items, sort);

  function gotoRandom() {
    if (!items || items.length === 0) return;
    const x = items[Math.floor(Math.random() * items.length)];
    navigate(`/detail/destination/${x.id}`);
  }

  return (
    <Container size="wide">
      <PageHeader
        eyebrow="둘러보기"
        title="일본 여행지"
        subtitle="현(都道府県)과 테마로 골라보는 일본 곳곳의 명소"
      />
      <ListPageLayout
        sidebar={
          <>
            <PrefectureGrid value={prefecture} onChange={setPrefecture} />
            <TagGrid tags={DEST_TAGS} value={tag} onChange={setTag} title="테마" />
            {(tag || prefecture) && (
              <Button variant="ghost" size="sm" onClick={() => { setPrefecture(''); setTag(''); }}>
                필터 초기화
              </Button>
            )}
          </>
        }
        toolbar={
          <>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
              <Tag variant="brand" size="md">{prefecture || '전체'}{tag && ` · ${tag}`}</Tag>
              <span style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)' }}>
                {items === null ? '불러오는 중…' : `결과 ${items.length}건`}
              </span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <label htmlFor="dest-sort" style={{ fontSize: 'var(--fs-sm)', color: 'var(--color-ink-700)' }}>정렬</label>
              <select
                id="dest-sort"
                value={sort}
                onChange={(e) => setSort(e.target.value)}
                style={{
                  font: 'inherit', fontSize: 'var(--fs-sm)',
                  padding: '6px 10px', borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--color-border-strong)', background: '#fff',
                }}
              >
                <option value="default">추천순</option>
                <option value="name">이름순</option>
              </select>
              <Button variant="secondary" size="sm" onClick={gotoRandom} disabled={!items?.length}>🎲 랜덤</Button>
            </div>
          </>
        }
      >
        {items === null && <SkeletonGrid count={8} />}
        {items !== null && items.length === 0 && prefecture && (
          <EmptyStateCollector type="destination" prefecture={prefecture} onComplete={load} />
        )}
        {items !== null && items.length === 0 && !prefecture && (
          <div style={{ padding: 40, textAlign: 'center', color: 'var(--color-ink-500)' }}>표시할 여행지가 없습니다</div>
        )}
        {items !== null && items.length > 0 && (
          <ResultsGrid>
            {sortedItems.map((it) => <PlaceCard key={it.id} item={it} type="destination" />)}
          </ResultsGrid>
        )}
      </ListPageLayout>
    </Container>
  );
}

function sortItems(items, sort) {
  if (!items) return items;
  if (sort === 'name') {
    return [...items].sort((a, b) => (a.name || a.title || '').localeCompare(b.name || b.title || ''));
  }
  return items;
}
