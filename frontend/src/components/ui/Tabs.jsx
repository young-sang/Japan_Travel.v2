import styles from './Tabs.module.css';

// Controlled tabs. items: [{ key, label, count? }]
export default function Tabs({ items, value, onChange, sticky = false, ariaLabel = '탭' }) {
  return (
    <div className={`${styles.wrap} ${sticky ? styles.sticky : ''}`} role="tablist" aria-label={ariaLabel}>
      <div className={styles.inner}>
        {items.map((it) => {
          const active = it.key === value;
          return (
            <button
              key={it.key}
              type="button"
              role="tab"
              aria-selected={active}
              className={`${styles.tab} ${active ? styles.active : ''}`}
              onClick={() => onChange?.(it.key)}
            >
              <span>{it.label}</span>
              {typeof it.count === 'number' && <span className={styles.count}>{it.count}</span>}
            </button>
          );
        })}
      </div>
    </div>
  );
}
