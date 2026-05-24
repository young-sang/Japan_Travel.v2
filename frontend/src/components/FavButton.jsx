import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { useToast } from './ui/Toast.jsx';
import styles from './FavButton.module.css';

const PLACEHOLDER_CACHE = { items: null, ts: 0 };

async function fetchOnce() {
  if (PLACEHOLDER_CACHE.items && Date.now() - PLACEHOLDER_CACHE.ts < 5000) return PLACEHOLDER_CACHE.items;
  const items = await api.listFavorites();
  PLACEHOLDER_CACHE.items = items;
  PLACEHOLDER_CACHE.ts = Date.now();
  return items;
}

export default function FavButton({ targetType, targetId, size = 'md' }) {
  const [on, setOn] = useState(false);
  const [busy, setBusy] = useState(false);
  const toast = useToast();

  useEffect(() => {
    fetchOnce()
      .then((items) => setOn(items.some((it) => it.targetType === targetType && it.targetId === targetId)))
      .catch(() => {});
  }, [targetType, targetId]);

  async function toggle(e) {
    e.preventDefault(); e.stopPropagation();
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
