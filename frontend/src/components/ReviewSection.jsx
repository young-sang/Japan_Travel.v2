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

// 리뷰는 대상 아래 중첩 경로다 (D-037). 서버는 최신순 배열만 주므로 평균 · 개수는 여기서 계산한다.
// 수정 · 삭제 버튼은 내 리뷰(userId === 내 id)에만 보인다.
export default function ReviewSection({ targetType, targetId }) {
  const [items, setItems] = useState([]);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const toast = useToast();
  const { user } = useAuth();

  async function reload() {
    try {
      setItems((await api.reviewsFor(targetType, targetId)) || []);
    } catch (e) {
      toast.error('리뷰를 불러오지 못했습니다');
    }
  }
  useEffect(() => { reload(); }, [targetType, targetId]);

  const count = items.length;
  const average = count ? items.reduce((a, r) => a + r.rating, 0) / count : 0;

  async function submit(e) {
    e.preventDefault();
    if (!comment.trim()) { toast.warn('감상을 입력해주세요'); return; }
    setSubmitting(true);
    try {
      if (editingId) {
        await api.updateReview(targetType, targetId, editingId, { rating, comment });
        toast.success('리뷰를 수정했습니다');
        setEditingId(null);
      } else {
        await api.addReview(targetType, targetId, { rating, comment });
        toast.success('리뷰를 등록했습니다');
      }
      setComment(''); setRating(5);
      reload();
    } catch (e) {
      toast.error(e.message || '리뷰 저장에 실패했습니다');
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
      await api.deleteReview(targetType, targetId, id);
      toast.info('리뷰를 삭제했습니다');
      reload();
    } catch (e) {
      toast.error(e.message || '삭제에 실패했습니다');
    }
  }

  return (
    <div>
      <div className={styles.head}>
        <h2 className={styles.title}>리뷰 <span className={styles.count}>{count}</span></h2>
        {average > 0 && (
          <Rating value={average} showValue />
        )}
      </div>

      {user ? (
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
              {editingId ? '저장' : '작성'}
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
        {items.length === 0 && (
          <EmptyState
            icon="💬"
            title="아직 리뷰가 없어요"
            description="첫 번째 후기를 남겨보세요!"
            size="sm"
          />
        )}
        {items.map((r) => (
          <li key={r.id} className={styles.item}>
            <div className={styles.itemHead}>
              <Rating value={r.rating} size={14} />
              <strong style={{ fontSize: 'var(--fs-sm)' }}>{r.nickname}</strong>
              <span className={styles.date}>{formatDate(r.updatedAt || r.createdAt)}{r.updatedAt ? ' (수정됨)' : ''}</span>
              {user && user.id === r.userId && (
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

// "2026-09-29T20:00:00" → "2026-09-29 20:00"
function formatDate(s) {
  return s ? String(s).replace('T', ' ').slice(0, 16) : '';
}
