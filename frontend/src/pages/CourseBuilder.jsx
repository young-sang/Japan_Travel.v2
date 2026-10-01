import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api, typeSlug } from '../api/client.js';
import { PREFECTURES } from '../data/prefectures.js';
import Container from '../components/ui/Container.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import Field from '../components/ui/Field.jsx';
import Button from '../components/ui/Button.jsx';
import IconButton from '../components/ui/IconButton.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Spinner from '../components/ui/Spinner.jsx';
import { useToast } from '../components/ui/Toast.jsx';
import styles from './CourseBuilder.module.css';

// 코스 = 기본 정보 + 정류장 (D-041 · D-042). 정류장은 여행지 · 축제 하나를 가리키고
// 일차 · 메모만 갖는다 — 시간은 없다 (D-043). 임시저장은 없고 공개 / 비공개만 고른다 (D-041).
// 요청 모양: stops[] = {dayNo, type: DESTINATION|FESTIVAL, targetId, memo} (D-046). 순서는 배열 순서다.
export default function CourseBuilder() {
  const { id } = useParams();
  const editing = !!id;
  const navigate = useNavigate();
  const toast = useToast();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [prefecture, setPrefecture] = useState('도쿄도');
  const [imagePath, setImagePath] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [stops, setStops] = useState([]);   // {dayNo, type: 'destination'|'festival', targetId, name, memo}
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);

  const [tab, setTab] = useState('search');
  const [favs, setFavs] = useState(null);
  const [favsLoading, setFavsLoading] = useState(false);

  useEffect(() => {
    if (editing) {
      api.getCourse(id)
        .then((c) => {
          setTitle(c.title || ''); setDescription(c.description || '');
          setPrefecture(c.prefecture || '도쿄도'); setImagePath(c.imagePath || '');
          setIsPublic(c.isPublic);
          setStops((c.stops || []).map((s) => ({
            dayNo: s.dayNo, type: typeSlug(s.type), targetId: s.targetId, name: s.name, memo: s.memo || '',
          })));
        })
        .catch(() => toast.error('코스를 불러오지 못했습니다'));
    }
  }, [id, editing]);

  useEffect(() => {
    if (!query.trim()) { setResults([]); return; }
    setSearchLoading(true);
    const t = setTimeout(() => {
      Promise.all([
        api.listDestinations({ prefecture }).catch(() => []),
        api.listFestivals({ prefecture }).catch(() => []),
      ])
        .then(([dests, fests]) => {
          const tagged = [
            ...dests.map((d) => ({ ...d, _type: 'destination' })),
            ...fests.map((f) => ({ ...f, _type: 'festival' })),
          ];
          setResults(tagged.filter((x) => x.name?.includes(query)).slice(0, 10));
        })
        .catch(() => setResults([]))
        .finally(() => setSearchLoading(false));
    }, 200);
    return () => clearTimeout(t);
  }, [query, prefecture]);

  // 즐겨찾기 응답이 장소 DTO 두 배열이라 상세를 다시 부르지 않는다 (D-039)
  useEffect(() => {
    if (tab !== 'favorites' || favs !== null) return;
    setFavsLoading(true);
    api.listFavorites()
      .then((fav) => setFavs([
        ...fav.destinations.map((d) => ({ ...d, _type: 'destination' })),
        ...fav.festivals.map((f) => ({ ...f, _type: 'festival' })),
      ]))
      .catch(() => setFavs([]))
      .finally(() => setFavsLoading(false));
  }, [tab, favs]);

  const stopsCount = stops.length;
  const days = stopsCount ? Math.max(...stops.map((s) => s.dayNo)) : 0;
  const stopSummary = useMemo(() => stops.map((s, i) => `${i + 1}. ${s.name}`).join(' → '), [stops]);

  function addStop(item, type) {
    setStops((s) => [...s, {
      dayNo: s.length ? s[s.length - 1].dayNo : 1,   // 마지막 정류장과 같은 일차로 시작
      type,
      targetId: item.id,
      name: item.name,
      memo: '',
    }]);
    setQuery('');
  }

  function move(idx, dir) {
    const target = idx + dir;
    if (target < 0 || target >= stops.length) return;
    const next = [...stops];
    [next[idx], next[target]] = [next[target], next[idx]];
    setStops(next);
  }

  function update(idx, field, value) {
    setStops((s) => s.map((st, i) => i === idx ? { ...st, [field]: value } : st));
  }

  function validate() {
    const e = {};
    if (!title.trim()) e.title = '제목을 입력해주세요';
    if (stops.length === 0) e.stops = '하나 이상의 일정이 필요합니다';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function save() {
    if (!validate()) {
      toast.warn('입력 내용을 확인해주세요');
      return;
    }
    setSaving(true);
    // 서버는 일차 안에서 배열 순서대로 순번을 매긴다. 일차가 섞여 있으면 일차 순으로 안정 정렬한다
    const ordered = stops.map((s, i) => ({ ...s, i })).sort((a, b) => a.dayNo - b.dayNo || a.i - b.i);
    const body = {
      title,
      description: description || null,
      prefecture,
      imagePath: imagePath || null,
      isPublic,
      stops: ordered.map((s) => ({
        dayNo: Number(s.dayNo), type: s.type.toUpperCase(), targetId: s.targetId, memo: s.memo || null,
      })),
    };
    try {
      const saved = editing ? await api.updateCourse(id, body) : await api.createCourse(body);
      toast.success('저장했어요');
      navigate(`/detail/course/${saved.id}`);
    } catch (err) {
      toast.error(err.message || '저장에 실패했습니다');
    } finally {
      setSaving(false);
    }
  }

  return (
    <Container size="wide">
      <PageHeader
        eyebrow={editing ? '코스 편집' : '새 코스'}
        title={editing ? '코스 편집' : '내 코스 만들기'}
        subtitle={stopsCount > 0 ? stopSummary : '왼쪽에서 장소를 검색해 일정을 추가해보세요'}
        actions={
          <>
            <label style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 'var(--fs-sm)' }}>
              <input type="checkbox" checked={isPublic} onChange={(e) => setIsPublic(e.target.checked)} />
              공개
            </label>
            <Button variant="primary" onClick={save} loading={saving}>저장</Button>
          </>
        }
      />

      <div className={styles.layout}>
        <section className={styles.left}>
          <div className={styles.card}>
            <h2 className={styles.cardTitle}>기본 정보</h2>
            <div className={styles.fields}>
              <Field label="제목" required error={errors.title}>
                <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="예: 도쿄 1일 산책" />
              </Field>
              <Field label="지역" required helper="코스의 대표 지역. 다른 지역의 장소도 넣을 수 있어요">
                <select value={prefecture} onChange={(e) => setPrefecture(e.target.value)}>
                  {PREFECTURES.map((p) => <option key={p}>{p}</option>)}
                </select>
              </Field>
              <Field label="소개" helper="선택">
                <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="어떤 코스인지 짧게 소개해주세요" />
              </Field>
              <Field label="대표 이미지 URL" helper="선택">
                <input value={imagePath} onChange={(e) => setImagePath(e.target.value)} placeholder="https://…" />
              </Field>
            </div>
          </div>

          <div className={styles.card}>
            <div className={styles.tabs}>
              <button
                type="button"
                className={`${styles.tab} ${tab === 'search' ? styles.tabActive : ''}`}
                onClick={() => setTab('search')}
              >
                검색
              </button>
              <button
                type="button"
                className={`${styles.tab} ${tab === 'favorites' ? styles.tabActive : ''}`}
                onClick={() => setTab('favorites')}
              >
                즐겨찾기
              </button>
            </div>

            {tab === 'search' && (
              <>
                <Field label="검색어" htmlFor="stop-search">
                  <input
                    id="stop-search"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder={`${prefecture} 내 장소 검색`}
                  />
                </Field>
                <div className={styles.results}>
                  {searchLoading && <div className={styles.resultsLoading}><Spinner size={20} /></div>}
                  {!searchLoading && query.trim() && results.length === 0 && (
                    <p className={styles.resultsEmpty}>일치하는 장소가 없어요</p>
                  )}
                  {results.map((d) => (
                    <PlaceResult key={`${d._type}-${d.id}`} item={d} onAdd={() => addStop(d, d._type)} label="+ 추가" />
                  ))}
                </div>
              </>
            )}

            {tab === 'favorites' && (
              <div className={styles.results}>
                {favsLoading && <div className={styles.resultsLoading}><Spinner size={20} /></div>}
                {!favsLoading && favs && favs.length === 0 && (
                  <p className={styles.resultsEmpty}>
                    즐겨찾기가 없어요. <a href="/destination">여행지</a> 또는 <a href="/festival">축제</a>에서 추가해보세요.
                  </p>
                )}
                {!favsLoading && favs && favs.map((f) => (
                  <PlaceResult key={`${f._type}-${f.id}`} item={f} onAdd={() => addStop(f, f._type)} label="+ 구간 추가" />
                ))}
              </div>
            )}
          </div>
        </section>

        <section className={styles.right}>
          <div className={styles.card}>
            <header className={styles.timelineHead}>
              <h2 className={styles.cardTitle}>
                일정 <span className={styles.count}>{stopsCount}</span>
                {days > 0 && <span style={{ marginLeft: 8, fontSize: 'var(--fs-sm)', color: 'var(--color-ink-500)' }}>{days}일</span>}
              </h2>
              {errors.stops && <span className={styles.errorMsg}>{errors.stops}</span>}
            </header>

            {stops.length === 0 ? (
              <EmptyState
                icon="🧭"
                title="아직 일정이 없어요"
                description="왼쪽에서 장소를 검색해 추가하면 여기 타임라인에 쌓입니다."
                size="sm"
              />
            ) : (
              <ol className={styles.timeline}>
                {stops.map((s, i) => (
                  <li key={i} className={styles.stop}>
                    <div className={styles.stopHead}>
                      <span className={styles.stopIndex}>{i + 1}</span>
                      <select
                        className={styles.timeInput}
                        value={s.dayNo}
                        onChange={(e) => update(i, 'dayNo', Number(e.target.value))}
                        aria-label={`${i + 1}번 일정 일차`}
                      >
                        {Array.from({ length: Math.max(days, s.dayNo) + 1 }, (_, k) => k + 1).map((d) => (
                          <option key={d} value={d}>{d}일차</option>
                        ))}
                      </select>
                      <span className={styles.titleInput} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span className={s.type === 'festival' ? styles.badgeFest : styles.badgeDest}>
                          {s.type === 'festival' ? '축제' : '여행지'}
                        </span>
                        {s.name}
                      </span>
                      <div className={styles.stopActions}>
                        <IconButton label="위로" size="sm" onClick={() => move(i, -1)} disabled={i === 0}>↑</IconButton>
                        <IconButton label="아래로" size="sm" onClick={() => move(i, 1)} disabled={i === stops.length - 1}>↓</IconButton>
                        <IconButton label="삭제" size="sm" onClick={() => setStops(stops.filter((_, k) => k !== i))}>×</IconButton>
                      </div>
                    </div>
                    <textarea
                      className={styles.descInput}
                      value={s.memo}
                      maxLength={500}
                      onChange={(e) => update(i, 'memo', e.target.value)}
                      aria-label={`${i + 1}번 일정 메모`}
                      placeholder="이 장소에 대한 메모 (예: 오전 9시 도착, 점심은 근처 시장)"
                    />
                  </li>
                ))}
              </ol>
            )}
          </div>
        </section>
      </div>
    </Container>
  );
}

function PlaceResult({ item, onAdd, label }) {
  return (
    <button type="button" className={styles.resultItem} onClick={onAdd}>
      <span className={styles.resultName}>
        <span className={item._type === 'festival' ? styles.badgeFest : styles.badgeDest}>
          {item._type === 'festival' ? '축제' : '여행지'}
        </span>
        {item.name}
      </span>
      <span className={styles.resultAdd}>{label}</span>
    </button>
  );
}
