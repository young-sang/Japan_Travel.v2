import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import styles from './Modal.module.css';

export default function Modal({
  open,
  onClose,
  title,
  size = 'md',
  fullscreenOnMobile = true,
  closeOnBackdrop = true,
  hideClose = false,
  ariaLabel,
  children,
}) {
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    const onKey = (e) => { if (e.key === 'Escape') onClose?.(); };
    document.addEventListener('keydown', onKey);
    // Lock body scroll
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prevOverflow;
    };
  }, [open, onClose]);

  useEffect(() => {
    if (open && ref.current) {
      const focusable = ref.current.querySelector(
        'input, textarea, button, [tabindex]:not([tabindex="-1"])'
      );
      focusable?.focus();
    }
  }, [open]);

  if (!open) return null;

  const onBackdropClick = (e) => {
    if (e.target === e.currentTarget && closeOnBackdrop) onClose?.();
  };

  return createPortal(
    <div className={styles.backdrop} onMouseDown={onBackdropClick}>
      <div
        ref={ref}
        className={`${styles.dialog} ${styles[`s_${size}`] || ''} ${fullscreenOnMobile ? styles.fsm : ''}`}
        role="dialog"
        aria-modal="true"
        aria-label={ariaLabel || (typeof title === 'string' ? title : undefined)}
      >
        {(title || !hideClose) && (
          <div className={styles.head}>
            {title && <h2 className={styles.title}>{title}</h2>}
            {!hideClose && (
              <button type="button" className={styles.close} onClick={onClose} aria-label="닫기 (ESC)">×</button>
            )}
          </div>
        )}
        <div className={styles.body}>{children}</div>
      </div>
    </div>,
    document.body
  );
}
