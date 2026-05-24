import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';

export default function AdminReviews() {
  const [reviews, setReviews] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [history, setHistory] = useState([]);

  async function reload() {
    const [r, f, h] = await Promise.all([api.allReviews(), api.listFavorites(), api.history()]);
    setReviews(r); setFavorites(f); setHistory(h);
  }
  useEffect(() => { reload(); }, []);

  async function delReview(id) {
    if (!confirm('리뷰 삭제?')) return;
    await api.deleteReview(id);
    reload();
  }

  return (
    <div>
      <h2>리뷰 ({reviews.length})</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 12 }}>
        <thead><tr style={{ background: '#f9f9f9' }}>
          {['ID', 'User', 'Type', 'TargetID', 'Rating', 'Comment', 'When', ''].map((h) => <th key={h} style={th}>{h}</th>)}
        </tr></thead>
        <tbody>
          {reviews.map((r) => (
            <tr key={r.id} style={{ borderBottom: '1px solid #eee' }}>
              <td style={td}>{r.id}</td>
              <td style={td}>{r.userId}</td>
              <td style={td}>{r.targetType}</td>
              <td style={td}>{r.targetId}</td>
              <td style={td}>{'★'.repeat(r.rating)}</td>
              <td style={td}>{(r.comment || '').slice(0, 60)}</td>
              <td style={td}>{r.createdAt}</td>
              <td style={td}><button onClick={() => delReview(r.id)} style={{ background: 'none', border: 'none', color: '#e91e63', cursor: 'pointer' }}>삭제</button></td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2 style={{ marginTop: 40 }}>즐겨찾기 ({favorites.length})</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 12 }}>
        <thead><tr style={{ background: '#f9f9f9' }}>
          {['Type', 'TargetID', 'When'].map((h) => <th key={h} style={th}>{h}</th>)}
        </tr></thead>
        <tbody>
          {favorites.map((f) => (
            <tr key={`${f.targetType}-${f.targetId}`} style={{ borderBottom: '1px solid #eee' }}>
              <td style={td}>{f.targetType}</td>
              <td style={td}>{f.targetId}</td>
              <td style={td}>{f.createdAt}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2 style={{ marginTop: 40 }}>히스토리 ({history.length})</h2>
      <button onClick={() => api.clearHistory().then(reload)}
              style={{ padding: '8px 16px', background: '#fee', color: '#e91e63', border: 'none', borderRadius: 4, cursor: 'pointer', marginBottom: 12 }}>
        전체 삭제
      </button>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead><tr style={{ background: '#f9f9f9' }}>
          {['Type', 'TargetID', 'Visited'].map((h) => <th key={h} style={th}>{h}</th>)}
        </tr></thead>
        <tbody>
          {history.map((h) => (
            <tr key={`${h.targetType}-${h.targetId}`} style={{ borderBottom: '1px solid #eee' }}>
              <td style={td}>{h.targetType}</td>
              <td style={td}>{h.targetId}</td>
              <td style={td}>{h.visitedAt}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

const th = { padding: 8, textAlign: 'left', fontSize: 13, color: '#666' };
const td = { padding: 8, fontSize: 13 };
