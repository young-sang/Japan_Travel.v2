import styles from './IconButton.module.css';

export default function IconButton({
  label,
  size = 'md',
  variant = 'ghost',
  type = 'button',
  children,
  className = '',
  ...rest
}) {
  if (!label) {
    if (typeof console !== 'undefined') console.warn('IconButton requires a label prop for a11y');
  }
  const classes = [
    styles.btn, styles[`s_${size}`] || '', styles[`v_${variant}`] || '', className,
  ].filter(Boolean).join(' ');
  return (
    <button type={type} aria-label={label} title={label} className={classes} {...rest}>
      {children}
    </button>
  );
}
