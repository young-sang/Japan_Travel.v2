import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';

export default function AdminSystem() {
  const [cacheStats, setCacheStats] = useState(null);

  async function reload() {
    setCacheStats(await api.cacheStats());
  }
  useEffect(() => { reload(); }, []);

  async function invalidate(name) {
    await api.invalidateCache(name);
    reload();
  }

  return (
    <div>
      <h2>캐시 관리</h2>
      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        <button onClick={() => invalidate()} style={btn}>전체 무효화</button>
        <button onClick={() => invalidate('wikiSummary')} style={btn}>Wikipedia 요약 무효화</button>
        <button onClick={() => invalidate('wikiCategoryMembers')} style={btn}>Wikipedia 카테고리 무효화</button>
        <button onClick={() => invalidate('nominatimSearch')} style={btn}>Nominatim 무효화</button>
        <button onClick={reload} style={btn}>새로고침</button>
      </div>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead><tr style={{ background: '#f9f9f9' }}>
          {['Cache', 'Hits', 'Misses', 'Hit Rate', 'Evictions', 'Size'].map((h) => <th key={h} style={th}>{h}</th>)}
        </tr></thead>
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

      <h2 style={{ marginTop: 40 }}>외부 API</h2>
      <p style={{ color: '#888', fontSize: 13 }}>Wikipedia REST · OpenStreetMap Nominatim · Open-Meteo · Frankfurter — 모두 무료/키 불필요.</p>
    </div>
  );
}

const btn = { padding: '8px 16px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer' };
const th = { padding: 8, textAlign: 'left', fontSize: 13, color: '#666' };
const td = { padding: 8, fontSize: 13 };
