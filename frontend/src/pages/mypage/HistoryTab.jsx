import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import Button from '../../components/ui/Button.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import { SkeletonGrid } from '../../components/ui/Skeleton.jsx';
import { useToast } from '../../components/ui/Toast.jsx';

function groupByDate(items) {
  const today = new Date().toDateString();
  const yesterday = new Date(Date.now() - 86400000).toDateString();
  const lastWeek = Date.now() - 7 * 86400000;
  const groups = { '오늘': [], '어제': [], '지난 7일': [], '이전': [] };
  for (const it of items) {
    const d = new Date(it.visitedAt);
    if (d.toDateString() === today) groups['오늘'].push(it);
    else if (d.toDateString() === yesterday) groups['어제'].push(it);
    else if (d.getTime() > lastWeek) groups['지난 7일'].push(it);
    else groups['이전'].push(it);
  }
  return groups;
}

export default function HistoryTab() {
  const [enriched, setEnriched] = useState(null);
  const toast = useToast();

  async function reload() {
    setEnriched(null);
    try {
      const list = await api.history();
      const e = await Promise.all(list.map(async (it) => {
        try {
          const fn = it.targetType === 'destination' ? api.getDestination
                   : it.targetType === 'festival' ? api.getFestival
                   : api.getCourse;
          return { ...it, detail: await fn(it.targetId) };
        } catch { return { ...it, detail: null }; }
      }));
      setEnriched(e.filter((x) => x.detail));
    } catch {
      setEnriched([]);
    }
  }

  useEffect(() => { reload(); }, []);

  async function clearAll() {
    if (!confirm('모든 히스토리를 삭제할까요?')) return;
    try {
      await api.clearHistory();
      toast.info('히스토리를 모두 삭제했어요');
      reload();
    } catch { toast.error('삭제 실패'); }
  }

  if (enriched === null) return <SkeletonGrid count={8} columns="repeat(auto-fill, minmax(180px, 1fr))" />;

  if (enriched.length === 0) {
    return (
      <EmptyState
        icon="🕒"
        title="아직 본 장소가 없어요"
        description="여행지를 둘러보면 여기에 기록이 쌓입니다."
        primaryAction={<Link to="/destination"><Button variant="primary">여행지 둘러보기</Button></Link>}
      />
    );
  }

  const groups = groupByDate(enriched);

  return (
    <div>
      <div style={{ textAlign: 'right', marginBottom: 'var(--space-5)' }}>
        <Button variant="ghost" size="sm" onClick={clearAll}>기록 전체 삭제</Button>
      </div>
      {Object.entries(groups).map(([label, list]) => list.length > 0 && (
        <div key={label} style={{ marginBottom: 'var(--space-6)' }}>
          <h3 style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)', fontWeight: 700, letterSpacing: '0.5px', textTransform: 'uppercase', marginBottom: 'var(--space-3)' }}>{label}</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: 'var(--space-3)' }}>
            {list.map((h) => (
              <Link key={`${h.targetType}-${h.targetId}-${h.visitedAt}`}
                    to={`/detail/${h.targetType}/${h.targetId}`}
                    style={{ textDecoration: 'none', color: 'inherit' }}>
                <div style={{
                  border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)',
                  overflow: 'hidden', background: 'var(--color-surface-0)',
                }}>
                  <div style={{ aspectRatio: '4/3', background: 'var(--color-surface-2)' }}>
                    <img src={h.detail.imagePath || '/placeholder.svg'} alt=""
                         style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                         onError={(e) => { if (e.currentTarget.dataset.fb === '1') return; e.currentTarget.dataset.fb = '1'; e.currentTarget.src = '/placeholder.svg'; }} />
                  </div>
                  <div style={{ padding: '10px 12px' }}>
                    <p style={{ fontSize: 'var(--fs-sm)', fontWeight: 600, margin: 0, color: 'var(--color-ink-900)' }}>{h.detail.name || h.detail.title}</p>
                    <p style={{ fontSize: 'var(--fs-xs)', color: 'var(--color-ink-500)', margin: '4px 0 0' }}>{new Date(h.visitedAt).toLocaleString()}</p>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
