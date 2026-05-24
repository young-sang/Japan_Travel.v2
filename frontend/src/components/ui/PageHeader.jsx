import styles from './PageHeader.module.css';

export default function PageHeader({
  eyebrow,
  title,
  subtitle,
  breadcrumb,
  actions,
  align = 'left',
  className = '',
}) {
  return (
    <header className={`${styles.wrap} ${styles[`a_${align}`]} ${className}`}>
      <div className={styles.text}>
        {breadcrumb && <nav className={styles.crumbs} aria-label="브레드크럼">{breadcrumb}</nav>}
        {eyebrow && <div className={styles.eyebrow}>{eyebrow}</div>}
        {title && <h1 className={styles.title}>{title}</h1>}
        {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
      </div>
      {actions && <div className={styles.actions}>{actions}</div>}
    </header>
  );
}
