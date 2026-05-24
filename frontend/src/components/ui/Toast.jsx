import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import styles from './Toast.module.css';

const ToastContext = createContext(null);

let nextId = 1;

export function ToastProvider({ children }) {
  const [items, setItems] = useState([]);

  const remove = useCallback((id) => {
    setItems((arr) => arr.filter((t) => t.id !== id));
  }, []);

  const show = useCallback((message, opts = {}) => {
    const id = nextId++;
    const item = { id, message, type: opts.type || 'info', duration: opts.duration ?? 3200 };
    setItems((arr) => [...arr, item]);
    if (item.duration > 0) {
      setTimeout(() => remove(id), item.duration);
    }
    return id;
  }, [remove]);

  const api = {
    show,
    success: (m, o) => show(m, { ...o, type: 'success' }),
    error: (m, o) => show(m, { ...o, type: 'error', duration: o?.duration ?? 4500 }),
    info: (m, o) => show(m, { ...o, type: 'info' }),
    warn: (m, o) => show(m, { ...o, type: 'warn' }),
    dismiss: remove,
  };

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className={styles.viewport} role="status" aria-live="polite" aria-atomic="false">
        {items.map((t) => (
          <div key={t.id} className={`${styles.toast} ${styles[t.type] || ''}`}>
            <span className={styles.icon} aria-hidden="true">{iconFor(t.type)}</span>
            <span className={styles.msg}>{t.message}</span>
            <button
              type="button"
              className={styles.close}
              aria-label="알림 닫기"
              onClick={() => remove(t.id)}
            >×</button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

function iconFor(type) {
  switch (type) {
    case 'success': return '✓';
    case 'error': return '!';
    case 'warn': return '⚠';
    default: return 'ⓘ';
  }
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    // No-op fallback so components don't crash if provider missing.
    return {
      show: () => {}, success: () => {}, error: () => {},
      info: () => {}, warn: () => {}, dismiss: () => {},
    };
  }
  return ctx;
}
