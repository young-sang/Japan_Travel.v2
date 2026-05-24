import styles from './Spinner.module.css';

export default function Spinner({ size = 24, label = '로딩 중', inline = false }) {
  return (
    <span
      className={`${styles.spinner} ${inline ? styles.inline : ''}`}
      style={{ width: size, height: size, borderWidth: Math.max(2, Math.round(size / 10)) }}
      role="status"
      aria-label={label}
    />
  );
}
