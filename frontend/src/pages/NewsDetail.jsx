import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Button from '../components/ui/Button.jsx';
import styles from './NewsDetail.module.css';
import usePageTitle from '../utils/usePageTitle.js';
import formatDate from '../utils/formatDate.js';

//  본문은 줄바꿈만 살리는 텍스트다 (D-079). 사진은 출처를 함께 보인다 (D-063)
export default function NewsDetail() {
  const { id } = useParams();
  const [news, setNews] = useState(null);
  const [error, setError] = useState(false);
  usePageTitle(news?.title);

  useEffect(() => {
    setNews(null); setError(false);
    api.getNews(id).then(setNews).catch(() => setError(true));
  }, [id]);

  if (error) {
    return (
      <Container>
        <EmptyState
          icon="📰"
          title="뉴스를 찾을 수 없어요"
          primaryAction={<Link to="/news"><Button variant="primary">뉴스 목록으로</Button></Link>}
        />
      </Container>
    );
  }
  if (!news) return <p className={styles.loading}>불러오는 중…</p>;

  return (
    <article className={styles.article}>
      <Link to="/news" className={styles.back}>← 뉴스 목록</Link>
      <h1 className={styles.title}>{news.title}</h1>
      <p className={styles.meta}>
        {formatDate(news.createdAt)}
        {news.authorNickname && <> · {news.authorNickname}</>}
        {news.updatedAt && <> · 수정됨</>}
      </p>
      {news.imagePath && (
        <figure className={styles.figure}>
          <img src={news.imagePath} alt="" />
          {news.imageCredit && <figcaption data-news-credit>사진: {news.imageCredit}</figcaption>}
        </figure>
      )}
      <div className={styles.body} data-news-body>{news.body}</div>
    </article>
  );
}
