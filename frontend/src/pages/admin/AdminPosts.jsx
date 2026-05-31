import { Fragment, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { useToast } from '../../components/ui/Toast.jsx';

const PAGE_SIZE = 20;

export default function AdminPosts() {
  const [data, setData] = useState({ items: [], total: 0 });
  const [page, setPage] = useState(0);
  const [comments, setComments] = useState({});
  const [expanded, setExpanded] = useState(null);
  const [loading, setLoading] = useState(false);
  const toast = useToast();

  async function reload() {
    setLoading(true);
    try {
      const r = await api.adminListPosts(page, PAGE_SIZE);
      setData(r || { items: [], total: 0 });
    } catch {
      toast.error('게시글 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => { reload(); }, [page]);

  async function delPost(id) {
    if (!confirm('이 게시글을 삭제할까요? (댓글도 함께 삭제됩니다)')) return;
    try {
      await api.adminDeletePost(id);
      reload();
    } catch {
      toast.error('삭제에 실패했습니다');
    }
  }

  async function delComment(id, postId) {
    if (!confirm('이 댓글을 삭제할까요?')) return;
    try {
      await api.adminDeleteComment(id);
      const c = await api.listComments(postId);
      setComments((m) => ({ ...m, [postId]: c }));
    } catch {
      toast.error('삭제에 실패했습니다');
    }
  }

  async function toggleExpand(postId) {
    if (expanded === postId) {
      setExpanded(null);
      return;
    }
    setExpanded(postId);
    if (!comments[postId]) {
      try {
        const c = await api.listComments(postId);
        setComments((m) => ({ ...m, [postId]: c }));
      } catch {
        toast.error('댓글을 불러오지 못했습니다');
      }
    }
  }

  const totalPages = Math.max(1, Math.ceil((data.total || 0) / PAGE_SIZE));

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>게시글 ({data.total || 0})</h2>
      {loading && <p style={{ color: '#888' }}>불러오는 중…</p>}

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f9f9f9' }}>
            {['ID', '제목', '작성자', '댓글', '작성일', ''].map((h) => <th key={h} style={th}>{h}</th>)}
          </tr>
        </thead>
        <tbody>
          {(data.items || []).map((p) => (
            <Fragment key={p.id}>
              <tr style={{ borderBottom: '1px solid #eee' }}>
                <td style={td}>{p.id}</td>
                <td style={td}>
                  <Link to={`/board/${p.id}`} style={{ color: '#333' }}>{p.title}</Link>
                </td>
                <td style={td}>{p.userName || '-'}{p.userId ? ` (#${p.userId})` : ''}</td>
                <td style={td}>
                  <button onClick={() => toggleExpand(p.id)} style={linkBtn}>
                    {p.commentCount || 0} {expanded === p.id ? '접기' : '보기'}
                  </button>
                </td>
                <td style={td}>{p.createdAt}</td>
                <td style={td}>
                  <button onClick={() => delPost(p.id)} style={dangerBtn}>삭제</button>
                </td>
              </tr>
              {expanded === p.id && (
                <tr style={{ background: '#fafafa' }}>
                  <td colSpan={6} style={{ padding: 12 }}>
                    {(comments[p.id] || []).length === 0 ? (
                      <span style={{ color: '#888', fontSize: 13 }}>댓글 없음</span>
                    ) : (
                      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                          <tr>
                            {['ID', '작성자', '내용', '작성일', ''].map((h) => <th key={h} style={th}>{h}</th>)}
                          </tr>
                        </thead>
                        <tbody>
                          {(comments[p.id] || []).map((c) => (
                            <tr key={c.id} style={{ borderBottom: '1px solid #eee' }}>
                              <td style={td}>{c.id}</td>
                              <td style={td}>{c.userName || '-'}{c.userId ? ` (#${c.userId})` : ''}</td>
                              <td style={td}>{(c.body || '').slice(0, 100)}</td>
                              <td style={td}>{c.createdAt}</td>
                              <td style={td}><button onClick={() => delComment(c.id, p.id)} style={dangerBtn}>삭제</button></td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}
                  </td>
                </tr>
              )}
            </Fragment>
          ))}
        </tbody>
      </table>

      {!loading && (data.items || []).length === 0 && <p style={{ textAlign: 'center', color: '#888', padding: 40 }}>게시글 없음</p>}

      <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginTop: 16 }}>
        <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0} style={btn}>이전</button>
        <span style={{ padding: '6px 12px' }}>{page + 1} / {totalPages}</span>
        <button onClick={() => setPage((p) => (p + 1 < totalPages ? p + 1 : p))} disabled={page + 1 >= totalPages} style={btn}>다음</button>
      </div>
    </div>
  );
}

const btn = { padding: '6px 12px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer' };
const linkBtn = { background: 'none', border: 'none', color: '#3b82f6', cursor: 'pointer', padding: 0, fontSize: 13 };
const dangerBtn = { background: 'none', border: 'none', color: '#e91e63', cursor: 'pointer', fontSize: 13 };
const th = { padding: 8, textAlign: 'left', fontSize: 13, color: '#666' };
const td = { padding: 8, fontSize: 13 };
