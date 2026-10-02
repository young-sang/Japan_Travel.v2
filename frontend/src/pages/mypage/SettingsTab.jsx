import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/client.js';
import { useAuth } from '../../auth/useAuth.js';
import Button from '../../components/ui/Button.jsx';
import Field from '../../components/ui/Field.jsx';
import { invalidateFavorites } from '../../components/FavButton.jsx';
import { useToast } from '../../components/ui/Toast.jsx';

const sectionTitle = { fontSize: 'var(--fs-lg)', fontWeight: 700, marginBottom: 'var(--space-3)' };
const formBox = {
  display: 'flex', flexDirection: 'column', gap: 'var(--space-3)', padding: 'var(--space-4)',
  background: 'var(--color-surface-1)', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)',
};

// 비밀번호 불일치(WRONG_PASSWORD)만 문구를 바꾸고 나머지는 서버 메시지를 그대로 쓴다
const errorText = (e) => (e.code === 'WRONG_PASSWORD' ? '비밀번호가 올바르지 않습니다' : e.message || '실패');

// 계정 관리 (D-056) — 닉네임 · 비밀번호
function AccountSection() {
  const { user, updateNickname } = useAuth();
  const toast = useToast();
  const [nickname, setNickname] = useState(user?.nickname || '');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [busy, setBusy] = useState('');

  async function saveNickname(e) {
    e.preventDefault();
    setBusy('nickname');
    try {
      await updateNickname(nickname);
      toast.success('닉네임을 바꿨어요');
    } catch (err) { toast.error(errorText(err)); }
    finally { setBusy(''); }
  }

  async function savePassword(e) {
    e.preventDefault();
    setBusy('password');
    try {
      await api.changePassword(currentPassword, newPassword);
      setCurrentPassword('');
      setNewPassword('');
      toast.success('비밀번호를 바꿨어요');
    } catch (err) { toast.error(errorText(err)); }
    finally { setBusy(''); }
  }

  return (
    <section>
      <h2 style={sectionTitle}>계정</h2>
      <div style={{ display: 'grid', gap: 'var(--space-3)' }}>
        <form onSubmit={saveNickname} style={formBox}>
          <Field label="아이디">
            <input type="text" value={user?.username || ''} disabled />
          </Field>
          <Field label="닉네임">
            <input type="text" value={nickname} onChange={(e) => setNickname(e.target.value)} />
          </Field>
          <Button type="submit" variant="secondary" loading={busy === 'nickname'}
            disabled={nickname === user?.nickname}>닉네임 저장</Button>
        </form>
        <form onSubmit={savePassword} style={formBox}>
          <Field label="현재 비밀번호">
            <input type="password" autoComplete="current-password" value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)} />
          </Field>
          <Field label="새 비밀번호">
            <input type="password" autoComplete="new-password" value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)} />
          </Field>
          <Button type="submit" variant="secondary" loading={busy === 'password'}
            disabled={!currentPassword || !newPassword}>비밀번호 변경</Button>
        </form>
      </div>
    </section>
  );
}

// 회원 탈퇴 (D-056) — 비밀번호 확인 후 지우고 홈으로
function WithdrawSection() {
  const { withdraw } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    if (!confirm('탈퇴하면 즐겨찾기 · 리뷰 · 코스 · 히스토리가 모두 삭제되고 되돌릴 수 없어요. 탈퇴할까요?')) return;
    setBusy(true);
    try {
      await withdraw(password);
      invalidateFavorites();
      toast.success('탈퇴했어요. 이용해 주셔서 감사합니다');
      navigate('/', { replace: true });
    } catch (err) {
      toast.error(errorText(err));
      setBusy(false);
    }
  }

  return (
    <section>
      <h2 style={{ ...sectionTitle, color: 'var(--color-danger)' }}>회원 탈퇴</h2>
      <form onSubmit={onSubmit} style={{ ...formBox, borderColor: 'var(--color-danger)' }}>
        <p style={{ margin: 0, fontSize: 'var(--fs-sm)', color: 'var(--color-ink-500)' }}>
          내 즐겨찾기 · 리뷰 · 코스 · 히스토리가 모두 삭제됩니다.
        </p>
        <Field label="비밀번호 확인">
          <input type="password" autoComplete="current-password" value={password}
            onChange={(e) => setPassword(e.target.value)} />
        </Field>
        <Button type="submit" variant="danger" loading={busy} disabled={!password}>탈퇴하기</Button>
      </form>
    </section>
  );
}

export default function SettingsTab() {
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
  const toast = useToast();

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem('theme', theme);
  }, [theme]);

  async function clearFavorites() {
    if (!confirm('모든 즐겨찾기를 삭제할까요?')) return;
    try {
      const fav = await api.listFavorites();
      await Promise.all([
        ...fav.destinations.map((d) => api.removeFavorite('destination', d.id)),
        ...fav.festivals.map((f) => api.removeFavorite('festival', f.id)),
      ]);
      invalidateFavorites();
      toast.success('즐겨찾기를 초기화했어요');
    } catch { toast.error('실패'); }
  }

  async function clearHistory() {
    if (!confirm('모든 히스토리를 삭제할까요?')) return;
    try {
      await api.clearHistory();
      toast.success('히스토리를 초기화했어요');
    } catch { toast.error('실패'); }
  }

  async function exportData() {
    try {
      const [fav, hist, revs, courses] = await Promise.all([
        api.listFavorites(), api.history(), api.myReviews(), api.myCourses(),
      ]);
      const blob = new Blob([JSON.stringify({ favorites: fav, history: hist, reviews: revs, courses }, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `japan-travel-mydata-${Date.now()}.json`; a.click();
      URL.revokeObjectURL(url);
      toast.success('내보내기를 완료했어요');
    } catch { toast.error('내보내기 실패'); }
  }

  return (
    <div style={{ maxWidth: 640, display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
      <AccountSection />

      <section>
        <h2 style={{ fontSize: 'var(--fs-lg)', fontWeight: 700, marginBottom: 'var(--space-3)' }}>화면</h2>
        <label style={{ display: 'flex', alignItems: 'center', gap: 12, padding: 'var(--space-3)', background: 'var(--color-surface-1)', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)' }}>
          <input type="checkbox" checked={theme === 'dark'} onChange={(e) => setTheme(e.target.checked ? 'dark' : 'light')} />
          <div>
            <div style={{ fontWeight: 600 }}>다크모드</div>
            <div style={{ fontSize: 'var(--fs-sm)', color: 'var(--color-ink-500)' }}>아직 베타입니다</div>
          </div>
        </label>
      </section>

      <section>
        <h2 style={{ fontSize: 'var(--fs-lg)', fontWeight: 700, marginBottom: 'var(--space-3)' }}>데이터</h2>
        <div style={{ display: 'grid', gap: 'var(--space-3)' }}>
          <Button variant="secondary" onClick={exportData}>📤 내 데이터 JSON 내보내기</Button>
          <Button variant="ghost" onClick={clearFavorites}>즐겨찾기 초기화</Button>
          <Button variant="ghost" onClick={clearHistory}>히스토리 초기화</Button>
        </div>
      </section>

      <WithdrawSection />
    </div>
  );
}
