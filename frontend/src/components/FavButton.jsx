import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from './ui/Toast.jsx';
import styles from './FavButton.module.css';

const PLACEHOLDER_CACHE = { items: null, ts: 0, userKey: null };

async function fetchOnce(userKey) {
  if (PLACEHOLDER_CACHE.userKey === userKey && PLACEHOLDER_CACHE.items
      && Date.now() - PLACEHOLDER_CACHE.ts < 5000) return PLACEHOLDER_CACHE.items;
  const items = await api.listFavorites();
  PLACEHOLDER_CACHE.items = items;
  PLACEHOLDER_CACHE.ts = Date.now();
  PLACEHOLDER_CACHE.userKey = userKey;
  return items;
}

export default function FavButton({ targetType, targetId, size = 'md' }) {
  const [on, setOn] = useState(false);
  const [busy, setBusy] = useState(false);
  const toast = useToast();
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (!user || user.role === 'ADMIN') { setOn(false); return; }
    fetchOnce(user.id)
      .then((items) => setOn(items.some((it) => it.targetType === targetType && it.targetId === targetId)))
      .catch(() => {});
  }, [targetType, targetId, user]);

  if (user?.role === 'ADMIN') return null;

  async function toggle(e) {
    e.preventDefault(); e.stopPropagation();
    if (!user) {
      toast.info('로그인이 필요합니다');
      navigate('/login');
      return;
    }
    if (busy) return;
    setBusy(true);
    try {
      if (on) {
        await api.removeFavorite(targetType, targetId);
        setOn(false);
        toast.info('즐겨찾기에서 제거했어요');
      } else {
        await api.addFavorite(targetType, targetId);
        setOn(true);
        toast.success('즐겨찾기에 추가했어요');
      }
      PLACEHOLDER_CACHE.items = null;
    } catch (err) {
      toast.error('즐겨찾기 처리에 실패했습니다');
    } finally {
      setBusy(false);
    }
  }

  return (
    <button
      type="button"
      onClick={toggle}
      className={`${styles.btn} ${styles[`s_${size}`] || ''} ${on ? styles.on : ''}`}
      aria-pressed={on}
      aria-label={on ? '즐겨찾기 해제' : '즐겨찾기 추가'}
      disabled={busy}
    >
      <span aria-hidden="true">{on ? '♥' : '♡'}</span>
    </button>
  );
}
