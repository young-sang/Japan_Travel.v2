import { Component } from 'react';
import styles from './ErrorBoundary.module.css';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }
  static getDerivedStateFromError(error) {
    return { error };
  }
  componentDidCatch(error, info) {
    if (typeof console !== 'undefined') {
      console.error('[ErrorBoundary]', error, info?.componentStack);
    }
  }
  handleRetry = () => {
    this.setState({ error: null });
  };
  render() {
    if (this.state.error) {
      return (
        <div className={styles.wrap} role="alert">
          <div className={styles.icon} aria-hidden="true">⚠️</div>
          <h2 className={styles.title}>문제가 발생했습니다</h2>
          <p className={styles.desc}>잠시 후 다시 시도해 주세요.</p>
          <div className={styles.actions}>
            <button type="button" className={styles.btn} onClick={this.handleRetry}>다시 시도</button>
            <button type="button" className={styles.btnGhost} onClick={() => (window.location.href = '/')}>홈으로</button>
          </div>
          {import.meta.env?.DEV && (
            <pre className={styles.detail}>{String(this.state.error?.message || this.state.error)}</pre>
          )}
        </div>
      );
    }
    return this.props.children;
  }
}
