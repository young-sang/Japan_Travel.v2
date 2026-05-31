import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import Field from '../components/ui/Field.jsx';
import { api } from '../api/client.js';
import { useAuth } from '../auth/useAuth.js';
import { useToast } from '../components/ui/Toast.jsx';
import styles from './Board.module.css';

export default function PostEditor() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const { user } = useAuth();
  const toast = useToast();
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [loading, setLoading] = useState(editing);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!editing) return;
    api.getPost(id)
      .then((p) => {
        if (user && p.userId !== user.id) {
          toast.error('수정 권한이 없습니다');
          navigate(`/board/${id}`, { replace: true });
          return;
        }
        setTitle(p.title);
        setBody(p.body);
      })
      .catch(() => toast.error('게시글을 불러오지 못했습니다'))
      .finally(() => setLoading(false));
  }, [id]);

  async function submit(e) {
    e.preventDefault();
    if (!title.trim() || !body.trim()) { toast.warn('제목과 내용을 입력해주세요'); return; }
    setSubmitting(true);
    try {
      if (editing) {
        await api.updatePost(id, { title, body });
        toast.success('게시글을 수정했습니다');
        navigate(`/board/${id}`);
      } else {
        const p = await api.createPost({ title, body });
        toast.success('게시글을 등록했습니다');
        navigate(`/board/${p?.id ?? ''}` || '/board');
      }
    } catch (e) {
      toast.error('저장에 실패했습니다');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <Container><p className={styles.muted}>불러오는 중…</p></Container>;

  return (
    <Container>
      <PageHeader
        eyebrow="자유게시판"
        title={editing ? '글 수정' : '새 글 작성'}
      />
      <form onSubmit={submit} className={styles.editorForm}>
        <Field label="제목" required>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="제목을 입력하세요"
            maxLength={200}
          />
        </Field>
        <Field label="내용" required>
          <textarea
            value={body}
            onChange={(e) => setBody(e.target.value)}
            placeholder="내용을 입력하세요"
          />
        </Field>
        <div className={styles.editorActions}>
          <Button type="button" variant="ghost" onClick={() => navigate(-1)}>취소</Button>
          <Button type="submit" variant="primary" loading={submitting}>{editing ? '수정' : '등록'}</Button>
        </div>
      </form>
    </Container>
  );
}
