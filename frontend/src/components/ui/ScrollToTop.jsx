import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

// Resets scroll to top on route change. Mount once near the router root.
export default function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
  }, [pathname]);
  return null;
}
