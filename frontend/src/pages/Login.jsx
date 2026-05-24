import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Container from '../components/ui/Container.jsx';
import Field from '../components/ui/Field.jsx';
import Button from '../components/ui/Button.jsx';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from '../components/ui/Toast.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);

  const from = location.state?.from || '/mypage';

  async function onSubmit(e) {
    e.preventDefault();
    if (!username.trim() || !password) { toast.warn('아이디와 비밀번호를 입력해주세요'); return; }
    setBusy(true);
    try {
      await login(username.trim(), password);
      toast.success('로그인되었습니다');
      navigate(from, { replace: true });
    } catch (err) {
      toast.error(err.body?.error || '로그인 실패');
    } finally {
      setBusy(false);
    }
  }

  return (
    <Container>
      <section style={{ maxWidth: 420, margin: '64px auto', padding: 'var(--space-6)',
        background: 'var(--color-surface-0)', border: '1px solid var(--color-border)',
        borderRadius: 'var(--radius-lg)' }}>
        <h1 style={{ marginTop: 0, fontSize: 'var(--fs-xl)', fontWeight: 700 }}>로그인</h1>
        <p style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)', marginBottom: 'var(--space-5)' }}>
          데모 계정: <code>admin</code> / <code>admin1234</code>
        </p>
        <form onSubmit={onSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          <Field label="아이디" required>
            <input type="text" autoComplete="username" value={username}
              onChange={(e) => setUsername(e.target.value)} autoFocus />
          </Field>
          <Field label="비밀번호" required>
            <input type="password" autoComplete="current-password" value={password}
              onChange={(e) => setPassword(e.target.value)} />
          </Field>
          <Button type="submit" variant="primary" loading={busy}>로그인</Button>
        </form>
        <div style={{ marginTop: 'var(--space-5)', textAlign: 'center', fontSize: 'var(--fs-sm)' }}>
          계정이 없으신가요? <Link to="/signup">가입하기</Link>
        </div>
      </section>
    </Container>
  );
}
