import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from '../components/ui/Toast.jsx';
import styles from './Board.module.css';

const PAGE_SIZE = 20;

export default function Board() {
  const [data, setData] = useState({ items: [], total: 0, page: 0, size: PAGE_SIZE });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  useEffect(() => {
    setLoading(true);
    api.listPosts(page, PAGE_SIZE)
      .then((r) => setData(r || { items: [], total: 0 }))
      .catch(() => toast.error('게시글을 불러오지 못했습니다'))
      .finally(() => setLoading(false));
  }, [page]);

  const totalPages = Math.max(1, Math.ceil((data.total || 0) / PAGE_SIZE));

  return (
    <Container>
      <PageHeader
        eyebrow="자유게시판"
        title="여행자 이야기"
        subtitle="여행 후기, 질문, 정보를 자유롭게 나눠보세요"
        actions={user ? <Button variant="primary" onClick={() => navigate('/board/new')}>새 글 작성</Button> : null}
      />

      {loading && <p className={styles.muted}>불러오는 중…</p>}

      {!loading && (data.items || []).length === 0 ? (
        <div className={styles.empty}>아직 게시글이 없습니다.</div>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th style={{ width: 70 }}>#</th>
              <th>제목</th>
              <th style={{ width: 140 }}>작성자</th>
              <th style={{ width: 170 }}>작성일</th>
            </tr>
          </thead>
          <tbody>
            {(data.items || []).map((p) => (
              <tr key={p.id}>
                <td className={styles.muted}>{p.id}</td>
                <td className={styles.titleCell}>
                  <Link to={`/board/${p.id}`}>{p.title}</Link>
                  {p.commentCount > 0 && <span className={styles.count}>[{p.commentCount}]</span>}
                </td>
                <td>{p.userName || '익명'}</td>
                <td className={styles.muted}>{p.createdAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div className={styles.pagination}>
        <Button variant="ghost" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>이전</Button>
        <span className={styles.muted}>{page + 1} / {totalPages}</span>
        <Button variant="ghost" size="sm" onClick={() => setPage((p) => (p + 1 < totalPages ? p + 1 : p))} disabled={page + 1 >= totalPages}>다음</Button>
      </div>
    </Container>
  );
}
