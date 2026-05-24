import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import Field from './ui/Field.jsx';
import Button from './ui/Button.jsx';
import Rating from './ui/Rating.jsx';
import EmptyState from './ui/EmptyState.jsx';
import { useToast } from './ui/Toast.jsx';
import styles from './ReviewSection.module.css';

export default function ReviewSection({ targetType, targetId }) {
  const [data, setData] = useState({ items: [], average: 0, count: 0 });
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const toast = useToast();
  const { user } = useAuth();

  async function reload() {
    try {
      const r = await api.reviewsFor(targetType, targetId);
      setData(r || { items: [], average: 0, count: 0 });
    } catch (e) {
      toast.error('리뷰를 불러오지 못했습니다');
    }
  }
  useEffect(() => { reload(); }, [targetType, targetId]);

  async function submit(e) {
    e.preventDefault();
    if (!comment.trim()) { toast.warn('감상을 입력해주세요'); return; }
    setSubmitting(true);
    try {
      if (editingId) {
        await api.updateReview(editingId, { targetType, targetId, rating, comment });
        toast.success('리뷰를 수정했습니다');
        setEditingId(null);
      } else {
        await api.addReview({ targetType, targetId, rating, comment });
        toast.success('리뷰를 등록했습니다');
      }
      setComment(''); setRating(5);
      reload();
    } catch (e) {
      toast.error('리뷰 저장에 실패했습니다');
    } finally {
      setSubmitting(false);
    }
  }

  function startEdit(r) {
    setEditingId(r.id); setRating(r.rating); setComment(r.comment || '');
    window.scrollTo({ top: window.scrollY, behavior: 'smooth' });
  }

  async function remove(id) {
    if (!confirm('리뷰를 삭제할까요?')) return;
    try {
      await api.deleteReview(id);
      toast.info('리뷰를 삭제했습니다');
      reload();
    } catch {
      toast.error('삭제에 실패했습니다');
    }
  }

  return (
    <div>
      <div className={styles.head}>
        <h2 className={styles.title}>리뷰 <span className={styles.count}>{data.count || 0}</span></h2>
        {data.average > 0 && (
          <Rating value={data.average} showValue />
        )}
      </div>

      {user && user.role === 'ADMIN' ? null : user ? (
        <form onSubmit={submit} className={styles.form}>
          <Field label="평점" required>
            <div>
              <Rating value={rating} onChange={setRating} size={28} />
            </div>
          </Field>
          <Field label="감상" required helper="다른 여행자에게 도움이 되는 솔직한 후기를 남겨주세요">
            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="기억에 남는 순간, 추천 시간대, 주의할 점…"
            />
          </Field>
          <div className={styles.actions}>
            {editingId && (
              <Button type="button" variant="ghost" onClick={() => { setEditingId(null); setComment(''); setRating(5); }}>
                취소
              </Button>
            )}
            <Button type="submit" variant="primary" loading={submitting}>
              {editingId ? '수정' : '작성'}
            </Button>
          </div>
        </form>
      ) : (
        <div style={{ padding: 'var(--space-4)', textAlign: 'center',
          background: 'var(--color-surface-1)', borderRadius: 'var(--radius-md)',
          border: '1px dashed var(--color-border)', marginBottom: 'var(--space-4)' }}>
          리뷰를 작성하려면 <Link to="/login">로그인</Link>이 필요합니다.
        </div>
      )}

      <ul className={styles.list}>
        {(data.items || []).length === 0 && (
          <EmptyState
            icon="💬"
            title="아직 리뷰가 없어요"
            description="첫 번째 후기를 남겨보세요!"
            size="sm"
          />
        )}
        {(data.items || []).map((r) => (
          <li key={r.id} className={styles.item}>
            <div className={styles.itemHead}>
              <Rating value={r.rating} size={14} />
              <span className={styles.date}>{r.createdAt}</span>
              {user && (user.id === r.userId || user.role === 'ADMIN') && (
                <span className={styles.itemActions}>
                  <button type="button" onClick={() => startEdit(r)} className={styles.linkBtn}>수정</button>
                  <button type="button" onClick={() => remove(r.id)} className={styles.linkBtn}>삭제</button>
                </span>
              )}
            </div>
            {r.comment && <p className={styles.comment}>{r.comment}</p>}
          </li>
        ))}
      </ul>
    </div>
  );
}
