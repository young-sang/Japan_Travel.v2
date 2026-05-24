import styles from './Skeleton.module.css';

export function Skeleton({ width = '100%', height = 14, radius = 6, className = '', style = {} }) {
  return (
    <span
      className={`${styles.box} ${className}`}
      style={{ width, height, borderRadius: radius, ...style }}
      aria-hidden="true"
    />
  );
}

export function SkeletonCard() {
  return (
    <div className={styles.card} aria-hidden="true">
      <div className={styles.media} />
      <div className={styles.body}>
        <Skeleton height={18} width="80%" />
        <Skeleton height={12} width="50%" />
        <Skeleton height={12} width="92%" />
        <Skeleton height={12} width="70%" />
      </div>
    </div>
  );
}

export function SkeletonGrid({ count = 8, columns = 'repeat(auto-fill, minmax(240px, 1fr))', gap = '16px' }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: columns, gap }}>
      {Array.from({ length: count }).map((_, i) => <SkeletonCard key={i} />)}
    </div>
  );
}

export default Skeleton;
