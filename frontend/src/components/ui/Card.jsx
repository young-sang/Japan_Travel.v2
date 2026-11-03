import styles from './Card.module.css';

export default function Card({
  as: Tag = 'article',
  interactive = false,
  className = '',
  children,
  ...rest
}) {
  const cls = [styles.card, interactive ? styles.interactive : '', className].filter(Boolean).join(' ');
  return <Tag className={cls} {...rest}>{children}</Tag>;
}

export function CardMedia({ src, alt = '', aspect = '4/3', fallback, children, className = '' }) {
  const onError = (e) => {
    if (!fallback) return;
    if (e.currentTarget.dataset.fallback === '1') return;
    e.currentTarget.dataset.fallback = '1';
    e.currentTarget.src = fallback;
  };
  return (
    <div className={`${styles.media} ${className}`} style={{ aspectRatio: aspect }}>
      {/* 사진이 없어도 빈 회색 칸 대신 대체 이미지를 보인다 (D-064) */}
      {(src || fallback) && <img src={src || fallback} alt={alt} loading="lazy" onError={onError} />}
      {children}
    </div>
  );
}

export function CardBody({ children, className = '' }) {
  return <div className={`${styles.body} ${className}`}>{children}</div>;
}

export function CardTitle({ children, className = '' }) {
  return <h3 className={`${styles.title} ${className}`}>{children}</h3>;
}

export function CardMeta({ children, className = '' }) {
  return <div className={`${styles.meta} ${className}`}>{children}</div>;
}

export function CardDesc({ children, className = '', clamp = 2 }) {
  return <p className={`${styles.desc} ${className}`} style={{ WebkitLineClamp: clamp }}>{children}</p>;
}

export function CardFooter({ children, className = '' }) {
  return <div className={`${styles.footer} ${className}`}>{children}</div>;
}
