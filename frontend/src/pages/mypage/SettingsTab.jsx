import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import Button from '../../components/ui/Button.jsx';
import { useToast } from '../../components/ui/Toast.jsx';

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
      const list = await api.listFavorites();
      await Promise.all(list.map((f) => api.removeFavorite(f.targetType, f.targetId)));
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
        api.listFavorites(), api.history(), api.myReviews(), api.listCourses({ mine: true }),
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
    </div>
  );
}
