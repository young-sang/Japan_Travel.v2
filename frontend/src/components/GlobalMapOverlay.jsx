import { useEffect, useState } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import { api } from '../api/client.js';
import IconButton from './ui/IconButton.jsx';
import Tag from './ui/Tag.jsx';
import { useToast } from './ui/Toast.jsx';
import styles from './GlobalMapOverlay.module.css';

export default function GlobalMapOverlay({ open, onClose }) {
  const [pins, setPins] = useState([]);
  const [show, setShow] = useState({ destination: true, festival: true });
  const toast = useToast();

  useEffect(() => {
    if (!open) return;
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    Promise.all([
      api.listDestinations().catch(() => []),
      api.listFestivals().catch(() => []),
    ]).then(([d, f]) => {
      const ds = d.filter((x) => x.lat && x.lng).map((x) => ({
        id: x.id, type: 'destination', title: x.name, lat: x.lat, lng: x.lng, color: '#4285F4',
      }));
      const fs = f.filter((x) => x.lat && x.lng).map((x) => ({
        id: x.id, type: 'festival', title: x.name, lat: x.lat, lng: x.lng, color: '#E700FF',
      }));
      setPins([...ds, ...fs]);
    }).catch(() => toast.error('지도 데이터를 불러오지 못했습니다'));
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prevOverflow;
    };
  }, [open]);

  if (!open) return null;

  const visiblePins = pins.filter((p) => show[p.type]);

  return (
    <div className={styles.overlay} role="dialog" aria-modal="true" aria-label="일본 여행지 지도">
      <div className={styles.topbar}>
        <div className={styles.titleBlock}>
          <span className={styles.title}>🗾 일본 여행지 지도</span>
          <span className={styles.sub}>{visiblePins.length}개 위치 · 핀을 클릭해 살펴보세요</span>
        </div>
        <div className={styles.filterRow}>
          <Tag
            size="md"
            selected={show.destination}
            onClick={() => setShow((s) => ({ ...s, destination: !s.destination }))}
          >여행지</Tag>
          <Tag
            size="md"
            selected={show.festival}
            onClick={() => setShow((s) => ({ ...s, festival: !s.festival }))}
          >축제</Tag>
        </div>
        <IconButton label="닫기 (ESC)" variant="solid" size="md" onClick={onClose}>×</IconButton>
      </div>
      <div className={styles.mapWrap}>
        <MapContainer center={[36.5, 136.0]} zoom={5} style={{ height: '100%', width: '100%' }}>
          <TileLayer attribution='&copy; OpenStreetMap' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
          {visiblePins.map((p) => (
            <CircleMarker key={`${p.type}-${p.id}`} center={[p.lat, p.lng]} radius={8}
              pathOptions={{ color: '#fff', weight: 2, fillColor: p.color, fillOpacity: 1 }}>
              <Popup>
                <strong>{p.title}</strong><br />
                <a href={`/detail/${p.type}/${p.id}`}>상세 보기 →</a>
              </Popup>
            </CircleMarker>
          ))}
        </MapContainer>
      </div>
    </div>
  );
}
