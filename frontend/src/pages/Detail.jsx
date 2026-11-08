import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { MapContainer, TileLayer, CircleMarker, Polyline, Popup } from 'react-leaflet';
import FavButton from '../components/FavButton.jsx';
import ReviewSection from '../components/ReviewSection.jsx';
import { api, tokenStore } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import IconButton from '../components/ui/IconButton.jsx';
import Tag from '../components/ui/Tag.jsx';
import Spinner from '../components/ui/Spinner.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Button from '../components/ui/Button.jsx';
import { useToast } from '../components/ui/Toast.jsx';
import styles from './Detail.module.css';
import usePageTitle from '../utils/usePageTitle.js';

const CATEGORY_LABEL = { destination: '여행지', festival: '축제', course: '코스' };

// 최근 본 장소 · 리뷰 · 즐겨찾기는 여행지 · 축제만 대상이다 (D-036 · D-052). 코스는 셋 다 없다.
const isPlace = (type) => type === 'destination' || type === 'festival';

// 코스 상세의 정류장(D-046) → 옛 타임라인 모양. 시간은 없고(D-043) 일차를 대신 보인다.
function stopsToTimeline(stops = []) {
  return stops.map((s) => ({
    time: `${s.dayNo}일차`,
    title: s.name,
    desc: [s.prefecture, s.memo].filter(Boolean).join(' · '),
    lat: s.lat,
    lng: s.lng,
    href: `/detail/${s.type.toLowerCase()}/${s.targetId}`,
  }));
}

export default function Detail() {
  const { type, id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const [item, setItem] = useState(null);
  const [error, setError] = useState(false);
  const [nearby, setNearby] = useState([]);
  usePageTitle(item?.name || item?.title);

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
        // 비로그인이면 부르지 않는다(401 이 콘솔에 남는다). user 는 새로고침 직후 아직 비어 있을 수 있어 토큰으로 본다
        if (isPlace(type) && tokenStore.get()) api.touchHistory(type, Number(id)).catch(() => {});
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
  const timeline = type === 'course' ? stopsToTimeline(item.stops) : [];
  // 코스는 좌표가 없으므로 좌표가 있는 첫 정류장을 지도 중심으로 쓴다
  const firstWithCoords = timeline.find((s) => s.lat && s.lng);
  const lat = item.lat || firstWithCoords?.lat;
  const lng = item.lng || firstWithCoords?.lng;
  const owner = type === 'course' ? (item.ownerNickname || '기본 제공') : null;
  const days = timeline.length ? Math.max(...item.stops.map((s) => s.dayNo)) : 0;
  const duration = type === 'course' && days ? `${days}일` : null;
  const metaItems = [item.prefecture, item.dateText, duration].filter(Boolean);

  return (
    <article className={styles.page}>
      {/* HERO */}
      <section className={styles.hero}>
        {/* 사진이 없으면 카드와 같은 대체 이미지를 쓴다 (D-064) */}
        <img
          className={styles.heroImg}
          src={img || '/placeholder.svg'}
          alt={name}
          onError={(e) => { if (e.currentTarget.dataset.fb === '1') return; e.currentTarget.dataset.fb = '1'; e.currentTarget.src = '/placeholder.svg'; }}
        />
        {/* CC BY · BY-SA 사진은 출처를 밝혀야 한다 (D-063) */}
        {img && item.imageCredit && (
          <a className={styles.heroCredit} href={item.imageSource} target="_blank" rel="noreferrer">
            사진: {item.imageCredit}
          </a>
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
              {metaItems.join(' · ')}
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
                          <Link to={s.href} style={{ color: 'inherit' }}><strong>{s.title}</strong></Link>
                        </div>
                        {s.desc && <p className={styles.timelineDesc}>{s.desc}</p>}
                      </div>
                    </li>
                  ))}
                </ol>
              </section>
            )}

            {isPlace(type) && (
              <section className={styles.section}>
                <ReviewSection targetType={type} targetId={Number(id)} />
              </section>
            )}
          </div>

          <aside className={styles.side}>
            <div className={styles.sideCard}>
              <h3 className={styles.sideTitle}>정보</h3>
              <dl className={styles.dl}>
                {item.prefecture && (<><dt>지역</dt><dd>{item.prefecture}</dd></>)}
                {item.dateText && (<><dt>일정</dt><dd>{item.dateText}</dd></>)}
                {duration && (<><dt>소요</dt><dd>{duration}</dd></>)}
                {owner && (<><dt>만든 이</dt><dd>{owner}</dd></>)}
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
