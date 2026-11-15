import { useState } from 'react';
import { api, typeSlug } from '../api/client.js';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Button from '../components/ui/Button.jsx';
import PlaceCard from '../components/PlaceCard.jsx';
import { SkeletonCard } from '../components/ui/Skeleton.jsx';
import { PREFECTURES } from '../data/prefectures.js';
import styles from './AiRecommend.module.css';
import usePageTitle from '../utils/usePageTitle.js';

const MAX_LENGTH = 200;

//  M4-2 조건. value 는 API enum 그대로, '' = 보내지 않음(서버 기본값)
const KINDS = [
  { value: 'ALL', label: '둘 다' },
  { value: 'DESTINATION', label: '여행지만' },
  { value: 'FESTIVAL', label: '축제만' },
];
const SEASONS = [
  { value: '', label: '상관없음' },
  { value: 'SPRING', label: '봄 (3~5월)' },
  { value: 'SUMMER', label: '여름 (6~8월)' },
  { value: 'AUTUMN', label: '가을 (9~11월)' },
  { value: 'WINTER', label: '겨울 (12~2월)' },
];

//  후보 0곳 안내 (M4-3). 서버 문구에는 요청 값이 없으므로 보낸 조건으로 만든다
function noCandidatesMessage({ prefecture, kind, season }) {
  const seasonLabel = SEASONS.find((s) => s.value === season)?.label.replace(' ', '');
  if (kind === 'FESTIVAL' && season) {
    return `${prefecture}에는 ${seasonLabel}에 열리는 축제가 없어요. 방문 시기나 종류를 바꿔 보세요.`;
  }
  if (kind === 'FESTIVAL') return `${prefecture}에는 등록된 축제가 없어요. 종류를 바꿔 보세요.`;
  if (kind === 'DESTINATION') return `${prefecture}에는 등록된 여행지가 없어요. 지역을 바꿔 보세요.`;
  return `${prefecture}에는 조건에 맞는 장소가 없어요. 지역을 바꿔 보세요.`;
}

//  AI 추천 페이지 (M4-1 · M4-2 · M4-3). 로그인 없이 쓴다. 취향 문장 + 도도부현 + 종류 · 계절 → LLM 이 고른 2~3곳.
//  축제는 개최 월로 거르고, 여행지는 계절을 LLM 이 판단한다.
//  카드는 메인 추천 칸의 PlaceCard 를 그대로 쓰고, 설명 자리에 AI 가 쓴 이유를 넣는다
export default function AiRecommend() {
  usePageTitle('AI 추천');
  const [preference, setPreference] = useState('');
  const [prefecture, setPrefecture] = useState('');
  const [kind, setKind] = useState('ALL');
  const [season, setSeason] = useState('');
  //  status: idle | loading | done | no-candidates | failed (M4-3) | limit (M4-4)
  const [status, setStatus] = useState('idle');
  const [items, setItems] = useState([]);
  //  마지막으로 보낸 조건 — 안내 문구와 다시 시도에 쓴다. 입력 칸을 바꿔도 문구는 보낸 조건 기준
  const [lastRequest, setLastRequest] = useState(null);

  const canSubmit = preference.trim().length > 0 && prefecture !== '' && status !== 'loading';

  const send = (body) => {
    setLastRequest(body);
    setStatus('loading');
    api.aiRecommend(body)
      .then((list) => {
        setItems((list || []).map((x) => ({ ...x, id: x.targetId, type: typeSlug(x.type), description: x.reason })));
        setStatus('done');
      })
      //  후보 0곳만 따로 안내하고, LLM 실패 · 검증 후 0곳 · 그 밖의 오류는 같은 문구 + 다시 시도
      //  하루 상한(M4-4)은 다시 시도해도 소용없으므로 버튼 없는 안내
      .catch((err) => setStatus(
        err?.code === 'NO_AI_CANDIDATES' ? 'no-candidates'
          : err?.code === 'AI_DAILY_LIMIT_EXCEEDED' ? 'limit'
            : 'failed',
      ));
  };

  const submit = (e) => {
    e.preventDefault();
    if (!canSubmit) return;
    send({ preference: preference.trim(), prefecture, kind, ...(season ? { season } : {}) });
  };

  return (
    <Container>
      <PageHeader
        eyebrow="AI 추천"
        title="어떤 여행을 원하세요?"
        subtitle="취향을 한 문장으로 적고 지역을 고르면, 사이트의 여행지 · 축제 중에서 AI가 2~3곳을 골라 이유와 함께 알려 드려요."
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

        <div className={styles.row}>
          <div className={styles.col}>
            <label className={styles.label} htmlFor="ai-kind">종류</label>
            <select id="ai-kind" className={styles.select} value={kind} onChange={(e) => setKind(e.target.value)}>
              {KINDS.map((k) => <option key={k.value} value={k.value}>{k.label}</option>)}
            </select>
          </div>
          <div className={styles.col}>
            <label className={styles.label} htmlFor="ai-season">방문 시기</label>
            <select id="ai-season" className={styles.select} value={season} onChange={(e) => setSeason(e.target.value)}>
              {SEASONS.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
            </select>
          </div>
        </div>

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
        {status === 'no-candidates' && lastRequest && (
          <p className={styles.notice}>{noCandidatesMessage(lastRequest)}</p>
        )}
        {status === 'limit' && (
          <p className={styles.notice}>오늘 AI 추천 이용 횟수를 모두 썼어요. 내일 다시 이용해 주세요.</p>
        )}
        {status === 'failed' && (
          <div className={styles.error}>
            <p>AI 추천을 만들지 못했어요. 잠시 후 다시 시도해 주세요.</p>
            <Button variant="secondary" size="sm" onClick={() => send(lastRequest)}>다시 시도</Button>
          </div>
        )}
        {status === 'done' && (
          <div className={styles.cardGrid}>
            {items.map((p) => <PlaceCard key={`${p.type}-${p.id}`} item={p} type={p.type} />)}
          </div>
        )}
      </section>
    </Container>
  );
}
