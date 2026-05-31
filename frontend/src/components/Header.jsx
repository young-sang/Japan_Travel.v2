import { useState, useEffect, useRef } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import GlobalMapOverlay from './GlobalMapOverlay.jsx';
import SearchOverlay from './SearchOverlay.jsx';
import WeatherWidget from './WeatherWidget.jsx';
import FxWidget from './FxWidget.jsx';
import IconButton from './ui/IconButton.jsx';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from './ui/Toast.jsx';
import styles from './Header.module.css';

const NAV = [
  { to: '/board', label: '자유게시판' },
  { to: '/destination', label: '여행지' },
  { to: '/festival', label: '축제' },
  { to: '/course', label: '여행코스' },
];

export default function Header() {
  const [searchOpen, setSearchOpen] = useState(false);
  const [mapOpen, setMapOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [userOpen, setUserOpen] = useState(false);
  const userRef = useRef(null);
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const toast = useToast();

  async function onLogout() {
    setUserOpen(false);
    await logout();
    toast.info('로그아웃되었습니다');
    navigate('/');
  }

  useEffect(() => {
    const onKey = (e) => {
      if (e.key === 'Escape') { setSearchOpen(false); setMapOpen(false); setUserOpen(false); setMenuOpen(false); }
      // Ctrl/Cmd + K opens search
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setSearchOpen((v) => !v);
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  useEffect(() => {
    const onClick = (e) => {
      if (userRef.current && !userRef.current.contains(e.target)) setUserOpen(false);
    };
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  return (
    <>
      <header className={styles.header} role="banner">
        <div className={styles.inner}>
          <div className={styles.left}>
            <Link to="/" className={styles.brand} aria-label="홈으로">
              <span className={styles.logoIcon}>
                <img src="/image/index_logo.png" alt="" aria-hidden="true" />
              </span>
              <span className={styles.logoText}>일본 구석구석</span>
            </Link>
            <nav className={styles.gnb} aria-label="주 메뉴">
              <ul>
                {NAV.map((n) => (
                  <li key={n.to}>
                    <NavLink
                      to={n.to}
                      className={({ isActive }) =>
                        `${styles.navLink} ${isActive ? styles.active : ''}`
                      }
                    >{n.label}</NavLink>
                  </li>
                ))}
              </ul>
            </nav>
          </div>

          <div className={styles.right}>
            <div className={styles.widgets}>
              <WeatherWidget />
              <FxWidget />
            </div>
            <IconButton label="지도 보기" onClick={() => setMapOpen(true)}>
              <span aria-hidden="true">🗺️</span>
            </IconButton>
            <IconButton label="검색 (Ctrl+K)" onClick={() => setSearchOpen(true)}>
              <span aria-hidden="true">🔍</span>
            </IconButton>
            <div className={styles.user} ref={userRef}>
              <IconButton
                label="사용자 메뉴"
                onClick={() => setUserOpen((v) => !v)}
                aria-expanded={userOpen}
              >
                <span aria-hidden="true">{user ? '🙂' : '👤'}</span>
              </IconButton>
              {userOpen && (
                <div className={styles.dropdown} role="menu">
                  {user ? (
                    <>
                      <div style={{ padding: '6px 12px', fontSize: 'var(--fs-sm)', color: 'var(--color-ink-500)' }}>
                        {user.nickname} <span style={{ opacity: 0.6 }}>({user.username})</span>
                      </div>
                      <div className={styles.dropdownDivider} />
                      {user.role === 'ADMIN' ? (
                        <button type="button" role="menuitem" onClick={() => { setUserOpen(false); navigate('/admin'); }}>🛠 관리자</button>
                      ) : (
                        <>
                          <button type="button" role="menuitem" onClick={() => { setUserOpen(false); navigate('/mypage'); }}>마이페이지</button>
                          <button type="button" role="menuitem" onClick={() => { setUserOpen(false); navigate('/mypage/favorites'); }}>즐겨찾기</button>
                          <button type="button" role="menuitem" onClick={() => { setUserOpen(false); navigate('/mypage/courses'); }}>내 코스</button>
                        </>
                      )}
                      <div className={styles.dropdownDivider} />
                      <button type="button" role="menuitem" onClick={onLogout}>로그아웃</button>
                    </>
                  ) : (
                    <>
                      <button type="button" role="menuitem" onClick={() => { setUserOpen(false); navigate('/login'); }}>로그인</button>
                      <button type="button" role="menuitem" onClick={() => { setUserOpen(false); navigate('/signup'); }}>가입</button>
                    </>
                  )}
                </div>
              )}
            </div>
            <IconButton
              label="메뉴 열기"
              className={styles.hamburger}
              onClick={() => setMenuOpen((v) => !v)}
              aria-expanded={menuOpen}
            >
              <span aria-hidden="true">{menuOpen ? '✕' : '☰'}</span>
            </IconButton>
          </div>
        </div>

        {menuOpen && (
          <div className={styles.mobileMenu} role="menu">
            {NAV.map((n) => (
              <NavLink
                key={n.to}
                to={n.to}
                className={({ isActive }) =>
                  `${styles.mobileLink} ${isActive ? styles.mobileActive : ''}`
                }
                onClick={() => setMenuOpen(false)}
                role="menuitem"
              >{n.label}</NavLink>
            ))}
            <div className={styles.mobileDivider} />
            {user ? (
              <>
                {user.role === 'ADMIN' ? (
                  <button type="button" className={styles.mobileLink} onClick={() => { setMenuOpen(false); navigate('/admin'); }} role="menuitem">관리자</button>
                ) : (
                  <button type="button" className={styles.mobileLink} onClick={() => { setMenuOpen(false); navigate('/mypage'); }} role="menuitem">마이페이지</button>
                )}
                <button type="button" className={styles.mobileLink} onClick={() => { setMenuOpen(false); onLogout(); }} role="menuitem">로그아웃</button>
              </>
            ) : (
              <>
                <button type="button" className={styles.mobileLink} onClick={() => { setMenuOpen(false); navigate('/login'); }} role="menuitem">로그인</button>
                <button type="button" className={styles.mobileLink} onClick={() => { setMenuOpen(false); navigate('/signup'); }} role="menuitem">가입</button>
              </>
            )}
          </div>
        )}
      </header>

      <SearchOverlay open={searchOpen} onClose={() => setSearchOpen(false)} />
      <GlobalMapOverlay open={mapOpen} onClose={() => setMapOpen(false)} />
    </>
  );
}
