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

export default function PlaceCard({ item, type, onClick, onFavChange }) {
  const name = item.name || item.title;
  const metaParts = [item.prefecture, item.dateText, item.duration].filter(Boolean);
  const desc = item.description || (item.tags ? '#' + (item.tags || []).join(' #') : '');
  const img = item.imagePath;

  const inner = (
    <Card interactive className={styles.card}>
      <CardMedia src={img} alt={name} aspect="4/3" fallback="/placeholder.svg">
        <span className={`${styles.badge} ${styles[`badge_${type}`] || ''}`}>{CATEGORY_LABEL[type] || ''}</span>
        <div className={styles.fav}>
          <FavButton targetType={type} targetId={item.id} onChange={onFavChange} />
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
        {/* 서버 집계 (D-057). 리뷰가 없으면 표시하지 않는다 */}
        {item.reviewCount > 0 && (
          <CardMeta>
            <span className={styles.rating} aria-label={`평균 별점 ${item.avgRating}점, 리뷰 ${item.reviewCount}개`}>
              ★ {item.avgRating.toFixed(1)}
            </span>
            <span>({item.reviewCount})</span>
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
