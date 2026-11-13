import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './useAuth.js';

export default function ProtectedRoute({ children, requireAdmin = false, denyAdmin = false }) {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div style={{ padding: 'var(--space-8)', textAlign: 'center', color: 'var(--color-ink-500)' }}>
        불러오는 중…
      </div>
    );
  }
  if (!user) {
    // 쿼리까지 기억한다 — 담아 온 장소(?add=…)가 로그인 뒤에도 빌더에 남도록 (D-094)
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  }
  if (requireAdmin && user.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }
  if (denyAdmin && user.role === 'ADMIN') {
    return <Navigate to="/admin" replace />;
  }
  return children;
}
