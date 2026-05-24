import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { PREFECTURES } from '../../data/prefectures.js';
import BulkProgress from './BulkProgress.jsx';
import { useToast } from '../../components/ui/Toast.jsx';

const TYPES = [
  { key: 'destination', label: '관광지' },
  { key: 'festival', label: '축제' },
];

const TERMINAL = new Set(['success', 'partial', 'failed', 'aborted', 'empty']);

export default function CollectionStatus() {
  const [matrix, setMatrix] = useState(null);
  const [filterPref, setFilterPref] = useState('');
  const [activeBulkId, setActiveBulkId] = useState(null);
  const [lastBulkFailures, setLastBulkFailures] = useState(new Set());
  const [bulkRunning, setBulkRunning] = useState(false);
  const [bulkLive, setBulkLive] = useState(false);
  const toast = useToast();

  async function reloadMatrix() {
    try {
      const m = await api.collectionMatrix();
      setMatrix(m);
    } catch (e) {
      toast.error('수집 매트릭스 로드 실패');
    }
  }

  useEffect(() => { reloadMatrix(); }, []);

  // 최근 bulk run의 실패 셀 오버레이 + 진행 중 여부 감지
  useEffect(() => {
    let alive = true;
    let timer = null;
    async function tick() {
      try {
        const list = await api.bulkRuns(1);
        if (!alive) return;
        if (list && list.length > 0) {
          const latest = list[0];
          if (!activeBulkId) setActiveBulkId(latest.id);
          const running = !TERMINAL.has(latest.status);
          setBulkLive(running);
          if (!running) {
            // 종료된 상태면 보유 매트릭스 갱신(새로 들어온 데이터 반영)
            reloadMatrix();
          }
          const d = await api.bulkRunDetail(latest.id);
          if (!alive) return;
          const fails = new Set();
          for (const c of (d.children || [])) {
            if (c.status === 'failed' || c.status === 'aborted') {
              fails.add(`${c.type}:${c.prefecture}`);
            }
          }
          setLastBulkFailures(fails);
        }
      } catch {}
      timer = setTimeout(tick, bulkLive ? 4000 : 15000);
    }
    tick();
    return () => { alive = false; if (timer) clearTimeout(timer); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bulkLive]);

  async function runBulk() {
    if (!confirm('47개 도도부현 × 2 카테고리 = 94개 작업을 큐잉합니다. 진행할까요?')) return;
    setBulkRunning(true);
    try {
      const r = await api.runBulkCollector();
      if (r?.conflict) {
        toast.info(`이미 진행 중입니다 (bulk #${r.bulkRunId})`);
        setActiveBulkId(r.bulkRunId);
      } else {
        toast.success(`${r?.queued ?? 94}개 작업을 큐잉했습니다 (bulk #${r?.bulkRunId})`);
        setActiveBulkId(r?.bulkRunId ?? null);
      }
      setBulkLive(true);
    } catch (e) {
      toast.error('일괄 수집 시작 실패');
    } finally {
      setBulkRunning(false);
    }
  }

  if (!matrix) {
    return <div style={{ padding: 20, color: '#888' }}>로딩 중...</div>;
  }

  const prefs = filterPref ? [filterPref] : matrix.prefectures;
  const totals = computeTotals(matrix);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, gap: 8, flexWrap: 'wrap' }}>
        <h2 style={{ margin: 0 }}>수집 현황</h2>
        <button onClick={runBulk} disabled={bulkRunning} style={{ ...btnPrimary, opacity: bulkRunning ? 0.6 : 1 }}>
          🚀 전체 47×2 일괄 수집
        </button>
      </div>

      {/* 상단 통계 */}
      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginBottom: 16 }}>
        <StatCard label="관광지" value={totals.destination} sub={`${totals.destinationPrefs}개 도도부현 커버`} />
        <StatCard label="축제" value={totals.festival} sub={`${totals.festivalPrefs}개 도도부현 커버`} />
        <StatCard label="미보유 셀" value={totals.empty} sub={`94개 중 ${totals.empty}개 비어있음`} />
        <StatCard label="최근 bulk 실패" value={lastBulkFailures.size} sub={lastBulkFailures.size > 0 ? '⚠ 표시된 셀 참고' : '없음'} />
      </div>

      {/* 진행 중인 bulk가 있으면 인라인 패널 */}
      {bulkLive && activeBulkId && (
        <BulkProgress activeBulkId={activeBulkId} setActiveBulkId={setActiveBulkId} toast={toast} />
      )}

      {/* 필터 */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center', flexWrap: 'wrap' }}>
        <label style={{ fontSize: 13, color: '#555' }}>도도부현:</label>
        <select value={filterPref} onChange={(e) => setFilterPref(e.target.value)} style={{ padding: 8, border: '1px solid #ddd', borderRadius: 4 }}>
          <option value="">전체</option>
          {PREFECTURES.map((p) => <option key={p}>{p}</option>)}
        </select>
        <div style={{ marginLeft: 'auto', display: 'flex', gap: 12, alignItems: 'center', fontSize: 12, color: '#666' }}>
          <Legend color="#e8f5e9" label="보유" />
          <Legend color="#f5f5f5" label="미보유" />
          <span>⚠ 최근 bulk 실패</span>
        </div>
      </div>

      {/* 47×2 매트릭스 */}
      <div style={{ overflowX: 'auto' }}>
        <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f9f9f9' }}>
              <th style={th}>도도부현</th>
              {TYPES.map((t) => <th key={t.key} style={{ ...th, textAlign: 'center' }}>{t.label}</th>)}
            </tr>
          </thead>
          <tbody>
            {prefs.map((p) => {
              const row = matrix.rows[p] || { destination: 0, festival: 0 };
              return (
                <tr key={p} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ ...td, fontWeight: 500 }}>{p}</td>
                  {TYPES.map((t) => {
                    const n = row[t.key] || 0;
                    const failed = lastBulkFailures.has(`${t.key}:${p}`);
                    return (
                      <td key={t.key} style={{ ...td, textAlign: 'center' }}>
                        <CountCell count={n} failed={failed} />
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function CountCell({ count, failed }) {
  const has = count > 0;
  const bg = has ? '#e8f5e9' : '#f5f5f5';
  const color = has ? '#2e7d32' : '#999';
  return (
    <span style={{
      display: 'inline-block', minWidth: 64, padding: '4px 10px', borderRadius: 4,
      background: bg, color, fontWeight: 600, fontSize: 13,
    }} title={failed ? '최근 일괄 수집에서 실패한 셀' : undefined}>
      {failed && <span style={{ color: '#c62828', marginRight: 4 }}>⚠</span>}
      {count}
    </span>
  );
}

function StatCard({ label, value, sub }) {
  return (
    <div style={{ flex: '1 1 180px', padding: 16, background: '#fff', border: '1px solid #eee', borderRadius: 6 }}>
      <div style={{ fontSize: 12, color: '#888', marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 24, fontWeight: 700, color: '#333' }}>{value}</div>
      <div style={{ fontSize: 11, color: '#999', marginTop: 4 }}>{sub}</div>
    </div>
  );
}

function Legend({ color, label }) {
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
      <span style={{ display: 'inline-block', width: 14, height: 14, background: color, border: '1px solid #ddd', borderRadius: 2 }} />
      {label}
    </span>
  );
}

function computeTotals(matrix) {
  let destination = 0, festival = 0, destinationPrefs = 0, festivalPrefs = 0, empty = 0;
  for (const p of matrix.prefectures) {
    const r = matrix.rows[p] || { destination: 0, festival: 0 };
    destination += r.destination;
    festival += r.festival;
    if (r.destination > 0) destinationPrefs++;
    if (r.festival > 0) festivalPrefs++;
    if (r.destination === 0) empty++;
    if (r.festival === 0) empty++;
  }
  return { destination, festival, destinationPrefs, festivalPrefs, empty };
}

const th = { padding: 10, textAlign: 'left', fontSize: 13, color: '#666', borderBottom: '1px solid #eee' };
const td = { padding: 10, fontSize: 13 };
const btn = { padding: '8px 14px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer', fontSize: 13 };
const btnPrimary = { ...btn, background: '#e91e63', color: '#fff' };
