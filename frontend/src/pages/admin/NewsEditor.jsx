import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../../api/client.js';
import Container from '../../components/ui/Container.jsx';
import PageHeader from '../../components/ui/PageHeader.jsx';
import Button from '../../components/ui/Button.jsx';
import Field from '../../components/ui/Field.jsx';
import { useToast } from '../../components/ui/Toast.jsx';
import usePageTitle from '../../utils/usePageTitle.js';
import styles from './NewsAdmin.module.css';

const TITLE_MAX = 200;

//  뉴스 작성 · 수정 폼 (D-079). 사진은 파일 업로드 없이 URL 과 출처만 받는다 (D-075)
export default function NewsEditor() {
  const { id } = useParams();
  const editing = !!id;
  usePageTitle(editing ? '뉴스 수정' : '뉴스 쓰기');
  const navigate = useNavigate();
  const toast = useToast();

  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [imagePath, setImagePath] = useState('');
  const [imageCredit, setImageCredit] = useState('');
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!editing) return;
    api.getNews(id)
      .then((n) => {
        setTitle(n.title); setBody(n.body);
        setImagePath(n.imagePath || ''); setImageCredit(n.imageCredit || '');
      })
      .catch(() => { toast.error('뉴스를 불러오지 못했습니다'); navigate('/admin/news'); });
  }, [id]);

//  서버(INVALID_NEWS)와 같은 규칙을 미리 보여 준다
  function check() {
    const e = {};
    if (!title.trim()) e.title = '제목을 입력해주세요';
    else if (title.trim().length > TITLE_MAX) e.title = `제목은 ${TITLE_MAX}자까지예요`;
    if (!body.trim()) e.body = '본문을 입력해주세요';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function save(ev) {
    ev.preventDefault();
    if (!check()) return;
    setSaving(true);
    const req = { title, body, imagePath, imageCredit };
    try {
      const saved = editing ? await api.updateNews(id, req) : await api.createNews(req);
      toast.success(editing ? '뉴스를 고쳤어요' : '뉴스를 올렸어요');
      navigate(`/news/${saved.id}`);
    } catch (err) {
      toast.error(err.message || '저장 실패');
      setSaving(false);
    }
  }

  return (
    <Container>
      <PageHeader eyebrow="관리자" title={editing ? '뉴스 수정' : '뉴스 쓰기'} />
      <form className={styles.form} onSubmit={save}>
        <Field label="제목" required error={errors.title}>
          <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={TITLE_MAX} />
        </Field>
        <Field label="본문" required error={errors.body} helper="줄바꿈은 그대로 보입니다">
          <textarea value={body} onChange={(e) => setBody(e.target.value)} rows={12} />
        </Field>
        <Field label="대표 사진 URL" helper="선택. 자유 라이선스 사진(예: Wikimedia Commons)을 써 주세요">
          <input value={imagePath} onChange={(e) => setImagePath(e.target.value)} placeholder="https://…" />
        </Field>
        <Field label="사진 출처" helper="선택. 예: 작가 / CC BY-SA 4.0 / Wikimedia Commons">
          <input value={imageCredit} onChange={(e) => setImageCredit(e.target.value)} />
        </Field>
        <div className={styles.formActions}>
          <Button variant="secondary" onClick={() => navigate('/admin/news')}>취소</Button>
          <Button type="submit" variant="primary" loading={saving}>{editing ? '저장' : '올리기'}</Button>
        </div>
      </form>
    </Container>
  );
}
