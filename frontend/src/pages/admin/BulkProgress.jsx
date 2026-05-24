import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { PREFECTURES } from '../../data/prefectures.js';

const CELL_COLORS = {
  success: '#4caf50',
  partial: '#ff9800',
  failed: '#f44336',
  aborted: '#757575',
  running: '#2196f3',
  queued: '#e0e0e0',
  empty: '#bdbdbd',
};

const TERMINAL = new Set(['success', 'partial', 'failed', 'aborted', 'empty']);

export function statusBadge(s) {
  return (
    <span style={{ padding: '2px 8px', borderRadius: 10, background: CELL_COLORS[s] || '#999', color: '#fff', fontSize: 11 }}>{s}</span>
  );
}

export default function BulkProgress({ activeBulkId, setActiveBulkId, toast }) {
  const [detail, setDetail] = useState(null);
  const [bulkList, setBulkList] = useState([]);
  const [selectedChild, setSelectedChild] = useState(null);
  const [retryingBulk, setRetryingBulk] = useState(false);

  useEffect(() => {
    if (!activeBulkId) { setDetail(null); return; }
    let alive = true;
    let timer = null;
    async function tick() {
      try {
        const d = await api.bulkRunDetail(activeBulkId);
        if (!alive) return;
        setDetail(d);
        const st = d?.summary?.status;
        if (st && TERMINAL.has(st)) return;
        timer = setTimeout(tick, 3000);
      } catch {
        timer = setTimeout(tick, 5000);
      }
    }
    tick();
    return () => { alive = false; if (timer) clearTimeout(timer); };
  }, [activeBulkId]);

  useEffect(() => {
    let alive = true;
    let timer = null;
    async function tick() {
      try {
        const list = await api.bulkRuns(10);
        if (!alive) return;
        setBulkList(list || []);
        if (!activeBulkId && list && list.length > 0) {
          setActiveBulkId(list[0].id);
        }
      } catch {}
      timer = setTimeout(tick, 5000);
    }
    tick();
    return () => { alive = false; if (timer) clearTimeout(timer); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!selectedChild || !detail) return;
    const fresh = (detail.children || []).find((c) => c.id === selectedChild.id);
    if (fresh) setSelectedChild(fresh);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [detail]);

  const summary = detail?.summary;
  const children = detail?.children || [];
  const total = summary?.totalTasks || children.length || 0;
  const success = summary?.tasksSuccess || 0;
  const partial = summary?.tasksPartial || 0;
  const failed = summary?.tasksFailed || 0;
  const aborted = summary?.tasksAborted || 0;
  const done = success + partial + failed + aborted;
  const remaining = Math.max(0, total - done);
  const pct = total > 0 ? Math.round((done / total) * 100) : 0;

  const finished = children.filter((c) => c.finishedAt && c.startedAt);
  let etaMs = 0;
  if (finished.length > 0 && remaining > 0) {
    const total_ms = finished.reduce((acc, c) => {
      const a = new Date(c.startedAt).getTime();
      const b = new Date(c.finishedAt).getTime();
      return acc + Math.max(0, b - a);
    }, 0);
    const avg = total_ms / finished.length;
    etaMs = avg * remaining;
  }
  const etaText = etaMs > 0 ? formatDuration(etaMs) : null;

  const failedChildren = children.filter((c) => ['failed', 'aborted', 'partial'].includes(c.status));
  const isTerminal = summary && TERMINAL.has(summary.status);
  const canRetryFailed = isTerminal && failedChildren.length > 0;

  async function onRetryFailed() {
    if (!activeBulkId) return;
    if (!confirm(`실패/부분/중단된 ${failedChildren.length}개 작업을 재시도할까요?`)) return;
    setRetryingBulk(true);
    try {
      const r = await api.retryFailedInBulk(activeBulkId);
      if (r?.bulkRunId) {
        toast.success(`재시도 큐잉됨 (bulk #${r.bulkRunId}, ${r.queued ?? r.retried ?? 0}개)`);
        setActiveBulkId(r.bulkRunId);
      } else if (r?.retried === 0) {
        toast.info('재시도할 항목이 없습니다');
      }
    } catch (e) {
      if (e?.status === 409) toast.error(e?.body?.code || '이미 진행 중입니다');
      else toast.error('재시도 실패');
    } finally {
      setRetryingBulk(false);
    }
  }

  async function onRetrySingle(child) {
    try {
      const r = await api.retryRun(child.id);
      if (r?.parentBulkRunId) {
        toast.success(`재시도 큐잉됨 (run #${r.runId})`);
        if (r.parentBulkRunId !== activeBulkId) setActiveBulkId(r.parentBulkRunId);
        setSelectedChild(null);
      }
    } catch (e) {
      const msg = e?.body?.code || e?.body?.message || '재시도 실패';
      alert(msg);
    }
  }

  const types = ['destination', 'festival'];
  const cellMap = new Map();
  for (const c of children) {
    cellMap.set(`${c.type}:${c.prefecture}`, c);
  }

  return (
    <div style={{ marginBottom: 20, padding: 16, background: '#fafafa', border: '1px solid #e0e0e0', borderRadius: 6 }}>
      <style>{`@keyframes pulse { 0%,100% { opacity: 1 } 50% { opacity: 0.45 } }`}</style>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8, gap: 8, flexWrap: 'wrap' }}>
        <strong>
          일괄 수집 진행률 {activeBulkId ? `(bulk #${activeBulkId})` : ''}
          {summary && <span style={{ marginLeft: 8 }}>{statusBadge(summary.status)}</span>}
        </strong>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          {etaText && <span style={{ fontSize: 12, color: '#666' }}>예상 잔여: {etaText}</span>}
          <button
            onClick={onRetryFailed}
            disabled={!canRetryFailed || retryingBulk}
            style={{ ...btn, background: canRetryFailed ? '#fff3e0' : '#eee', color: canRetryFailed ? '#e65100' : '#999', cursor: canRetryFailed ? 'pointer' : 'not-allowed', opacity: retryingBulk ? 0.6 : 1 }}
            title={!isTerminal ? '진행 중에는 재시도할 수 없습니다' : ''}
          >
            실패한 것만 다시 시도 ({failedChildren.length}개)
          </button>
        </div>
      </div>

      <div style={{ fontSize: 13, color: '#555', marginBottom: 8 }}>
        전체 {total} · ✅ 성공 {success} · ⚠️ 부분 {partial} · ❌ 실패 {failed} · ⏸ 중단 {aborted} · 남은 {remaining} ({pct}%)
      </div>

      <div style={{ height: 12, background: '#eee', borderRadius: 6, overflow: 'hidden', display: 'flex', marginBottom: 16 }}>
        {total > 0 && (
          <>
            <div style={{ width: `${(success / total) * 100}%`, background: CELL_COLORS.success }} title={`성공 ${success}`} />
            <div style={{ width: `${(partial / total) * 100}%`, background: CELL_COLORS.partial }} title={`부분 ${partial}`} />
            <div style={{ width: `${(failed / total) * 100}%`, background: CELL_COLORS.failed }} title={`실패 ${failed}`} />
            <div style={{ width: `${(aborted / total) * 100}%`, background: CELL_COLORS.aborted }} title={`중단 ${aborted}`} />
          </>
        )}
      </div>

      {activeBulkId && (
        <div style={{ overflowX: 'auto', marginBottom: 16 }}>
          <table style={{ borderCollapse: 'separate', borderSpacing: 2, fontSize: 11 }}>
            <thead>
              <tr>
                <th style={{ padding: 4, textAlign: 'left', color: '#666' }}>도도부현</th>
                <th style={{ padding: 4, textAlign: 'center', color: '#666' }}>관광지</th>
                <th style={{ padding: 4, textAlign: 'center', color: '#666' }}>축제</th>
              </tr>
            </thead>
            <tbody>
              {PREFECTURES.map((p) => (
                <tr key={p}>
                  <td style={{ padding: '2px 8px', color: '#444', whiteSpace: 'nowrap' }}>{p}</td>
                  {types.map((t) => {
                    const cell = cellMap.get(`${t}:${p}`);
                    return (
                      <td key={t} style={{ padding: 0 }}>
                        <GridCell cell={cell} onClick={() => cell && setSelectedChild(cell)} />
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div>
        <div style={{ fontSize: 13, color: '#555', marginBottom: 6, fontWeight: 600 }}>최근 일괄 수집 ({bulkList.length})</div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
          <thead>
            <tr style={{ background: '#f0f0f0' }}>
              <th style={{ padding: 6, textAlign: 'left' }}>ID</th>
              <th style={{ padding: 6, textAlign: 'left' }}>상태</th>
              <th style={{ padding: 6, textAlign: 'left' }}>시작</th>
              <th style={{ padding: 6, textAlign: 'right' }}>총/성공/부분/실패/중단</th>
            </tr>
          </thead>
          <tbody>
            {bulkList.map((b) => (
              <tr
                key={b.id}
                onClick={() => setActiveBulkId(b.id)}
                style={{ borderBottom: '1px solid #eee', cursor: 'pointer', background: b.id === activeBulkId ? '#fce4ec' : 'transparent' }}
              >
                <td style={{ padding: 6 }}>{b.id}</td>
                <td style={{ padding: 6 }}>{statusBadge(b.status)}</td>
                <td style={{ padding: 6 }}>{b.startedAt ? new Date(b.startedAt).toLocaleString() : '-'}</td>
                <td style={{ padding: 6, textAlign: 'right', color: '#555' }}>
                  {b.totalTasks} / {b.tasksSuccess} / {b.tasksPartial} / {b.tasksFailed} / {b.tasksAborted}
                </td>
              </tr>
            ))}
            {bulkList.length === 0 && (
              <tr><td colSpan={4} style={{ padding: 12, textAlign: 'center', color: '#999' }}>일괄 수집 이력이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {selectedChild && (
        <ChildModal
          child={selectedChild}
          onClose={() => setSelectedChild(null)}
          onRetry={() => onRetrySingle(selectedChild)}
        />
      )}
    </div>
  );
}

function GridCell({ cell, onClick }) {
  const status = cell?.status || 'queued';
  const bg = CELL_COLORS[status] || '#ddd';
  const isRunning = status === 'running';
  const title = cell
    ? `${cell.type} / ${cell.prefecture} / ${cell.status}${cell.stage ? ' / ' + cell.stage : ''}`
    : '미생성';
  return (
    <div
      onClick={cell ? onClick : undefined}
      title={title}
      style={{
        width: 28,
        height: 18,
        borderRadius: 3,
        background: bg,
        cursor: cell ? 'pointer' : 'default',
        margin: '0 auto',
        opacity: cell ? 1 : 0.3,
        animation: isRunning ? 'pulse 1.2s ease-in-out infinite' : 'none',
      }}
    />
  );
}

function ChildModal({ child, onClose, onRetry }) {
  return (
    <div onClick={onClose} style={overlay}>
      <div onClick={(e) => e.stopPropagation()} style={{ ...modal, width: 560 }}>
        <h3 style={{ marginTop: 0 }}>
          {child.type} · {child.prefecture} {statusBadge(child.status)}
        </h3>
        <div style={{ fontSize: 13, color: '#555', display: 'grid', gap: 4, marginBottom: 12 }}>
          <div>단계: {child.stage || '-'}</div>
          <div>마지막 heartbeat: {child.lastHeartbeatAt ? new Date(child.lastHeartbeatAt).toLocaleString() : '-'}</div>
          <div>추가 {child.itemsAdded ?? 0} · 갱신 {child.itemsUpdated ?? 0} · 실패 {child.itemsFailed ?? 0}</div>
          {child.abortReason && (
            <div style={{ color: '#c62828', marginTop: 4 }}>
              <strong>중단 사유:</strong> {child.abortReason}
            </div>
          )}
        </div>

        <div style={{ marginBottom: 12 }}>
          <strong style={{ fontSize: 13 }}>실패 항목 ({(child.failures || []).length})</strong>
          {(!child.failures || child.failures.length === 0) ? (
            <div style={{ padding: 8, color: '#888', fontSize: 13 }}>실패 항목 없음</div>
          ) : (
            <div style={{ maxHeight: 240, overflowY: 'auto', border: '1px solid #eee', borderRadius: 4, marginTop: 4 }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
                <thead>
                  <tr style={{ background: '#f5f5f5', position: 'sticky', top: 0 }}>
                    <th style={{ padding: 6, textAlign: 'left' }}>제목</th>
                    <th style={{ padding: 6, textAlign: 'left' }}>단계</th>
                    <th style={{ padding: 6, textAlign: 'left' }}>예외</th>
                    <th style={{ padding: 6, textAlign: 'left' }}>메시지</th>
                  </tr>
                </thead>
                <tbody>
                  {child.failures.map((f, i) => (
                    <tr key={i} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: 6 }}>{f.title || '-'}</td>
                      <td style={{ padding: 6 }}>{f.stage || '-'}</td>
                      <td style={{ padding: 6, color: '#c62828' }}>{f.exceptionClass || '-'}</td>
                      <td style={{ padding: 6, color: '#666' }}>{f.message || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button onClick={onClose} style={btn}>닫기</button>
          <button onClick={onRetry} style={btnPrimary}>이 작업만 재시도</button>
        </div>
      </div>
    </div>
  );
}

function formatDuration(ms) {
  const s = Math.round(ms / 1000);
  if (s < 60) return `${s}초`;
  const m = Math.floor(s / 60);
  const rs = s % 60;
  if (m < 60) return `${m}분 ${rs}초`;
  const h = Math.floor(m / 60);
  const rm = m % 60;
  return `${h}시간 ${rm}분`;
}

const btn = { padding: '6px 12px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer' };
const btnPrimary = { ...btn, background: '#e91e63', color: '#fff' };
const overlay = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 };
const modal = { background: '#fff', padding: 24, borderRadius: 8, width: 480, maxWidth: '90vw', maxHeight: '90vh', overflow: 'auto' };
