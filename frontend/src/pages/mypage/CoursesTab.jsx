import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import Tag from '../../components/ui/Tag.jsx';
import Button from '../../components/ui/Button.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import { SkeletonGrid } from '../../components/ui/Skeleton.jsx';
import { useToast } from '../../components/ui/Toast.jsx';

// 임시저장은 없다 — 공개 / 비공개만 있다 (D-041). 필터는 받아 온 목록에서 거른다.
const FILTERS = [
  { key: '', label: '전체' },
  { key: 'public', label: '공개' },
  { key: 'private', label: '비공개' },
];

export default function CoursesTab() {
  const [courses, setCourses] = useState(null);
  const [filter, setFilter] = useState('');
  const toast = useToast();

  async function reload() {
    setCourses(null);
    try {
      setCourses(await api.myCourses());
    } catch {
      toast.error('코스를 불러오지 못했습니다');
      setCourses([]);
    }
  }
  useEffect(() => { reload(); }, []);

  async function remove(id) {
    if (!confirm('이 코스를 삭제할까요?')) return;
    try {
      await api.deleteCourse(id);
      toast.info('코스를 삭제했어요');
      reload();
    } catch { toast.error('삭제 실패'); }
  }

  // 목록은 요약이라 정류장이 없다 — 상세를 받아 요청 모양(D-046)으로 바꿔 새로 만든다
  async function duplicate(c) {
    try {
      const d = await api.getCourse(c.id);
      await api.createCourse({
        title: d.title + ' (복사본)',
        description: d.description,
        prefecture: d.prefecture,
        imagePath: d.imagePath,
        isPublic: false,
        stops: d.stops.map((s) => ({ dayNo: s.dayNo, type: s.type, targetId: s.targetId, memo: s.memo })),
      });
      toast.success('비공개 복사본을 만들었어요');
      reload();
    } catch { toast.error('복제 실패'); }
  }

  const shown = (courses || []).filter((c) =>
    filter === 'public' ? c.isPublic : filter === 'private' ? !c.isPublic : true);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--space-5)', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
        <div style={{ display: 'flex', gap: 8 }}>
          {FILTERS.map((f) => (
            <Tag key={f.key} size="md" selected={filter === f.key} onClick={() => setFilter(f.key)}>{f.label}</Tag>
          ))}
        </div>
        <Link to="/mypage/courses/new"><Button variant="primary">+ 새 코스 만들기</Button></Link>
      </div>
      {courses === null && <SkeletonGrid count={4} />}
      {courses !== null && shown.length === 0 && (
        <EmptyState
          icon="🗒️"
          title="아직 만든 코스가 없어요"
          description="나만의 일정을 만들어 공유해보세요."
          primaryAction={<Link to="/mypage/courses/new"><Button variant="primary">+ 새 코스 만들기</Button></Link>}
        />
      )}
      {courses !== null && shown.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 'var(--space-4)' }}>
          {shown.map((c) => (
            <article key={c.id} style={{
              border: '1px solid var(--color-border)', borderRadius: 'var(--radius-lg)',
              padding: 'var(--space-4)', background: 'var(--color-surface-0)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', gap: 8 }}>
                <h3 style={{ margin: 0, fontSize: 'var(--fs-lg)', fontWeight: 700 }}>{c.title}</h3>
                <Tag size="sm" variant={c.isPublic ? 'success' : 'neutral'}>
                  {c.isPublic ? '공개' : '비공개'}
                </Tag>
              </div>
              <p style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)', margin: '8px 0 0' }}>
                {c.prefecture} · {String(c.createdAt || '').slice(0, 10)} 만듦
              </p>
              <div style={{ marginTop: 'var(--space-4)', display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                <Link to={`/detail/course/${c.id}`}><Button size="sm" variant="secondary">보기</Button></Link>
                <Link to={`/mypage/courses/${c.id}/edit`}><Button size="sm" variant="secondary">편집</Button></Link>
                <Button size="sm" variant="ghost" onClick={() => duplicate(c)}>복제</Button>
                <Button size="sm" variant="ghost" onClick={() => remove(c.id)}>삭제</Button>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
