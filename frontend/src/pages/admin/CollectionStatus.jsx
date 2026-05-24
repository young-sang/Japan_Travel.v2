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
  const [lastBulkFailureMeta, setLastBulkFailureMeta] = useState(new Map()); // key → { status, stage, exception }
  const [lastBulkEmpties, setLastBulkEmpties] = useState(new Set());
  const [lastMassBulkId, setLastMassBulkId] = useState(null);
  const [bulkRunning, setBulkRunning] = useState(false);
  const [bulkLive, setBulkLive] = useState(false);
  const [cellModal, setCellModal] = useState(null); // { prefecture, type, count }
  const [cellRunning, setCellRunning] = useState(false);
  const [recentRuns, setRecentRuns] = useState([]);
  const toast = useToast();

  async function reloadMatrix() {
    try {
      const m = await api.collectionMatrix();
      setMatrix(m);
    } catch (e) {
      toast.error('수집 매트릭스 로드 실패');
    }
  }

  async function reloadRecentRuns() {
    try {
      const r = await api.recentCollectorRuns(20);
      setRecentRuns(r || []);
    } catch {}
  }

  useEffect(() => { reloadMatrix(); reloadRecentRuns(); }, []);

  // 탭 포커스 / 가시화 변경 시 즉시 새로고침
  useEffect(() => {
    function onVisible() {
      if (document.visibilityState === 'visible') reloadMatrix();
    }
    document.addEventListener('visibilitychange', onVisible);
    window.addEventListener('focus', reloadMatrix);
    return () => {
      document.removeEventListener('visibilitychange', onVisible);
      window.removeEventListener('focus', reloadMatrix);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 최근 bulk run의 실패 셀 오버레이 + 진행 중 여부 감지
  useEffect(() => {
    let alive = true;
    let timer = null;
    async function tick() {
      try {
        const list = await api.bulkRuns(20);
        if (!alive) return;
        if (list && list.length > 0) {
          // 최근 mass-bulk(totalTasks > 1) 찾기 — single-task mini-bulk는 overlay 대상에서 제외
          const mass = list.find((b) => (b.totalTasks ?? 0) > 1);
          const latest = list[0];
          if (!activeBulkId) setActiveBulkId(latest.id);
          const running = !TERMINAL.has(latest.status);
          setBulkLive(running);
          if (!running) {
            reloadMatrix();
            reloadRecentRuns();
          }
          // running 중이든 끝났든 mass-bulk의 실패/부분/aborted/중단 child를 모두 ⚠ 대상으로
          if (mass) {
            const d = await api.bulkRunDetail(mass.id);
            if (!alive) return;
            const fails = new Set();
            const meta = new Map();
            const empties = new Set();
            for (const c of (d.children || [])) {
              const key = `${c.type}:${c.prefecture}`;
              if (['failed', 'aborted', 'partial'].includes(c.status)) {
                fails.add(key);
                const firstFailure = (c.failures || [])[0];
                meta.set(key, {
                  status: c.status,
                  stage: c.stage || firstFailure?.stage,
                  exception: firstFailure?.exceptionClass,
                  message: firstFailure?.message,
                  abortReason: c.abortReason,
                });
              } else if (c.status === 'empty') {
                empties.add(key);
              }
            }
            setLastBulkFailures(fails);
            setLastBulkFailureMeta(meta);
            setLastBulkEmpties(empties);
            setLastMassBulkId(mass.id);
          } else {
            setLastBulkFailures(new Set());
            setLastBulkFailureMeta(new Map());
            setLastBulkEmpties(new Set());
            setLastMassBulkId(null);
          }
        }
      } catch {}
      timer = setTimeout(tick, bulkLive ? 4000 : 5000);
    }
    tick();
    return () => { alive = false; if (timer) clearTimeout(timer); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bulkLive]);

  async function runCellCollect() {
    if (!cellModal) return;
    setCellRunning(true);
    try {
      const r = await api.runCollector(cellModal.type, cellModal.prefecture);
      toast.success(`${cellModal.prefecture} ${cellModal.type === 'destination' ? '관광지' : '축제'} 수집 시작`);
      if (r?.bulkRunId) {
        setActiveBulkId(r.bulkRunId);
        setBulkLive(true);
      }
      setCellModal(null);
    } catch (e) {
      if (e?.status === 409) toast.error(e?.body?.message || '진행 중인 수집이 있습니다');
      else toast.error('수집 시작 실패');
    } finally {
      setCellRunning(false);
    }
  }

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
        <StatCard
          label="관광지"
          value={totals.destination}
          sub={`${totals.destinationPrefs}개 도도부현 커버`}
          tip="DB에 저장된 관광지(destination) 행의 총 개수. 보유 도도부현 = destination이 1건 이상인 도도부현."
        />
        <StatCard
          label="축제"
          value={totals.festival}
          sub={`${totals.festivalPrefs}개 도도부현 커버`}
          tip="DB에 저장된 축제(festival) 행의 총 개수."
        />
        <StatCard
          label="미보유 셀"
          value={totals.empty}
          sub={`94개 중 ${totals.empty}개 비어있음`}
          tip="47 도도부현 × 2 카테고리(관광지/축제) = 94개 셀 중 보유 데이터가 0건인 셀 수. 이 숫자가 0에 가까울수록 커버리지가 완전."
        />
        <StatCard
          label="최근 bulk 실패"
          value={lastBulkFailures.size}
          sub={lastMassBulkId ? `bulk #${lastMassBulkId} 기준` : '대규모 bulk 이력 없음'}
          tip="가장 최근의 mass bulk(94/47 등 다건 수집)에서 실패·중단·부분 성공한 (도도부현, 카테고리) 셀의 수. 매트릭스 상의 ⚠ 표시와 동일. 단일 셀 수집은 포함되지 않음."
        />
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
          <Legend color="#f5f5f5" label="미시도" />
          <Legend color="#eceff1" label="∅ 카테고리 비어있음" />
          <Legend color="#ffebee" label="⚠ 실패" />
        </div>
      </div>

      {cellModal && (
        <CellCollectModal
          cell={cellModal}
          running={cellRunning}
          onConfirm={runCellCollect}
          onClose={() => setCellModal(null)}
        />
      )}

      <RecentRunsPanel runs={recentRuns} />

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
                    const key = `${t.key}:${p}`;
                    const failed = n === 0 && lastBulkFailures.has(key);
                    const knownEmpty = n === 0 && lastBulkEmpties.has(key);
                    const meta = lastBulkFailureMeta.get(key) || null;
                    const refreshedAt = row[t.key === 'destination' ? 'destinationAt' : 'festivalAt'];
                    return (
                      <td key={t.key} style={{ ...td, textAlign: 'center' }}>
                        <CountCell
                          count={n}
                          failed={failed}
                          knownEmpty={knownEmpty}
                          meta={meta}
                          refreshedAt={refreshedAt}
                          onClick={() => setCellModal({ prefecture: p, type: t.key, count: n, meta, bulkId: lastMassBulkId, refreshedAt, knownEmpty })}
                        />
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

function CountCell({ count, failed, knownEmpty, meta, refreshedAt, onClick }) {
  const has = count > 0;
  let bg, color, icon;
  if (has) { bg = '#e8f5e9'; color = '#2e7d32'; }
  else if (failed) { bg = '#ffebee'; color = '#c62828'; }
  else if (knownEmpty) { bg = '#eceff1'; color = '#546e7a'; icon = '∅'; }
  else { bg = '#f5f5f5'; color = '#999'; }
  const titleParts = [];
  if (failed) {
    titleParts.push('최근 일괄 수집 실패');
    if (meta?.stage) titleParts.push(`단계: ${meta.stage}`);
    if (meta?.exception) titleParts.push(`예외: ${meta.exception}`);
    if (meta?.message) titleParts.push(`메시지: ${meta.message.slice(0, 80)}`);
    titleParts.push('(클릭하여 재시도)');
  } else if (knownEmpty) {
    titleParts.push('Wikipedia 카테고리에 항목 없음 (정상 0건)');
    titleParts.push('(클릭하여 다시 시도)');
  } else if (!has) {
    titleParts.push('미수집 — 클릭하여 시작');
  } else {
    titleParts.push('클릭하여 다시 수집');
  }
  if (refreshedAt) titleParts.push(`마지막 갱신: ${formatRefreshed(refreshedAt)}`);
  return (
    <button
      onClick={onClick}
      style={{
        display: 'inline-block', minWidth: 64, padding: '4px 10px', borderRadius: 4,
        background: bg, color, fontWeight: 600, fontSize: 13,
        border: '1px solid transparent', cursor: 'pointer', fontFamily: 'inherit',
      }}
      onMouseEnter={(e) => { e.currentTarget.style.borderColor = '#999'; }}
      onMouseLeave={(e) => { e.currentTarget.style.borderColor = 'transparent'; }}
      title={titleParts.join('\n')}
    >
      {failed && <span style={{ color: '#c62828', marginRight: 4 }}>⚠</span>}
      {icon && <span style={{ marginRight: 4, opacity: 0.7 }}>{icon}</span>}
      {count}
    </button>
  );
}

function RecentRunsPanel({ runs }) {
  const [open, setOpen] = useState(false);
  if (!runs || runs.length === 0) return null;
  const visible = open ? runs : runs.slice(0, 5);
  return (
    <div style={{ marginBottom: 16, padding: 12, background: '#fafafa', border: '1px solid #eee', borderRadius: 6 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <strong style={{ fontSize: 13 }}>최근 수집 활동 ({runs.length})</strong>
        <button onClick={() => setOpen(!open)} style={{
          background: 'transparent', border: 'none', color: '#1976d2', cursor: 'pointer', fontSize: 12,
        }}>{open ? '접기' : `${runs.length > 5 ? '전체 보기' : '펴기'}`}</button>
      </div>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
        <thead>
          <tr style={{ background: '#f0f0f0' }}>
            <th style={{ padding: 4, textAlign: 'left' }}>ID</th>
            <th style={{ padding: 4, textAlign: 'left' }}>타입</th>
            <th style={{ padding: 4, textAlign: 'left' }}>도도부현</th>
            <th style={{ padding: 4, textAlign: 'left' }}>상태</th>
            <th style={{ padding: 4, textAlign: 'right' }}>추가/갱신/실패</th>
            <th style={{ padding: 4, textAlign: 'left' }}>시작</th>
            <th style={{ padding: 4, textAlign: 'left' }}>bulk</th>
          </tr>
        </thead>
        <tbody>
          {visible.map((r) => (
            <tr key={r.id} style={{ borderBottom: '1px solid #eee' }}>
              <td style={{ padding: 4 }}>{r.id}</td>
              <td style={{ padding: 4 }}>{r.type === 'destination' ? '관광지' : '축제'}</td>
              <td style={{ padding: 4 }}>{r.prefecture}</td>
              <td style={{ padding: 4 }}><RunStatusBadge s={r.status} /></td>
              <td style={{ padding: 4, textAlign: 'right', color: '#555' }}>
                {r.itemsAdded ?? 0} / {r.itemsUpdated ?? 0} / {r.itemsFailed ?? 0}
              </td>
              <td style={{ padding: 4, color: '#888' }}>{r.startedAt ? new Date(r.startedAt).toLocaleTimeString() : '-'}</td>
              <td style={{ padding: 4, color: '#888' }}>{r.bulkRunId ? `#${r.bulkRunId}` : '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function RunStatusBadge({ s }) {
  const colors = { success: '#4caf50', partial: '#ff9800', failed: '#f44336', aborted: '#757575', running: '#2196f3', queued: '#e0e0e0', empty: '#bdbdbd' };
  return <span style={{ padding: '1px 6px', borderRadius: 8, background: colors[s] || '#999', color: '#fff', fontSize: 10 }}>{s}</span>;
}

function CellCollectModal({ cell, running, onConfirm, onClose }) {
  const label = cell.type === 'destination' ? '관광지' : '축제';
  return (
    <div onClick={onClose} style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
    }}>
      <div onClick={(e) => e.stopPropagation()} style={{
        background: '#fff', padding: 24, borderRadius: 8, width: 460, maxWidth: '90vw',
      }}>
        <h3 style={{ marginTop: 0 }}>{cell.prefecture} · {label}</h3>
        <div style={{ fontSize: 13, color: '#555', marginBottom: 12 }}>
          현재 보유: <strong>{cell.count}건</strong>
          {cell.refreshedAt && (
            <span style={{ marginLeft: 8, color: '#888', fontSize: 11 }}>
              (마지막 갱신: {formatRefreshed(cell.refreshedAt)})
            </span>
          )}
        </div>
        {cell.meta && (
          <div style={{ padding: 10, marginBottom: 12, background: '#fff3e0', border: '1px solid #ffcc80', borderRadius: 4, fontSize: 12, color: '#5d4037' }}>
            <div style={{ fontWeight: 600, marginBottom: 4, color: '#e65100' }}>
              ⚠ 최근 bulk #{cell.bulkId} 실패 ({cell.meta.status})
            </div>
            {cell.meta.stage && <div>단계: <code>{cell.meta.stage}</code></div>}
            {cell.meta.exception && <div>예외: <code>{cell.meta.exception}</code></div>}
            {cell.meta.message && <div style={{ marginTop: 4, color: '#888' }}>{cell.meta.message.slice(0, 200)}</div>}
            {cell.meta.abortReason && <div style={{ marginTop: 4, color: '#c62828' }}>중단 사유: {cell.meta.abortReason}</div>}
          </div>
        )}
        <div style={{ fontSize: 12, color: '#888', marginBottom: 16 }}>
          Wikipedia에서 다시 가져와 데이터베이스에 추가/갱신합니다.
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button onClick={onClose} disabled={running} style={{
            padding: '8px 14px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer',
          }}>취소</button>
          <button onClick={onConfirm} disabled={running} style={{
            padding: '8px 14px', background: '#e91e63', color: '#fff', border: 'none',
            borderRadius: 4, cursor: running ? 'not-allowed' : 'pointer', opacity: running ? 0.6 : 1,
          }}>{running ? '시작 중...' : (cell.meta ? '🔁 재시도' : (cell.count > 0 ? '🔄 다시 수집' : '🚀 수집 시작'))}</button>
        </div>
      </div>
    </div>
  );
}

function StatCard({ label, value, sub, tip }) {
  return (
    <div title={tip} style={{ flex: '1 1 180px', padding: 16, background: '#fff', border: '1px solid #eee', borderRadius: 6, cursor: tip ? 'help' : 'default' }}>
      <div style={{ fontSize: 12, color: '#888', marginBottom: 4 }}>
        {label}{tip && <span style={{ marginLeft: 4, color: '#bbb' }}>ⓘ</span>}
      </div>
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

function formatRefreshed(ts) {
  if (!ts) return '';
  try {
    const d = new Date(ts.replace(' ', 'T') + 'Z');
    const now = Date.now();
    const diff = now - d.getTime();
    if (diff < 60_000) return '방금 전';
    if (diff < 3600_000) return `${Math.floor(diff / 60_000)}분 전`;
    if (diff < 86400_000) return `${Math.floor(diff / 3600_000)}시간 전`;
    return d.toLocaleDateString();
  } catch { return ts; }
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
