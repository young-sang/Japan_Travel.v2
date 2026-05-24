import styles from './Rating.module.css';

// Read-only or interactive star rating.
export default function Rating({ value = 0, max = 5, onChange, size = 16, showValue = false, label }) {
  const interactive = typeof onChange === 'function';
  const rounded = Math.round(value);
  return (
    <span
      className={styles.row}
      role={interactive ? 'radiogroup' : 'img'}
      aria-label={label || `${value} / ${max}`}
    >
      {Array.from({ length: max }).map((_, i) => {
        const idx = i + 1;
        const filled = idx <= rounded;
        if (interactive) {
          return (
            <button
              key={i}
              type="button"
              className={`${styles.star} ${filled ? styles.filled : ''}`}
              aria-label={`${idx}점`}
              aria-checked={idx === rounded}
              role="radio"
              onClick={() => onChange(idx)}
              style={{ fontSize: size }}
            >★</button>
          );
        }
        return (
          <span
            key={i}
            className={`${styles.star} ${filled ? styles.filled : ''} ${styles.readonly}`}
            style={{ fontSize: size }}
            aria-hidden="true"
          >★</span>
        );
      })}
      {showValue && (
        <span className={styles.value}>{Number(value || 0).toFixed(1)}</span>
      )}
    </span>
  );
}
