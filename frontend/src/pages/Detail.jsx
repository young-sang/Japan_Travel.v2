import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { MapContainer, TileLayer, CircleMarker, Polyline, Popup } from 'react-leaflet';
import FavButton from '../components/FavButton.jsx';
import ReviewSection from '../components/ReviewSection.jsx';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import IconButton from '../components/ui/IconButton.jsx';
import Tag from '../components/ui/Tag.jsx';
import Spinner from '../components/ui/Spinner.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Button from '../components/ui/Button.jsx';
import { useToast } from '../components/ui/Toast.jsx';
import styles from './Detail.module.css';

const CATEGORY_LABEL = { destination: '여행지', festival: '축제', course: '코스' };

export default function Detail() {
  const { type, id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const [item, setItem] = useState(null);
  const [error, setError] = useState(false);
  const [nearby, setNearby] = useState([]);

  useEffect(() => {
    let cancelled = false;
    setItem(null); setError(false); setNearby([]);
    const fn = type === 'destination' ? api.getDestination
              : type === 'festival' ? api.getFestival
              : api.getCourse;
    fn(id)
      .then((d) => {
        if (cancelled) return;
        setItem(d);
        api.touchHistory(type, Number(id)).catch(() => {});
        if (d && d.prefecture) {
          api.listDestinations({ prefecture: d.prefecture })
            .then((list) => { if (!cancelled) setNearby(list.filter((x) => !(type === 'destination' && x.id === d.id)).slice(0, 6)); })
            .catch(() => {});
        }
      })
      .catch(() => { if (!cancelled) { setError(true); toast.error('정보를 불러오지 못했습니다'); } });
    return () => { cancelled = true; };
  }, [type, id]);

  async function share() {
    const url = window.location.href;
    if (navigator.share) {
      try { await navigator.share({ title: item?.name || item?.title, url }); }
      catch {}
    } else {
      try { await navigator.clipboard.writeText(url); toast.success('링크를 복사했습니다'); }
      catch { toast.error('복사 실패'); }
    }
  }

  if (error) {
    return (
      <Container size="base">
        <EmptyState
          icon="😕"
          title="정보를 찾을 수 없어요"
          description="요청한 항목이 삭제되었거나 일시적인 오류일 수 있습니다."
          primaryAction={<Button variant="primary" onClick={() => navigate(-1)}>이전으로</Button>}
          secondaryAction={<Button variant="ghost" onClick={() => navigate('/')}>홈으로</Button>}
        />
      </Container>
    );
  }

  if (!item) {
    return (
      <Container size="base">
        <div className={styles.loadingWrap}>
          <Spinner size={40} />
          <p>불러오는 중…</p>
        </div>
      </Container>
    );
  }

  const name = item.name || item.title;
  const desc = item.description;
  const img = item.imagePath;
  const lat = item.lat || item.centerLat;
  const lng = item.lng || item.centerLng;
  const timeline = item.timeline || [];
  const metaItems = [item.prefecture, item.dateText, item.duration].filter(Boolean);

  return (
    <article className={styles.page}>
      {/* HERO */}
      <section className={styles.hero}>
        {img ? (
          <img
            className={styles.heroImg}
            src={img}
            alt={name}
            onError={(e) => { if (e.currentTarget.dataset.fb === '1') return; e.currentTarget.dataset.fb = '1'; e.currentTarget.src = '/placeholder.svg'; }}
          />
        ) : (
          <div className={styles.heroFallback} aria-hidden="true">🗾</div>
        )}
        <div className={styles.heroOverlay}>
          <div className={styles.heroLeft}>
            <IconButton label="뒤로 가기" variant="overlay" onClick={() => navigate(-1)}>
              <span aria-hidden="true">←</span>
            </IconButton>
          </div>
          <div className={styles.heroRight}>
            <IconButton label="공유" variant="overlay" onClick={share}>
              <span aria-hidden="true">↗</span>
            </IconButton>
            <div className={styles.favWrap}>
              <FavButton targetType={type} targetId={Number(id)} size="lg" />
            </div>
          </div>
        </div>
      </section>

      <Container size="wide">
        {/* Title block */}
        <header className={styles.titleBlock}>
          <Tag variant="brand" size="sm">{CATEGORY_LABEL[type] || ''}</Tag>
          <h1 className={styles.title}>{name}</h1>
          {metaItems.length > 0 && (
            <p className={styles.meta}>
              <span aria-hidden="true">📍</span> {metaItems.join(' · ')}
            </p>
          )}
          {(item.tags || []).length > 0 && (
            <div className={styles.tags}>
              {item.tags.map((t) => <Tag key={t} variant="neutral" size="sm">#{t}</Tag>)}
            </div>
          )}
        </header>

        {/* Two-column body */}
        <div className={styles.body}>
          <div className={styles.main}>
            {desc && (
              <section className={styles.section}>
                <h2 className={styles.h2}>소개</h2>
                <p className={styles.desc}>{desc}</p>
              </section>
            )}

            {timeline.length > 0 && (
              <section className={styles.section}>
                <h2 className={styles.h2}>일정</h2>
                <ol className={styles.timeline}>
                  {timeline.map((s, i) => (
                    <li key={i} className={styles.timelineItem}>
                      <span className={styles.timelineDot} aria-hidden="true">{i + 1}</span>
                      <div className={styles.timelineContent}>
                        <div className={styles.timelineHead}>
                          <span className={styles.timelineTime}>{s.time}</span>
                          <strong>{s.title}</strong>
                        </div>
                        {s.desc && <p className={styles.timelineDesc}>{s.desc}</p>}
                      </div>
                    </li>
                  ))}
                </ol>
              </section>
            )}

            <section className={styles.section}>
              <ReviewSection targetType={type} targetId={Number(id)} />
            </section>
          </div>

          <aside className={styles.side}>
            <div className={styles.sideCard}>
              <h3 className={styles.sideTitle}>정보</h3>
              <dl className={styles.dl}>
                {item.prefecture && (<><dt>지역</dt><dd>{item.prefecture}</dd></>)}
                {item.dateText && (<><dt>일정</dt><dd>{item.dateText}</dd></>)}
                {item.duration && (<><dt>소요</dt><dd>{item.duration}</dd></>)}
                {item.address && (<><dt>주소</dt><dd>{item.address}</dd></>)}
              </dl>
            </div>
            {lat && lng && (
              <div className={styles.miniMap}>
                <h3 className={styles.sideTitle}>지도</h3>
                <div className={styles.miniMapInner}>
                  <MapContainer center={[lat, lng]} zoom={13} style={{ height: '100%', width: '100%' }}>
                    <TileLayer attribution='&copy; OpenStreetMap' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                    <CircleMarker center={[lat, lng]} radius={10} pathOptions={{ color: '#fff', weight: 2, fillColor: '#E41A1C', fillOpacity: 1 }}>
                      <Popup>{name}</Popup>
                    </CircleMarker>
                    {timeline.length > 1 && (
                      <Polyline
                        positions={timeline.filter((s) => s.lat && s.lng).map((s) => [s.lat, s.lng])}
                        pathOptions={{ color: '#E41A1C', weight: 3, opacity: 0.7 }}
                      />
                    )}
                  </MapContainer>
                </div>
              </div>
            )}
          </aside>
        </div>

        {/* Nearby - promoted */}
        {nearby.length > 0 && (
          <section className={styles.nearby}>
            <div className={styles.nearbyHead}>
              <h2 className={styles.h2}>근처 추천</h2>
              <Link to={`/destination?prefecture=${encodeURIComponent(item.prefecture)}`}
                    className={styles.nearbyMore}>{item.prefecture} 더 보기 →</Link>
            </div>
            <div className={styles.nearbyScroll}>
              {nearby.map((n) => (
                <Link key={n.id} to={`/detail/destination/${n.id}`} className={styles.nearbyCard}>
                  <div className={styles.nearbyMedia}>
                    <img
                      src={n.imagePath || '/placeholder.svg'}
                      alt={n.name}
                      loading="lazy"
                      onError={(e) => { if (e.currentTarget.dataset.fb === '1') return; e.currentTarget.dataset.fb = '1'; e.currentTarget.src = '/placeholder.svg'; }}
                    />
                  </div>
                  <p className={styles.nearbyName}>{n.name}</p>
                </Link>
              ))}
            </div>
          </section>
        )}
      </Container>
    </article>
  );
}
