import { useEffect, useState } from 'react';
import PrefectureGrid from '../components/PrefectureGrid.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import Tag from '../components/ui/Tag.jsx';
import { SkeletonGrid } from '../components/ui/Skeleton.jsx';
import ListPageLayout, { ResultsGrid } from '../layouts/ListPageLayout.jsx';
import { useToast } from '../components/ui/Toast.jsx';
import filterStyles from '../components/FilterGroup.module.css';
import usePageTitle from '../utils/usePageTitle.js';
import useQueryState from '../utils/useQueryState.js';

const MONTHS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];

export default function Festival() {
  usePageTitle('축제');
  // 필터 · 보기 · 정렬은 주소에 둔다 (D-084)
  const [prefecture, setPrefecture] = useQueryState('prefecture');
  const [monthText, setMonthText] = useQueryState('month');
  const month = monthText ? Number(monthText) : null;
  const setMonth = (m) => setMonthText(m ?? '');
  const [items, setItems] = useState(null);
  const [view, setView] = useQueryState('view', 'list');
  const [sort, setSort] = useQueryState('sort', 'default');
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
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              {view === 'list' && (
                <>
                  <label htmlFor="fest-sort" style={{ fontSize: 'var(--fs-sm)', color: 'var(--color-ink-700)' }}>정렬</label>
                  <select
                    id="fest-sort"
                    value={sort}
                    onChange={(e) => setSort(e.target.value)}
                    style={{
                      font: 'inherit', fontSize: 'var(--fs-sm)',
                      padding: '6px 10px', borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--color-border-strong)', background: '#fff',
                    }}
                  >
                    <option value="default">월순</option>
                    <option value="rating">평점순</option>
                    <option value="reviews">리뷰 많은순</option>
                  </select>
                </>
              )}
              <Button variant={view === 'list' ? 'primary' : 'secondary'} size="sm" onClick={() => setView('list')}>리스트</Button>
              <Button variant={view === 'calendar' ? 'primary' : 'secondary'} size="sm" onClick={() => setView('calendar')}>캘린더</Button>
            </div>
          </>
        }
      >
        {items === null && <SkeletonGrid count={8} />}
        {items !== null && items.length === 0 && (
          <div style={{ padding: 40, textAlign: 'center', color: 'var(--color-ink-500)' }}>표시할 축제가 없습니다</div>
        )}
        {items !== null && items.length > 0 && view === 'list' && (
          <ResultsGrid>
            {sortItems(items, sort).map((it) => <PlaceCard key={it.id} item={it} type="festival" />)}
          </ResultsGrid>
        )}
        {items !== null && items.length > 0 && view === 'calendar' && (
          <CalendarView items={items} />
        )}
      </ListPageLayout>
    </Container>
  );
}

// 기본은 서버 순서(월 오름차순). 리뷰 없는 곳(avgRating null)은 -1 로 두어 뒤로 보낸다 (D-057)
function sortItems(items, sort) {
  if (sort === 'rating') {
    return [...items].sort((a, b) => ((b.avgRating ?? -1) - (a.avgRating ?? -1)) || (b.reviewCount - a.reviewCount));
  }
  if (sort === 'reviews') {
    return [...items].sort((a, b) => (b.reviewCount - a.reviewCount) || ((b.avgRating ?? -1) - (a.avgRating ?? -1)));
  }
  return items;
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
