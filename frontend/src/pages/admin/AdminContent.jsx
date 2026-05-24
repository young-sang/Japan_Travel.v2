import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { PREFECTURES } from '../../data/prefectures.js';
import EmptyStateCollector from '../../components/EmptyStateCollector.jsx';

export default function AdminContent() {
  const [tab, setTab] = useState('destinations');
  const [items, setItems] = useState([]);
  const [prefecture, setPrefecture] = useState('도쿄도');
  const [triggerKey, setTriggerKey] = useState(0);

  async function reload() {
    if (tab === 'destinations') setItems(await api.listDestinations({ prefecture }));
    else if (tab === 'festivals') setItems(await api.listFestivals({ prefecture }));
    else setItems(await api.listCourses({ prefecture: prefecture || undefined }));
  }
  useEffect(() => { reload(); }, [tab, prefecture, triggerKey]);

  return (
    <div>
      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        {[['destinations', '여행지'], ['festivals', '축제'], ['courses', '코스']].map(([k, l]) => (
          <button key={k} onClick={() => setTab(k)}
                  style={{ padding: '8px 16px', background: tab === k ? '#e91e63' : '#eee', color: tab === k ? '#fff' : '#333', border: 'none', borderRadius: 4 }}>
            {l}
          </button>
        ))}
        <select value={prefecture} onChange={(e) => setPrefecture(e.target.value)} style={{ padding: 8, marginLeft: 'auto', border: '1px solid #ddd', borderRadius: 4 }}>
          {PREFECTURES.map((p) => <option key={p}>{p}</option>)}
        </select>
      </div>

      {(tab === 'destinations' || tab === 'festivals') && (
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
            </tr>
          ))}
        </tbody>
      </table>
      {items.length === 0 && <p style={{ textAlign: 'center', color: '#888', padding: 40 }}>데이터 없음</p>}
    </div>
  );
}

const th = { padding: 8, textAlign: 'left', fontSize: 13, color: '#666' };
const td = { padding: 8, fontSize: 13 };
