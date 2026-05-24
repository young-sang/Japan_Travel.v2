import { Routes, Route, NavLink, Navigate } from 'react-router-dom';
import AdminDashboard from './admin/AdminDashboard.jsx';
import AdminContent from './admin/AdminContent.jsx';
import CollectionStatus from './admin/CollectionStatus.jsx';
import AdminReviews from './admin/AdminReviews.jsx';
import AdminSystem from './admin/AdminSystem.jsx';
import AdminUsers from './admin/AdminUsers.jsx';
import AdminAudit from './admin/AdminAudit.jsx';

const NAV = [
  { to: '/admin', label: '대시보드', end: true },
  { to: '/admin/content', label: '콘텐츠' },
  { to: '/admin/collection-status', label: '수집 현황' },
  { to: '/admin/reviews', label: '사용자 데이터' },
  { to: '/admin/users', label: '사용자' },
  { to: '/admin/audit', label: '감사 로그' },
  { to: '/admin/system', label: '시스템' },
];

export default function Admin() {
  return (
    <main style={{ maxWidth: 1200, margin: '20px auto', padding: '0 20px' }}>
      <h1>🛠 관리자</h1>
      <nav style={{ borderBottom: '1px solid #eee', margin: '20px 0' }}>
        <ul style={{ display: 'flex', gap: 24, listStyle: 'none', padding: 0, margin: 0, flexWrap: 'wrap' }}>
          {NAV.map((n) => (
            <li key={n.to}>
              <NavLink to={n.to} end={n.end}
                       style={({ isActive }) => ({
                         display: 'inline-block', padding: '12px 0', textDecoration: 'none',
                         color: isActive ? '#e91e63' : '#666',
                         borderBottom: isActive ? '2px solid #e91e63' : '2px solid transparent',
                         fontWeight: isActive ? 700 : 500
                       })}>
                {n.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
      <Routes>
        <Route index element={<AdminDashboard />} />
        <Route path="content" element={<AdminContent />} />
        <Route path="collection-status" element={<CollectionStatus />} />
        <Route path="reviews" element={<AdminReviews />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="audit" element={<AdminAudit />} />
        <Route path="system" element={<AdminSystem />} />
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </main>
  );
}
