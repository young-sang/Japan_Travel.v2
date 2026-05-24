import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { useAuth } from '../../auth/useAuth.js';
import { useToast } from '../../components/ui/Toast.jsx';

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const { user: me } = useAuth();
  const toast = useToast();

  async function reload() {
    setLoading(true);
    try {
      const list = await api.adminListUsers();
      setUsers(list || []);
    } catch (e) {
      toast.error('사용자 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => { reload(); }, []);

  async function changeRole(id, role) {
    try {
      await api.adminSetUserRole(id, role);
      toast.success('역할을 변경했습니다');
      reload();
    } catch (e) {
      toast.error(e?.body?.message || '역할 변경 실패');
    }
  }

  async function remove(id, username) {
    if (!confirm(`${username} 계정을 삭제할까요? 즐겨찾기/리뷰/코스/히스토리가 모두 삭제됩니다.`)) return;
    try {
      await api.adminDeleteUser(id);
      toast.info('계정을 삭제했습니다');
      reload();
    } catch (e) {
      toast.error(e?.body?.message || '삭제 실패');
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>사용자 관리</h2>
        <button onClick={reload} style={btn}>새로고침</button>
      </div>
      {loading && <p style={{ color: '#888' }}>불러오는 중…</p>}
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f9f9f9' }}>
            {['아이디', '닉네임', '역할', '즐겨찾기', '리뷰', '코스', '히스토리', '가입일', ''].map((h) => (
              <th key={h} style={th}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {users.map((u) => {
            const self = me && me.id === u.id;
            return (
              <tr key={u.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={td}>{u.username}</td>
                <td style={td}>{u.nickname}</td>
                <td style={td}>
                  <select
                    value={u.role}
                    disabled={self}
                    onChange={(e) => changeRole(u.id, e.target.value)}
                    style={{ padding: 4, border: '1px solid #ddd', borderRadius: 4 }}
                  >
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td style={td}>{u.favCount ?? 0}</td>
                <td style={td}>{u.reviewCount ?? 0}</td>
                <td style={td}>{u.courseCount ?? 0}</td>
                <td style={td}>{u.historyCount ?? 0}</td>
                <td style={td}>{u.createdAt}</td>
                <td style={td}>
                  <button
                    onClick={() => remove(u.id, u.username)}
                    disabled={self}
                    style={{ ...btn, background: self ? '#eee' : '#fce4ec', color: self ? '#aaa' : '#c2185b', cursor: self ? 'not-allowed' : 'pointer' }}
                  >
                    삭제
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
      {!loading && users.length === 0 && <p style={{ textAlign: 'center', color: '#888', padding: 40 }}>사용자 없음</p>}
    </div>
  );
}

const btn = { padding: '6px 12px', background: '#eee', border: 'none', borderRadius: 4, cursor: 'pointer' };
const th = { padding: 8, textAlign: 'left', fontSize: 13, color: '#666' };
const td = { padding: 8, fontSize: 13 };
