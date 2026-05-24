import { Link } from 'react-router-dom';
import Card, { CardMedia, CardBody, CardTitle, CardMeta, CardDesc } from './ui/Card.jsx';
import FavButton from './FavButton.jsx';
import styles from './PlaceCard.module.css';

const CATEGORY_LABEL = {
  destination: '여행지',
  festival: '축제',
  course: '코스',
};

function truncate(text, n = 120) {
  if (!text) return '';
  return text.length > n ? text.slice(0, n).trim() + '…' : text;
}

export default function PlaceCard({ item, type, onClick }) {
  const name = item.name || item.title;
  const metaParts = [item.prefecture, item.dateText, item.duration].filter(Boolean);
  const desc = item.description || (item.tags ? '#' + (item.tags || []).join(' #') : '');
  const img = item.imagePath;

  const inner = (
    <Card interactive className={styles.card}>
      <CardMedia src={img} alt={name} aspect="4/3" fallback="/placeholder.svg">
        <span className={`${styles.badge} ${styles[`badge_${type}`] || ''}`}>{CATEGORY_LABEL[type] || ''}</span>
        <div className={styles.fav}>
          <FavButton targetType={type} targetId={item.id} />
        </div>
      </CardMedia>
      <CardBody>
        <CardTitle>{name}</CardTitle>
        {metaParts.length > 0 && (
          <CardMeta>
            <span aria-hidden="true">📍</span>
            <span>{metaParts.join(' · ')}</span>
          </CardMeta>
        )}
        {desc && <CardDesc clamp={2}>{truncate(desc)}</CardDesc>}
      </CardBody>
    </Card>
  );

  if (onClick) {
    return (
      <div className={styles.linkWrap} onClick={(e) => onClick(item, e)} role="button" tabIndex={0}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); onClick(item, e); } }}>
        {inner}
      </div>
    );
  }
  return (
    <Link to={`/detail/${type}/${item.id}`} className={styles.linkWrap} aria-label={`${name} 상세 보기`}>
      {inner}
    </Link>
  );
}
