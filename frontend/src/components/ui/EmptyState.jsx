import styles from './EmptyState.module.css';

export default function EmptyState({
  icon = '🗺️',
  title,
  description,
  primaryAction,
  secondaryAction,
  size = 'md',
  className = '',
}) {
  return (
    <div className={`${styles.wrap} ${styles[`s_${size}`] || ''} ${className}`} role="status">
      <div className={styles.icon} aria-hidden="true">{icon}</div>
      {title && <h3 className={styles.title}>{title}</h3>}
      {description && <p className={styles.desc}>{description}</p>}
      {(primaryAction || secondaryAction) && (
        <div className={styles.actions}>
          {primaryAction}
          {secondaryAction}
        </div>
      )}
    </div>
  );
}
