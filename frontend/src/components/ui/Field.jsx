import { useId, cloneElement, isValidElement } from 'react';
import styles from './Field.module.css';

export default function Field({
  label,
  htmlFor,
  required = false,
  error,
  helper,
  children,
  className = '',
}) {
  const generated = useId();
  const id = htmlFor || generated;
  const child = isValidElement(children)
    ? cloneElement(children, {
        id: children.props.id || id,
        'aria-invalid': error ? true : undefined,
        'aria-describedby': (error || helper) ? `${id}-msg` : undefined,
      })
    : children;
  return (
    <div className={`${styles.field} ${error ? styles.hasError : ''} ${className}`}>
      {label && (
        <label htmlFor={id} className={styles.label}>
          <span>{label}</span>
          {required && <span className={styles.req} aria-label="필수">*</span>}
        </label>
      )}
      {child}
      {error ? (
        <div className={styles.error} id={`${id}-msg`} role="alert">{error}</div>
      ) : helper ? (
        <div className={styles.helper} id={`${id}-msg`}>{helper}</div>
      ) : null}
    </div>
  );
}
