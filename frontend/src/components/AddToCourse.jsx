import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';
import Button from './ui/Button.jsx';
import { useToast } from './ui/Toast.jsx';
import styles from './AddToCourse.module.css';

// 상세에서 코스로 담기 (D-094). "새 코스" 는 빌더를 이 장소로 채워 연다 — 담을 장소는 주소 쿼리로 넘긴다
// (상태 객체는 로그인 화면을 거치면 사라진다).
export default function AddToCourse({ type, id }) {
  const [open, setOpen] = useState(false);
  const box = useRef(null);
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();

  useEffect(() => {
    if (!open) return;
    const close = (e) => { if (!box.current?.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [open]);

  function toggle() {
    if (!user) {
      toast.info('로그인이 필요합니다');
      navigate('/login', { state: { from: location.pathname + location.search } });
      return;
    }
    setOpen((o) => !o);
  }

  return (
    <div className={styles.wrap} ref={box}>
      <Button variant="tint" size="sm" onClick={toggle} aria-expanded={open} aria-haspopup="menu">
        + 코스에 담기
      </Button>
      {open && (
        <div className={styles.menu} role="menu">
          <button type="button" role="menuitem" className={styles.item}
            onClick={() => navigate(`/mypage/courses/new?add=${type}-${id}`)}>
            <span className={styles.plus} aria-hidden="true">＋</span> 새 코스 만들기
          </button>
        </div>
      )}
    </div>
  );
}
