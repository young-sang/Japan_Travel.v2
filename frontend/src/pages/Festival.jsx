import { useEffect, useState } from 'react';
import PrefectureGrid from '../components/PrefectureGrid.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import EmptyStateCollector from '../components/EmptyStateCollector.jsx';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import Tag from '../components/ui/Tag.jsx';
import { SkeletonGrid } from '../components/ui/Skeleton.jsx';
import ListPageLayout, { ResultsGrid } from '../layouts/ListPageLayout.jsx';
import { useToast } from '../components/ui/Toast.jsx';
import filterStyles from '../components/FilterGroup.module.css';

const MONTHS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];

export default function Festival() {
  const [prefecture, setPrefecture] = useState('');
  const [month, setMonth] = useState(null);
  const [items, setItems] = useState(null);
  const [view, setView] = useState('list');
  const toast = useToast();

  async function load() {
    setItems(null);
    try {
      const list = await api.listFestivals({ prefecture: prefecture || undefined, month: month || undefined });
      setItems(list);
    } catch (e) {
      toast.error('축제 목록을 불러오지 못했습니다');
      setItems([]);
    }
  }
  useEffect(() => { load(); }, [prefecture, month]);

  return (
    <Container size="wide">
      <PageHeader
        eyebrow="둘러보기"
        title="일본 축제"
        subtitle="계절과 지역으로 골라보는 마츠리"
      />
      <ListPageLayout
        sidebar={
          <>
            <PrefectureGrid value={prefecture} onChange={setPrefecture} />
            <div className={filterStyles.group}>
              <h3 className={filterStyles.title}>월</h3>
              <div className={filterStyles.tags}>
                <Tag size="sm" selected={month === null} onClick={() => setMonth(null)}>전체</Tag>
                {MONTHS.map((m) => (
                  <Tag key={m} size="sm" selected={month === m} onClick={() => setMonth(m === month ? null : m)}>{m}월</Tag>
                ))}
              </div>
            </div>
            {(month !== null || prefecture) && (
              <Button variant="ghost" size="sm" onClick={() => { setMonth(null); setPrefecture(''); }}>필터 초기화</Button>
            )}
          </>
        }
        toolbar={
          <>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
              <Tag variant="brand" size="md">{prefecture || '전체'}{month && ` · ${month}월`}</Tag>
              <span style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)' }}>
                {items === null ? '불러오는 중…' : `결과 ${items.length}건`}
              </span>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <Button variant={view === 'list' ? 'primary' : 'secondary'} size="sm" onClick={() => setView('list')}>리스트</Button>
              <Button variant={view === 'calendar' ? 'primary' : 'secondary'} size="sm" onClick={() => setView('calendar')}>캘린더</Button>
            </div>
          </>
        }
      >
        {items === null && <SkeletonGrid count={8} />}
        {items !== null && items.length === 0 && prefecture && (
          <EmptyStateCollector type="festival" prefecture={prefecture} onComplete={load} />
        )}
        {items !== null && items.length === 0 && !prefecture && (
          <div style={{ padding: 40, textAlign: 'center', color: 'var(--color-ink-500)' }}>표시할 축제가 없습니다</div>
        )}
        {items !== null && items.length > 0 && view === 'list' && (
          <ResultsGrid>
            {items.map((it) => <PlaceCard key={it.id} item={it} type="festival" />)}
          </ResultsGrid>
        )}
        {items !== null && items.length > 0 && view === 'calendar' && (
          <CalendarView items={items} />
        )}
      </ListPageLayout>
    </Container>
  );
}

function CalendarView({ items }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12 }}>
      {MONTHS.map((m) => {
        const monthItems = items.filter((it) => it.month === m);
        return (
          <div key={m} style={{
            border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)',
            padding: 'var(--space-3)', minHeight: 120, background: 'var(--color-surface-0)'
          }}>
            <h4 style={{ marginBottom: 8, color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)', fontWeight: 700 }}>{m}월</h4>
            {monthItems.length === 0 && <p style={{ color: 'var(--color-ink-300)', fontSize: 12 }}>일정 없음</p>}
            {monthItems.map((it) => (
              <a key={it.id} href={`/detail/festival/${it.id}`}
                 style={{ display: 'block', padding: '4px 0', color: 'var(--color-ink-800)', textDecoration: 'none', fontSize: 13 }}>
                • {it.name}
              </a>
            ))}
          </div>
        );
      })}
    </div>
  );
}
