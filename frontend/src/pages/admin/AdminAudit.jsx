import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { useToast } from '../../components/ui/Toast.jsx';

const ACTIONS = [
  '', 'LOGIN_SUCCESS', 'LOGIN_FAIL', 'SIGNUP', 'LOGOUT',
  'REVIEW_CREATE', 'REVIEW_UPDATE', 'REVIEW_DELETE',
  'COURSE_CREATE', 'COURSE_UPDATE', 'COURSE_DELETE',
  'COLLECTOR_RUN', 'COLLECTOR_BULK',
  'USER_ROLE_CHANGE', 'USER_DELETE',
  'CONTENT_CREATE', 'CONTENT_UPDATE', 'CONTENT_DELETE',
];

const PAGE_SIZE = 50;

export default function AdminAudit() {
  const [userId, setUserId] = useState('');
  const [action, setAction] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ items: [], total: 0 });
  const [loading, setLoading] = useState(false);
  const toast = useToast();

  async function reload() {
    setLoading(true);
    try {
      const r = await api.adminListAudit({
        userId: userId || undefined,
        action: action || undefined,
        from: from || undefined,
        to: to || undefined,
        page,
        size: PAGE_SIZE,
      });
      setData(r || { items: [], total: 0 });
    } catch (e) {
      toast.error('감사 로그를 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { reload(); }, [page]);

  function applyFilter(e) {
    e.preventDefault();
    if (page === 0) reload();
    else setPage(0);
  }

  const totalPages = Math.max(1, Math.ceil((data.total || 0) / PAGE_SIZE));

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>감사 로그</h2>
      <form onSubmit={applyFilter} style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap', marginBottom: 16 }}>
        <input
          type="text"
          placeholder="사용자 ID"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          style={input}
        />
        <select value={action} onChange={(e) => setAction(e.target.value)} style={input}>
          {ACTIONS.map((a) => <option key={a} value={a}>{a || '액션(전체)'}</option>)}
        </select>
        <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} style={input} />
        <span>~</span>
        <input type="date" value={to} onChange={(e) => setTo(e.target.value)} style={input} />
        <button type="submit" style={btnPrimary}>조회</button>
      </form>

      {loading && <p style={{ color: '#888' }}>불러오는 중…</p>}

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f9f9f9' }}>
            {['시간', '사용자', '액션', '대상', '상세'].map((h) => (
              <th key={h} style={th}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {(data.items || []).map((row) => (
            <tr key={row.id} style={{ borderBottom: '1px solid #eee' }}>
              <td style={td}>{row.createdAt}</td>
              <td style={td}>{row.username || '-'}{row.userId ? ` (#${row.userId})` : ''}</td>
              <td style={td}>{row.action}</td>
              <td style={td}>{row.targetType ? `${row.targetType}${row.targetId ? '#' + row.targetId : ''}` : '-'}</td>
              <td style={{ ...td, color: '#666', maxWidth: 400, overflow: 'hidden', textOverflow: 'ellipsis' }}>{row.detail || ''}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {!loading && (data.items || []).length === 0 && <p style={{ textAlign: 'center', color: '#888', padding: 40 }}>로그 없음</p>}

      <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginTop: 16 }}>
        <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0} style={btn}>이전</button>
        <span style={{ padding: '6px 12px' }}>{page + 1} / {totalPages}</span>
        <button onClick={() => setPage((p) => (p + 1 < totalPages ? p + 1 : p))} disabled={page + 1 >= totalPages} style={btn}>다음</button>
      </div>
    </div>
  );
}

const input = { padding: 6, border: '1px solid #ddd', borderRadius: 4, fontSize: 13 };
const btn = { padding: '6px 12px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer' };
const btnPrimary = { ...btn, background: '#e91e63', color: '#fff' };
const th = { padding: 8, textAlign: 'left', fontSize: 13, color: '#666' };
const td = { padding: 8, fontSize: 13 };
