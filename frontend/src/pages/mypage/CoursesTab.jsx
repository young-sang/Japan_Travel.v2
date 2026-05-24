import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import Tag from '../../components/ui/Tag.jsx';
import Button from '../../components/ui/Button.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import { SkeletonGrid } from '../../components/ui/Skeleton.jsx';
import { useToast } from '../../components/ui/Toast.jsx';

const FILTERS = [
  { key: '', label: '전체' },
  { key: 'draft', label: '임시저장' },
  { key: 'published', label: '발행됨' },
];

export default function CoursesTab() {
  const [courses, setCourses] = useState(null);
  const [status, setStatus] = useState('');
  const toast = useToast();

  async function reload() {
    setCourses(null);
    try {
      const list = await api.listCourses({ ownerUserId: 1, status: status || undefined });
      setCourses(list);
    } catch {
      toast.error('코스를 불러오지 못했습니다');
      setCourses([]);
    }
  }
  useEffect(() => { reload(); }, [status]);

  async function remove(id) {
    if (!confirm('이 코스를 삭제할까요?')) return;
    try {
      await api.deleteCourse(id);
      toast.info('코스를 삭제했어요');
      reload();
    } catch { toast.error('삭제 실패'); }
  }

  async function duplicate(c) {
    try {
      const { id, createdAt, ...body } = c;
      body.title = c.title + ' (복사본)';
      await api.createCourse(body);
      toast.success('복제했어요');
      reload();
    } catch { toast.error('복제 실패'); }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--space-5)', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
        <div style={{ display: 'flex', gap: 8 }}>
          {FILTERS.map((f) => (
            <Tag key={f.key} size="md" selected={status === f.key} onClick={() => setStatus(f.key)}>{f.label}</Tag>
          ))}
        </div>
        <Link to="/mypage/courses/new"><Button variant="primary">+ 새 코스 만들기</Button></Link>
      </div>
      {courses === null && <SkeletonGrid count={4} />}
      {courses !== null && courses.length === 0 && (
        <EmptyState
          icon="🗒️"
          title="아직 만든 코스가 없어요"
          description="나만의 일정을 만들어 공유해보세요."
          primaryAction={<Link to="/mypage/courses/new"><Button variant="primary">+ 새 코스 만들기</Button></Link>}
        />
      )}
      {courses !== null && courses.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 'var(--space-4)' }}>
          {courses.map((c) => (
            <article key={c.id} style={{
              border: '1px solid var(--color-border)', borderRadius: 'var(--radius-lg)',
              padding: 'var(--space-4)', background: 'var(--color-surface-0)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', gap: 8 }}>
                <h3 style={{ margin: 0, fontSize: 'var(--fs-lg)', fontWeight: 700 }}>{c.title}</h3>
                <Tag size="sm" variant={c.status === 'draft' ? 'neutral' : 'success'}>
                  {c.status === 'draft' ? '임시저장' : '발행됨'}
                </Tag>
              </div>
              <p style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)', margin: '8px 0 0' }}>
                {c.prefecture} · {c.duration} · {(c.timeline || []).length}개 일정
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
