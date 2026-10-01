import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, typeSlug } from '../../api/client.js';

const TYPE_LABEL = { destination: '여행지', festival: '축제' };

// 내 리뷰 (D-055) — 여행지 · 축제 리뷰가 한 배열로 최근순. 삭제는 대상 아래 경로를 쓴다 (D-037).
export default function ReviewsTab() {
  const [reviews, setReviews] = useState([]);

  async function reload() {
    try { setReviews(await api.myReviews()); } catch { setReviews([]); }
  }
  useEffect(() => { reload(); }, []);

  const avg = reviews.length ? reviews.reduce((a, r) => a + r.rating, 0) / reviews.length : 0;
  const dist = [1, 2, 3, 4, 5].map((n) => reviews.filter((r) => r.rating === n).length);
  const max = Math.max(1, ...dist);

  async function remove(r) {
    if (!confirm('리뷰를 삭제할까요?')) return;
    await api.deleteReview(typeSlug(r.type), r.targetId, r.id);
    reload();
  }

  return (
    <div>
      <div style={{ background: '#f9f9f9', padding: 20, borderRadius: 8, marginBottom: 20 }}>
        <div style={{ display: 'flex', gap: 30, alignItems: 'center' }}>
          <div>
            <div style={{ fontSize: 36, fontWeight: 700, color: '#f59e0b' }}>★ {avg.toFixed(1)}</div>
            <div style={{ fontSize: 13, color: '#888' }}>{reviews.length}개 리뷰</div>
          </div>
          <div style={{ flex: 1 }}>
            {[5, 4, 3, 2, 1].map((n) => (
              <div key={n} style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                <span style={{ width: 24, fontSize: 13 }}>{n}★</span>
                <div style={{ flex: 1, background: '#eee', borderRadius: 4, height: 8 }}>
                  <div style={{ background: '#f59e0b', height: '100%', borderRadius: 4, width: `${(dist[n - 1] / max) * 100}%` }} />
                </div>
                <span style={{ width: 24, fontSize: 13, color: '#888' }}>{dist[n - 1]}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {reviews.map((r) => (
        <div key={`${r.type}-${r.id}`} style={{ padding: 16, borderBottom: '1px solid #eee' }}>
          <div>
            <Link to={`/detail/${typeSlug(r.type)}/${r.targetId}`} style={{ color: '#888', fontSize: 13 }}>
              {TYPE_LABEL[typeSlug(r.type)]} · {r.targetName}
            </Link>
            <span style={{ marginLeft: 12, color: '#f59e0b' }}>{'★'.repeat(r.rating)}</span>
            <span style={{ float: 'right' }}>
              <button onClick={() => remove(r)} style={{ background: 'none', border: 'none', color: '#888', cursor: 'pointer' }}>삭제</button>
            </span>
          </div>
          <p style={{ marginTop: 8 }}>{r.comment}</p>
        </div>
      ))}
      {reviews.length === 0 && <p style={{ color: '#888', padding: 40, textAlign: 'center' }}>아직 작성한 리뷰가 없습니다.</p>}
    </div>
  );
}
