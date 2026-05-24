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
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (requireAdmin && user.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }
  if (denyAdmin && user.role === 'ADMIN') {
    return <Navigate to="/admin" replace />;
  }
  return children;
}
