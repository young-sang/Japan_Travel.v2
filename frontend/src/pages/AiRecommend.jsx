import { useState } from 'react';
import { api } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import { SkeletonCard } from '../components/ui/Skeleton.jsx';
import { PREFECTURES } from '../data/prefectures.js';
import styles from './AiRecommend.module.css';
import usePageTitle from '../utils/usePageTitle.js';

const MAX_LENGTH = 200;

//  AI 추천 페이지 (M4-1). 로그인 없이 쓴다. 취향 문장 + 도도부현 → 그 현의 여행지 중 LLM 이 고른 2~3곳.
//  카드는 메인 추천 칸의 PlaceCard 를 그대로 쓰고, 설명 자리에 AI 가 쓴 이유를 넣는다
export default function AiRecommend() {
  usePageTitle('AI 추천');
  const [preference, setPreference] = useState('');
  const [prefecture, setPrefecture] = useState('');
  //  status: idle | loading | done | error
  const [status, setStatus] = useState('idle');
  const [items, setItems] = useState([]);

  const canSubmit = preference.trim().length > 0 && prefecture !== '' && status !== 'loading';

  const submit = (e) => {
    e.preventDefault();
    if (!canSubmit) return;
    setStatus('loading');
    api.aiRecommend({ preference: preference.trim(), prefecture })
      .then((list) => {
        setItems((list || []).map((x) => ({ ...x, id: x.targetId, description: x.reason })));
        setStatus('done');
      })
      .catch(() => setStatus('error'));
  };

  return (
    <Container>
      <PageHeader
        eyebrow="AI 추천"
        title="어떤 여행을 원하세요?"
        subtitle="취향을 한 문장으로 적고 지역을 고르면, 사이트의 여행지 중에서 AI가 2~3곳을 골라 이유와 함께 알려 드려요."
      />

      <form className={styles.form} onSubmit={submit}>
        <label className={styles.label} htmlFor="ai-preference">취향 문장</label>
        <textarea
          id="ai-preference"
          className={styles.textarea}
          rows={3}
          maxLength={MAX_LENGTH}
          placeholder="예: 조용한 온천에서 쉬고 싶어요"
          value={preference}
          onChange={(e) => setPreference(e.target.value)}
        />
        <div className={styles.count}>{preference.length} / {MAX_LENGTH}</div>

        <label className={styles.label} htmlFor="ai-prefecture">지역</label>
        <select
          id="ai-prefecture"
          className={styles.select}
          value={prefecture}
          onChange={(e) => setPrefecture(e.target.value)}
        >
          <option value="">도도부현 선택</option>
          {PREFECTURES.map((p) => <option key={p} value={p}>{p}</option>)}
        </select>

        <Button type="submit" disabled={!canSubmit} loading={status === 'loading'} className={styles.submit}>
          추천 받기
        </Button>
      </form>

      <section className={styles.result} aria-live="polite">
        {status === 'loading' && (
          <>
            <p className={styles.message}>AI가 추천 중이에요… 몇 초 걸릴 수 있어요.</p>
            <div className={styles.cardGrid}>
              {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
            </div>
          </>
        )}
        {status === 'error' && (
          <p className={styles.error}>추천을 만들지 못했어요. 잠시 후 다시 시도해 주세요.</p>
        )}
        {status === 'done' && (
          <div className={styles.cardGrid}>
            {items.map((p) => <PlaceCard key={p.id} item={p} type="destination" />)}
          </div>
        )}
      </section>
    </Container>
  );
}
