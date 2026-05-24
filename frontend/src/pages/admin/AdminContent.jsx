import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/client.js';
import { PREFECTURES } from '../../data/prefectures.js';
import EmptyStateCollector from '../../components/EmptyStateCollector.jsx';
import { useToast } from '../../components/ui/Toast.jsx';

const EMPTY_FORM = {
  name: '', prefecture: '도쿄도', category: '', tags: '', summary: '', imageUrl: '', lat: '', lng: '',
};

export default function AdminContent() {
  const [tab, setTab] = useState('destinations');
  const [items, setItems] = useState([]);
  const [prefecture, setPrefecture] = useState('도쿄도');
  const [triggerKey, setTriggerKey] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null); // null = create, object = edit
  const [form, setForm] = useState(EMPTY_FORM);
  const [bulkRunning, setBulkRunning] = useState(false);
  const toast = useToast();
  const navigate = useNavigate();

  async function reload() {
    const prefArg = prefecture || undefined;
    if (tab === 'destinations') setItems(await api.listDestinations({ prefecture: prefArg }));
    else if (tab === 'festivals') setItems(await api.listFestivals({ prefecture: prefArg }));
    else setItems(await api.listCourses({ prefecture: prefArg }));
  }
  useEffect(() => { reload(); }, [tab, prefecture, triggerKey]);

  async function runBulk() {
    if (!confirm('47개 도도부현 × 2 카테고리 = 94개 작업을 큐잉합니다. 진행할까요?\n진행률은 "수집 현황" 페이지에서 확인됩니다.')) return;
    setBulkRunning(true);
    try {
      const r = await api.runBulkCollector();
      if (r?.conflict) {
        toast.info(`이미 진행 중입니다 (bulk #${r.bulkRunId})`);
      } else {
        toast.success(`${r?.queued ?? 94}개 작업을 큐잉했습니다 (bulk #${r?.bulkRunId})`);
      }
      navigate('/admin/collection-status');
    } catch (e) {
      toast.error('일괄 수집 시작 실패');
    } finally {
      setBulkRunning(false);
    }
  }

  function openCreate() {
    setEditing(null);
    setForm({ ...EMPTY_FORM, prefecture: prefecture || '도쿄도' });
    setModalOpen(true);
  }

  function openEdit(it) {
    setEditing(it);
    setForm({
      name: it.name || it.title || '',
      prefecture: it.prefecture || prefecture,
      category: it.category || '',
      tags: (it.tags || []).join(', '),
      summary: it.summary || '',
      imageUrl: it.imagePath || it.imageUrl || '',
      lat: it.lat ?? '',
      lng: it.lng ?? '',
    });
    setModalOpen(true);
  }

  async function remove(it) {
    if (!confirm(`"${it.name || it.title}" 삭제할까요?`)) return;
    try {
      if (tab === 'destinations') await api.adminDeleteDestination(it.id);
      else if (tab === 'festivals') await api.adminDeleteFestival(it.id);
      toast.info('삭제했습니다');
      setTriggerKey((k) => k + 1);
    } catch (e) {
      toast.error('삭제 실패');
    }
  }

  async function save(e) {
    e.preventDefault();
    const body = {
      name: form.name,
      prefecture: form.prefecture,
      category: form.category || null,
      tags: form.tags ? form.tags.split(',').map((s) => s.trim()).filter(Boolean) : [],
      summary: form.summary || null,
      imageUrl: form.imageUrl || null,
      lat: form.lat === '' ? null : Number(form.lat),
      lng: form.lng === '' ? null : Number(form.lng),
    };
    try {
      if (tab === 'destinations') {
        if (editing) await api.adminUpdateDestination(editing.id, body);
        else await api.adminCreateDestination(body);
      } else if (tab === 'festivals') {
        if (editing) await api.adminUpdateFestival(editing.id, body);
        else await api.adminCreateFestival(body);
      }
      toast.success(editing ? '수정했습니다' : '추가했습니다');
      setModalOpen(false);
      setTriggerKey((k) => k + 1);
    } catch (e) {
      toast.error('저장 실패');
    }
  }

  const editable = tab === 'destinations' || tab === 'festivals';

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, gap: 8, flexWrap: 'wrap' }}>
        <h2 style={{ margin: 0 }}>콘텐츠 관리</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <button onClick={runBulk} disabled={bulkRunning} style={{ ...btnPrimary, opacity: bulkRunning ? 0.6 : 1 }}>
            🚀 전체 47×2 일괄 수집
          </button>
        </div>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 20, alignItems: 'center', flexWrap: 'wrap' }}>
        {[['destinations', '여행지'], ['festivals', '축제'], ['courses', '코스']].map(([k, l]) => (
          <button key={k} onClick={() => setTab(k)}
                  style={{ padding: '8px 16px', background: tab === k ? '#e91e63' : '#eee', color: tab === k ? '#fff' : '#333', border: 'none', borderRadius: 4 }}>
            {l}
          </button>
        ))}
        <select value={prefecture} onChange={(e) => setPrefecture(e.target.value)} style={{ padding: 8, marginLeft: 'auto', border: '1px solid #ddd', borderRadius: 4 }}>
          <option value="">전체</option>
          {PREFECTURES.map((p) => <option key={p}>{p}</option>)}
        </select>
        {editable && (
          <button onClick={openCreate} style={btnPrimary}>+ 새 항목</button>
        )}
      </div>

      {(tab === 'destinations' || tab === 'festivals') && prefecture && (
        <div style={{ marginBottom: 20 }}>
          <EmptyStateCollector
            type={tab === 'destinations' ? 'destination' : 'festival'}
            prefecture={prefecture}
            onComplete={() => setTriggerKey((k) => k + 1)}
            message={`${prefecture} ${tab === 'destinations' ? '관광지' : '축제'} Wikipedia 수집 (${items.length}건 보유 중)`}
          />
        </div>
      )}

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f9f9f9' }}>
            <th style={th}>ID</th>
            <th style={th}>이름</th>
            <th style={th}>도도부현</th>
            <th style={th}>태그</th>
            <th style={th}>좌표</th>
            <th style={th}>이미지</th>
            {editable && <th style={th}>작업</th>}
          </tr>
        </thead>
        <tbody>
          {items.map((it) => (
            <tr key={it.id} style={{ borderBottom: '1px solid #eee' }}>
              <td style={td}>{it.id}</td>
              <td style={td}>{it.name || it.title}</td>
              <td style={td}>{it.prefecture}</td>
              <td style={td}>{(it.tags || []).join(', ')}</td>
              <td style={td}>{it.lat ? `${it.lat.toFixed(3)}, ${it.lng.toFixed(3)}` : '-'}</td>
              <td style={td}>{it.imagePath ? <a href={it.imagePath} target="_blank" rel="noreferrer">📷</a> : '-'}</td>
              {editable && (
                <td style={td}>
                  <button onClick={() => openEdit(it)} style={btn}>편집</button>{' '}
                  <button onClick={() => remove(it)} style={{ ...btn, background: '#fce4ec', color: '#c2185b' }}>삭제</button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
      {items.length === 0 && <p style={{ textAlign: 'center', color: '#888', padding: 40 }}>데이터 없음</p>}

      {modalOpen && (
        <div onClick={() => setModalOpen(false)} style={overlay}>
          <div onClick={(e) => e.stopPropagation()} style={modal}>
            <h3 style={{ marginTop: 0 }}>{editing ? '편집' : '새 항목'} ({tab === 'destinations' ? '여행지' : '축제'})</h3>
            <form onSubmit={save} style={{ display: 'grid', gap: 10 }}>
              <label style={lbl}>이름
                <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} style={inp} />
              </label>
              <label style={lbl}>도도부현
                <select value={form.prefecture} onChange={(e) => setForm({ ...form, prefecture: e.target.value })} style={inp}>
                  {PREFECTURES.map((p) => <option key={p}>{p}</option>)}
                </select>
              </label>
              <label style={lbl}>카테고리
                <input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} style={inp} />
              </label>
              <label style={lbl}>태그 (쉼표 구분)
                <input value={form.tags} onChange={(e) => setForm({ ...form, tags: e.target.value })} style={inp} />
              </label>
              <label style={lbl}>요약
                <textarea rows={3} value={form.summary} onChange={(e) => setForm({ ...form, summary: e.target.value })} style={inp} />
              </label>
              <label style={lbl}>이미지 URL
                <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} style={inp} />
              </label>
              <div style={{ display: 'flex', gap: 8 }}>
                <label style={{ ...lbl, flex: 1 }}>위도
                  <input type="number" step="any" value={form.lat} onChange={(e) => setForm({ ...form, lat: e.target.value })} style={inp} />
                </label>
                <label style={{ ...lbl, flex: 1 }}>경도
                  <input type="number" step="any" value={form.lng} onChange={(e) => setForm({ ...form, lng: e.target.value })} style={inp} />
                </label>
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
                <button type="button" onClick={() => setModalOpen(false)} style={btn}>취소</button>
                <button type="submit" style={btnPrimary}>{editing ? '수정' : '추가'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}


const th = { padding: 8, textAlign: 'left', fontSize: 13, color: '#666' };
const td = { padding: 8, fontSize: 13 };
const btn = { padding: '6px 12px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer' };
const btnPrimary = { ...btn, background: '#e91e63', color: '#fff' };
const overlay = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 };
const modal = { background: '#fff', padding: 24, borderRadius: 8, width: 480, maxWidth: '90vw', maxHeight: '90vh', overflow: 'auto' };
const lbl = { display: 'flex', flexDirection: 'column', gap: 4, fontSize: 13, color: '#555' };
const inp = { padding: 6, border: '1px solid #ddd', borderRadius: 4, fontSize: 13, fontFamily: 'inherit' };
