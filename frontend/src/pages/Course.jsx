import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { MapContainer, TileLayer, CircleMarker, Polyline, Popup } from 'react-leaflet';
import PrefectureGrid from '../components/PrefectureGrid.jsx';
import TagGrid from '../components/TagGrid.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import Tag from '../components/ui/Tag.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Modal from '../components/ui/Modal.jsx';
import { SkeletonGrid } from '../components/ui/Skeleton.jsx';
import ListPageLayout, { ResultsGrid } from '../layouts/ListPageLayout.jsx';
import { useToast } from '../components/ui/Toast.jsx';

const COURSE_TAGS = ['역사', '문화유산', '쇼핑', '음식', '바다', '드라이브', '자연', '당일여행', '가족여행', '신혼여행'];

export default function Course() {
  const [prefecture, setPrefecture] = useState('');
  const [tag, setTag] = useState('');
  const [items, setItems] = useState(null);
  const [modal, setModal] = useState(null);
  const toast = useToast();

  async function load() {
    setItems(null);
    try {
      const list = await api.listCourses({ prefecture: prefecture || undefined, tag: tag || undefined });
      setItems(list);
    } catch (e) {
      toast.error('코스를 불러오지 못했습니다');
      setItems([]);
    }
  }
  useEffect(() => { load(); }, [prefecture, tag]);

  function random() {
    if (!items || items.length === 0) return;
    setModal(items[Math.floor(Math.random() * items.length)]);
  }

  return (
    <Container size="wide">
      <PageHeader
        eyebrow="여행 코스"
        title="추천 코스 둘러보기"
        subtitle="다른 여행자들이 만든 일정에서 영감을 얻어보세요"
        actions={<Link to="/mypage/courses/new"><Button variant="primary" size="md">+ 내 코스 만들기</Button></Link>}
      />
      <ListPageLayout
        sidebar={
          <>
            <PrefectureGrid value={prefecture} onChange={setPrefecture} title="지역" />
            <TagGrid tags={COURSE_TAGS} value={tag} onChange={setTag} title="테마" />
            {(prefecture || tag) && (
              <Button variant="ghost" size="sm" onClick={() => { setPrefecture(''); setTag(''); }}>필터 초기화</Button>
            )}
          </>
        }
        toolbar={
          <>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
              <Tag variant="brand" size="md">{prefecture || '전체'}{tag && ` · ${tag}`}</Tag>
              <span style={{ color: 'var(--color-ink-500)', fontSize: 'var(--fs-sm)' }}>
                {items === null ? '불러오는 중…' : `결과 ${items.length}건`}
              </span>
            </div>
            <Button variant="secondary" size="sm" onClick={random} disabled={!items?.length}>🎲 랜덤</Button>
          </>
        }
      >
        {items === null && <SkeletonGrid count={6} />}
        {items !== null && items.length === 0 && (
          <EmptyState
            icon="🗺️"
            title="아직 등록된 코스가 없어요"
            description="나만의 일정을 만들어 다른 사람과 공유해보세요."
            primaryAction={<Link to="/mypage/courses/new"><Button variant="primary">+ 내 코스 만들기</Button></Link>}
          />
        )}
        {items !== null && items.length > 0 && (
          <ResultsGrid>
            {items.map((c) => (
              <PlaceCard
                key={c.id}
                type="course"
                item={c}
                onClick={() => setModal(c)}
              />
            ))}
          </ResultsGrid>
        )}
      </ListPageLayout>

      {modal && <CourseModal course={modal} onClose={() => setModal(null)} />}
    </Container>
  );
}

function CourseModal({ course, onClose }) {
  const toast = useToast();
  const stops = course.timeline || [];
  const coords = stops.filter((s) => s.lat && s.lng).map((s) => [s.lat, s.lng]);
  const center = course.centerLat ? [course.centerLat, course.centerLng] : (coords[0] || [36.5, 136.0]);

  async function copyLink() {
    try {
      await navigator.clipboard.writeText(window.location.origin + `/detail/course/${course.id}`);
      toast.success('링크를 복사했습니다');
    } catch {
      toast.error('복사 실패');
    }
  }

  return (
    <Modal open onClose={onClose} title={course.title} size="xl">
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-5)', minHeight: 480 }}>
        <div>
          <div style={{ color: 'var(--color-ink-500)', marginBottom: 'var(--space-4)', fontSize: 'var(--fs-sm)' }}>
            {course.prefecture} · {course.duration}
            {course.tags?.length > 0 && (
              <> · {(course.tags || []).map((t) => '#' + t).join(' ')}</>
            )}
          </div>
          <div>
            {stops.map((s, idx) => (
              <div key={idx} style={{ padding: '12px 0', borderBottom: '1px solid var(--color-border)' }}>
                <div>
                  <span style={{ display: 'inline-block', minWidth: 64, color: 'var(--color-brand-500)', fontWeight: 700 }}>{s.time}</span>
                  <strong style={{ marginLeft: 8 }}>{s.title}</strong>
                </div>
                {s.desc && <p style={{ margin: '6px 0 0 72px', color: 'var(--color-ink-700)' }}>{s.desc}</p>}
              </div>
            ))}
          </div>
          <div style={{ marginTop: 'var(--space-4)', display: 'flex', gap: 8 }}>
            <Button variant="secondary" size="sm" onClick={copyLink}>🔗 공유 링크 복사</Button>
            <Link to={`/detail/course/${course.id}`}><Button variant="primary" size="sm">자세히 보기</Button></Link>
          </div>
        </div>
        <div style={{ minHeight: 480, borderRadius: 'var(--radius-md)', overflow: 'hidden', border: '1px solid var(--color-border)' }}>
          <MapContainer center={center} zoom={11} style={{ height: '100%', minHeight: 480, width: '100%' }}>
            <TileLayer attribution='&copy; OpenStreetMap' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            {stops.filter(s => s.lat && s.lng).map((s, idx) => (
              <CircleMarker key={idx} center={[s.lat, s.lng]} radius={12}
                pathOptions={{ color: '#fff', weight: 2, fillColor: '#E41A1C', fillOpacity: 1 }}>
                <Popup><strong>{idx + 1}. {s.title}</strong><br />{s.time}</Popup>
              </CircleMarker>
            ))}
            {coords.length > 1 && <Polyline positions={coords} pathOptions={{ color: '#E41A1C', weight: 3, opacity: 0.7 }} />}
          </MapContainer>
        </div>
      </div>
    </Modal>
  );
}
