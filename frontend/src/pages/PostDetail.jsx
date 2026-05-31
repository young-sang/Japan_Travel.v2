import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Container from '../components/ui/Container.jsx';
import Button from '../components/ui/Button.jsx';
import Field from '../components/ui/Field.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from '../components/ui/Toast.jsx';
import reviewStyles from '../components/ReviewSection.module.css';
import styles from './Board.module.css';

export default function PostDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const toast = useToast();
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [commentBody, setCommentBody] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);

  async function reload() {
    try {
      const [p, c] = await Promise.all([api.getPost(id), api.listComments(id)]);
      setPost(p);
      setComments(c || []);
    } catch (e) {
      if (e?.status === 404) toast.error('게시글을 찾을 수 없습니다');
      else toast.error('게시글을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { setLoading(true); reload(); }, [id]);

  async function removePost() {
    if (!confirm('이 게시글을 삭제할까요?')) return;
    try {
      if (user?.role === 'ADMIN' && user.id !== post.userId) {
        await api.adminDeletePost(post.id);
      } else {
        await api.deletePost(post.id);
      }
      toast.info('게시글을 삭제했습니다');
      navigate('/board');
    } catch {
      toast.error('삭제에 실패했습니다');
    }
  }

  async function submitComment(e) {
    e.preventDefault();
    if (!commentBody.trim()) { toast.warn('댓글을 입력해주세요'); return; }
    setSubmitting(true);
    try {
      await api.createComment(post.id, commentBody);
      setCommentBody('');
      const c = await api.listComments(post.id);
      setComments(c || []);
    } catch {
      toast.error('댓글 등록에 실패했습니다');
    } finally {
      setSubmitting(false);
    }
  }

  async function removeComment(c) {
    if (!confirm('댓글을 삭제할까요?')) return;
    try {
      if (user?.role === 'ADMIN' && user.id !== c.userId) {
        await api.adminDeleteComment(c.id);
      } else {
        await api.deleteComment(c.id);
      }
      const cs = await api.listComments(post.id);
      setComments(cs || []);
    } catch {
      toast.error('댓글 삭제에 실패했습니다');
    }
  }

  if (loading) return <Container><p className={styles.muted}>불러오는 중…</p></Container>;
  if (!post) return <Container><div className={styles.empty}>게시글이 없습니다. <Link to="/board">목록으로</Link></div></Container>;

  const isOwner = user && user.id === post.userId;
  const isAdmin = user && user.role === 'ADMIN';

  return (
    <Container>
      <div className={styles.postHead}>
        <h1 className={styles.postTitle}>{post.title}</h1>
        <div className={styles.meta}>
          <span>{post.userName || '익명'}</span>
          <span>·</span>
          <span>{post.createdAt}</span>
          {post.updatedAt && <span>(수정 {post.updatedAt})</span>}
          {(isOwner || isAdmin) && (
            <span className={styles.metaActions}>
              {isOwner && <Button size="sm" variant="ghost" onClick={() => navigate(`/board/${post.id}/edit`)}>수정</Button>}
              <Button size="sm" variant="ghost" onClick={removePost}>{isAdmin && !isOwner ? '관리자 삭제' : '삭제'}</Button>
            </span>
          )}
        </div>
      </div>

      <div className={styles.body}>{post.body}</div>

      <div style={{ marginTop: 'var(--space-6)' }}>
        <div className={reviewStyles.head}>
          <h2 className={reviewStyles.title}>댓글 <span className={reviewStyles.count}>{comments.length}</span></h2>
        </div>

        {user ? (
          <form onSubmit={submitComment} className={reviewStyles.form}>
            <Field label="댓글" required>
              <textarea
                value={commentBody}
                onChange={(e) => setCommentBody(e.target.value)}
                placeholder="댓글을 입력하세요"
              />
            </Field>
            <div className={reviewStyles.actions}>
              <Button type="submit" variant="primary" loading={submitting}>등록</Button>
            </div>
          </form>
        ) : (
          <div style={{ padding: 'var(--space-4)', textAlign: 'center',
            background: 'var(--color-surface-1)', borderRadius: 'var(--radius-md)',
            border: '1px dashed var(--color-border)', marginBottom: 'var(--space-4)' }}>
            댓글을 작성하려면 <Link to="/login">로그인</Link>이 필요합니다.
          </div>
        )}

        <ul className={reviewStyles.list}>
          {comments.length === 0 && (
            <EmptyState icon="💬" title="아직 댓글이 없어요" description="첫 번째 댓글을 남겨보세요!" size="sm" />
          )}
          {comments.map((c) => (
            <li key={c.id} className={reviewStyles.item}>
              <div className={reviewStyles.itemHead}>
                <strong>{c.userName || '익명'}</strong>
                <span className={reviewStyles.date}>{c.createdAt}</span>
                {user && (user.id === c.userId || user.role === 'ADMIN') && (
                  <span className={reviewStyles.itemActions}>
                    <button type="button" onClick={() => removeComment(c)} className={reviewStyles.linkBtn}>삭제</button>
                  </span>
                )}
              </div>
              <p className={reviewStyles.comment}>{c.body}</p>
            </li>
          ))}
        </ul>
      </div>
    </Container>
  );
}
