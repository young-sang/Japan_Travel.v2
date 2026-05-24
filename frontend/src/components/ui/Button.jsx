import styles from './Button.module.css';

export default function Button({
  variant = 'primary',
  size = 'md',
  type = 'button',
  loading = false,
  disabled = false,
  iconLeft,
  iconRight,
  children,
  className = '',
  ...rest
}) {
  const classes = [
    styles.btn,
    styles[`v_${variant}`] || '',
    styles[`s_${size}`] || '',
    loading ? styles.loading : '',
    className,
  ].filter(Boolean).join(' ');

  return (
    <button type={type} className={classes} disabled={disabled || loading} aria-busy={loading || undefined} {...rest}>
      {loading && <span className={styles.spinner} aria-hidden="true" />}
      {!loading && iconLeft && <span className={styles.iconL} aria-hidden="true">{iconLeft}</span>}
      <span className={styles.label}>{children}</span>
      {!loading && iconRight && <span className={styles.iconR} aria-hidden="true">{iconRight}</span>}
    </button>
  );
}
