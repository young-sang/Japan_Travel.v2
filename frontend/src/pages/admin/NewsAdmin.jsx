import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import Container from '../../components/ui/Container.jsx';
import PageHeader from '../../components/ui/PageHeader.jsx';
import Button from '../../components/ui/Button.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import { useToast } from '../../components/ui/Toast.jsx';
import usePageTitle from '../../utils/usePageTitle.js';
import formatDate from '../../utils/formatDate.js';
import styles from './NewsAdmin.module.css';

//  관리자 뉴스 관리 (D-079). 라우트가 requireAdmin 으로 막고, API 도 ADMIN 이 아니면 403 이다.
//  옛 관리자 화면(Admin*.jsx, D-054)과는 따로 둔다
export default function NewsAdmin() {
  usePageTitle('뉴스 관리');
  const toast = useToast();
  const [items, setItems] = useState(null);

  async function reload() {
    try {
      setItems(await api.listNews());
    } catch {
      toast.error('뉴스를 불러오지 못했습니다');
      setItems([]);
    }
  }
  useEffect(() => { reload(); }, []);

  async function remove(n) {
    if (!confirm(`"${n.title}" 뉴스를 삭제할까요?`)) return;
    try {
      await api.deleteNews(n.id);
      toast.info('뉴스를 삭제했어요');
      reload();
    } catch { toast.error('삭제 실패'); }
  }

  return (
    <Container>
      <PageHeader
        eyebrow="관리자"
        title="뉴스 관리"
        subtitle="여기서 쓴 글이 뉴스 페이지에 최신순으로 올라갑니다."
        actions={<Link to="/admin/news/new"><Button variant="primary">+ 뉴스 쓰기</Button></Link>}
      />
      {items !== null && items.length === 0 && (
        <EmptyState icon="📰" title="아직 쓴 뉴스가 없어요" />
      )}
      {items !== null && items.length > 0 && (
        <table className={styles.table}>
          <thead>
            <tr><th>제목</th><th className={styles.date}>작성일</th><th className={styles.actions}>관리</th></tr>
          </thead>
          <tbody>
            {items.map((n) => (
              <tr key={n.id}>
                <td><Link to={`/news/${n.id}`}>{n.title}</Link></td>
                <td className={styles.date}>{formatDate(n.createdAt)}</td>
                <td className={styles.actions}>
                  <Link to={`/admin/news/${n.id}/edit`}><Button variant="secondary" size="sm">수정</Button></Link>
                  <Button variant="danger" size="sm" onClick={() => remove(n)}>삭제</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Container>
  );
}
