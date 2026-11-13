import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import Button from './ui/Button.jsx';
import { useToast } from './ui/Toast.jsx';
import styles from './AddToCourse.module.css';

// 상세에서 코스로 담기 (D-094). "새 코스" 는 빌더를 이 장소로 채워 연다 — 담을 장소는 주소 쿼리로 넘긴다
// (상태 객체는 로그인 화면을 거치면 사라진다). 내 코스를 고르면 마지막 일차 끝에 더하고 메뉴에 표시한다.
export default function AddToCourse({ type, id }) {
  const [open, setOpen] = useState(false);
  const [courses, setCourses] = useState(null);
  const [added, setAdded] = useState({});   // 코스 id → true (이번에 담은 코스)
  const [busy, setBusy] = useState(null);
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
    if (courses === null) api.myCourses().then(setCourses).catch(() => setCourses([]));
  }

  async function addTo(course) {
    if (busy || added[course.id]) return;
    setBusy(course.id);
    try {
      await api.addCourseStop(course.id, type, id);
      setAdded((a) => ({ ...a, [course.id]: true }));
      toast.success(`'${course.title}' 에 담았습니다`);
    } catch (e) {
      toast.error(e.message || '코스에 담지 못했습니다');
    } finally {
      setBusy(null);
    }
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
          <div className={styles.label}>내 코스에 추가</div>
          {courses === null && <div className={styles.label}>불러오는 중…</div>}
          {courses?.length === 0 && <div className={styles.label}>아직 만든 코스가 없어요</div>}
          {courses?.map((c) => (
            <button key={c.id} type="button" role="menuitem" className={styles.item}
              onClick={() => addTo(c)} disabled={busy === c.id}>
              {c.title}
              {added[c.id]
                ? <Link to={`/detail/course/${c.id}`} className={styles.sub} onClick={(e) => e.stopPropagation()}>✓ 담음 · 보기</Link>
                : <span className={styles.sub}>{c.isPublic ? '' : '비공개'}</span>}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
