import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import Modal from './ui/Modal.jsx';
import Tag from './ui/Tag.jsx';
import Spinner from './ui/Spinner.jsx';
import EmptyState from './ui/EmptyState.jsx';
import styles from './SearchOverlay.module.css';

const TRENDING = [
  '교토 단풍', '오키나와 바다', '도쿄 야경', '홋카이도 눈축제',
  '오사카 음식', '기온 마츠리', '후지산 트레킹', '나라 사슴',
];

const CATEGORY_LABEL = { destination: '여행지', festival: '축제', course: '여행코스' };

export default function SearchOverlay({ open, onClose }) {
  const [q, setQ] = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) { setQ(''); setResults(null); return; }
  }, [open]);

  useEffect(() => {
    if (!q.trim()) { setResults(null); return; }
    setLoading(true);
    const t = setTimeout(() => {
      api.search(q.trim())
        .then((r) => setResults(r || { destinations: [], festivals: [], courses: [] }))
        .catch(() => setResults({ destinations: [], festivals: [], courses: [] }))
        .finally(() => setLoading(false));
    }, 200);
    return () => clearTimeout(t);
  }, [q]);

  const totalCount = results
    ? (results.destinations?.length || 0) + (results.festivals?.length || 0) + (results.courses?.length || 0)
    : 0;

  return (
    <Modal open={open} onClose={onClose} size="lg" ariaLabel="통합 검색" hideClose={false}
      title={<span className={styles.modalTitle}>🔍 통합 검색<kbd className={styles.kbd}>ESC</kbd></span>}
    >
      <div className={styles.searchInputWrap}>
        <input
          autoFocus
          className={styles.searchInput}
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="여행지, 축제, 코스를 검색해보세요"
          aria-label="검색어"
        />
        {loading && <span className={styles.searchSpinner}><Spinner size={20} /></span>}
      </div>

      {!q.trim() && (
        <section className={styles.trending}>
          <h4 className={styles.trendingTitle}>🔥 추천 검색어</h4>
          <div className={styles.trendingTags}>
            {TRENDING.map((t) => (
              <Tag key={t} size="md" onClick={() => setQ(t)}>#{t.replace(' ', '')}</Tag>
            ))}
          </div>
        </section>
      )}

      {results && totalCount > 0 && (
        <div className={styles.results}>
          <ResultGroup title="여행지" items={results.destinations} type="destination" onPick={onClose} />
          <ResultGroup title="축제" items={results.festivals} type="festival" onPick={onClose} />
          <ResultGroup title="여행코스" items={results.courses} type="course" onPick={onClose} fieldName="title" />
        </div>
      )}

      {results && totalCount === 0 && !loading && (
        <EmptyState
          icon="🔎"
          title="검색 결과가 없어요"
          description="다른 키워드로 검색해보세요"
          size="sm"
        />
      )}
    </Modal>
  );
}

function ResultGroup({ title, items, type, fieldName = 'name', onPick }) {
  if (!items || items.length === 0) return null;
  return (
    <div className={styles.group}>
      <h5 className={styles.groupTitle}>{title} <span>{items.length}</span></h5>
      <ul className={styles.list}>
        {items.map((it) => (
          <li key={`${type}-${it.id}`}>
            <Link to={`/detail/${type}/${it.id}`} className={styles.item} onClick={onPick}>
              <span className={`${styles.badge} ${styles[`badge_${type}`] || ''}`}>{CATEGORY_LABEL[type] || ''}</span>
              <span className={styles.itemTitle}>{it[fieldName] || it.name || it.title}</span>
              {it.prefecture && <span className={styles.itemMeta}>{it.prefecture}</span>}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
