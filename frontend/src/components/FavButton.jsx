import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from './ui/Toast.jsx';
import styles from './FavButton.module.css';

// 하트 켜짐 여부는 내 즐겨찾기 목록에서 찾는다 (D-039). 카드 수십 개가 동시에 마운트돼도
// 요청이 한 번만 나가도록 "요청 중인 Promise" 를 공유한다 — 옛 캐시는 응답이 온 뒤에만 채워져
// 동시 마운트를 막지 못했다.
let shared = { userId: null, promise: null };

function loadFavorites(userId) {
  if (shared.userId !== userId || !shared.promise) {
    const promise = api.listFavorites().catch((e) => {
      if (shared.promise === promise) shared.promise = null;   // 실패는 공유하지 않는다
      throw e;
    });
    shared = { userId, promise };
  }
  return shared.promise;
}

export function invalidateFavorites() {
  shared.promise = null;
}

// 즐겨찾기 대상은 여행지 · 축제뿐이다 (D-036). 코스 카드에는 하트를 그리지 않는다.
const FAVORABLE = ['destination', 'festival'];

//  onChange(on) — 성공한 뒤에만 부른다. 마이페이지 찜 탭이 해제한 카드를 빼는 데 쓴다 (D-081)
export default function FavButton({ targetType, targetId, size = 'md', onChange }) {
  const [on, setOn] = useState(false);
  const [busy, setBusy] = useState(false);
  const toast = useToast();
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (!user || !FAVORABLE.includes(targetType)) { setOn(false); return; }
    let cancelled = false;
    loadFavorites(user.id)
      .then((fav) => {
        if (!cancelled) setOn((fav[`${targetType}s`] || []).some((it) => it.id === Number(targetId)));
      })
      .catch(() => {});
    return () => { cancelled = true; };
  }, [targetType, targetId, user]);

  if (!FAVORABLE.includes(targetType)) return null;

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
      invalidateFavorites();
      onChange?.(!on);
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
