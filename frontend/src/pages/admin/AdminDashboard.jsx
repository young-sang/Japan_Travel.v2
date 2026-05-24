import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [cacheStats, setCacheStats] = useState(null);
  const [runs, setRuns] = useState([]);

  useEffect(() => {
    api.stats().then(setStats);
    api.cacheStats().then(setCacheStats);
    api.recentCollectorRuns(10).then(setRuns);
  }, []);

  return (
    <div>
      <h2>현황</h2>
      {stats && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: 16, marginTop: 20 }}>
          {Object.entries(stats).map(([k, v]) => (
            <div key={k} style={{ padding: 16, border: '1px solid #eee', borderRadius: 8, textAlign: 'center' }}>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#e91e63' }}>{v}</div>
              <div style={{ fontSize: 12, color: '#888', marginTop: 4 }}>{labelOf(k)}</div>
            </div>
          ))}
        </div>
      )}

      <h2 style={{ marginTop: 40 }}>최근 Collector 실행</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 12 }}>
        <thead>
          <tr style={{ background: '#f9f9f9' }}>
            {['ID', 'Type', 'Prefecture', 'Source', 'Status', 'Added', 'Updated', 'Failed', 'Started'].map((h) => (
              <th key={h} style={{ padding: 8, textAlign: 'left', fontSize: 13, color: '#666' }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {runs.map((r) => (
            <tr key={r.id} style={{ borderBottom: '1px solid #eee' }}>
              <td style={td}>{r.id}</td>
              <td style={td}>{r.type}</td>
              <td style={td}>{r.prefecture}</td>
              <td style={td}>{r.source}</td>
              <td style={td}><Status status={r.status} /></td>
              <td style={td}>{r.itemsAdded}</td>
              <td style={td}>{r.itemsUpdated}</td>
              <td style={td}>{r.itemsFailed}</td>
              <td style={td}>{r.startedAt}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2 style={{ marginTop: 40 }}>캐시 통계</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 12 }}>
        <thead>
          <tr style={{ background: '#f9f9f9' }}>
            {['Cache', 'Hits', 'Misses', 'Hit Rate', 'Evictions', 'Size'].map((h) => (
              <th key={h} style={{ padding: 8, textAlign: 'left', fontSize: 13, color: '#666' }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {cacheStats && Object.entries(cacheStats).map(([name, s]) => (
            <tr key={name} style={{ borderBottom: '1px solid #eee' }}>
              <td style={td}>{name}</td>
              <td style={td}>{s.hitCount}</td>
              <td style={td}>{s.missCount}</td>
              <td style={td}>{(s.hitRate * 100).toFixed(1)}%</td>
              <td style={td}>{s.evictionCount}</td>
              <td style={td}>{s.size}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

const td = { padding: 8, fontSize: 13 };

function Status({ status }) {
  const color = { success: '#10b981', partial: '#f59e0b', failed: '#ef4444', running: '#3b82f6' }[status] || '#888';
  return <span style={{ padding: '2px 8px', borderRadius: 12, background: color + '22', color, fontSize: 12, fontWeight: 600 }}>{status}</span>;
}

function labelOf(k) {
  return { destinations: '여행지', festivals: '축제', courses: '코스', favorites: '즐겨찾기', reviews: '리뷰', prefecturesCovered: '도도부현' }[k] || k;
}
