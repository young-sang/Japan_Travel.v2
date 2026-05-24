import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Container from '../components/ui/Container.jsx';
import Field from '../components/ui/Field.jsx';
import Button from '../components/ui/Button.jsx';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from '../components/ui/Toast.jsx';

export default function Signup() {
  const { signup } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [busy, setBusy] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    if (username.trim().length < 3) { toast.warn('아이디는 3자 이상'); return; }
    if (password.length < 4) { toast.warn('비밀번호는 4자 이상'); return; }
    setBusy(true);
    try {
      await signup(username.trim(), password, nickname.trim() || username.trim());
      toast.success('가입 완료! 환영합니다.');
      navigate('/mypage', { replace: true });
    } catch (err) {
      toast.error(err.body?.error || '가입 실패');
    } finally {
      setBusy(false);
    }
  }

  return (
    <Container>
      <section style={{ maxWidth: 420, margin: '64px auto', padding: 'var(--space-6)',
        background: 'var(--color-surface-0)', border: '1px solid var(--color-border)',
        borderRadius: 'var(--radius-lg)' }}>
        <h1 style={{ marginTop: 0, fontSize: 'var(--fs-xl)', fontWeight: 700 }}>회원가입</h1>
        <p style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)', marginBottom: 'var(--space-5)' }}>
          사내 프로토타입 — 이메일 인증 없음
        </p>
        <form onSubmit={onSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          <Field label="아이디 (3~32자)" required>
            <input type="text" autoComplete="username" value={username}
              onChange={(e) => setUsername(e.target.value)} autoFocus />
          </Field>
          <Field label="비밀번호 (4자 이상)" required>
            <input type="password" autoComplete="new-password" value={password}
              onChange={(e) => setPassword(e.target.value)} />
          </Field>
          <Field label="닉네임" helper="화면에 표시될 이름 (비우면 아이디 사용)">
            <input type="text" value={nickname}
              onChange={(e) => setNickname(e.target.value)} />
          </Field>
          <Button type="submit" variant="primary" loading={busy}>가입하고 시작하기</Button>
        </form>
        <div style={{ marginTop: 'var(--space-5)', textAlign: 'center', fontSize: 'var(--fs-sm)' }}>
          이미 계정이 있나요? <Link to="/login">로그인</Link>
        </div>
      </section>
    </Container>
  );
}
