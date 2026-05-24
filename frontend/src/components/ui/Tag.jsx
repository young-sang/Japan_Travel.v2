import styles from './Tag.module.css';

export default function Tag({
  variant = 'neutral',
  size = 'md',
  selected = false,
  removable = false,
  onRemove,
  onClick,
  as,
  children,
  className = '',
  ...rest
}) {
  const interactive = !!onClick;
  const Tag = as || (interactive ? 'button' : 'span');
  const cls = [
    styles.tag,
    styles[`v_${variant}`] || '',
    styles[`s_${size}`] || '',
    selected ? styles.selected : '',
    interactive ? styles.interactive : '',
    className,
  ].filter(Boolean).join(' ');
  const extra = interactive ? { type: 'button', onClick, 'aria-pressed': selected } : {};
  return (
    <Tag className={cls} {...extra} {...rest}>
      <span className={styles.label}>{children}</span>
      {removable && (
        <button
          type="button"
          className={styles.remove}
          aria-label="제거"
          onClick={(e) => { e.stopPropagation(); onRemove?.(); }}
        >×</button>
      )}
    </Tag>
  );
}
