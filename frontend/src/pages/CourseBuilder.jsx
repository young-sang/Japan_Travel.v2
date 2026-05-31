import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
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

export default function CourseBuilder() {
  const { id } = useParams();
  const editing = !!id;
  const navigate = useNavigate();
  const toast = useToast();

  const [title, setTitle] = useState('');
  const [prefecture, setPrefecture] = useState('도쿄도');
  const [duration, setDuration] = useState('약 8시간');
  const [tags, setTags] = useState('');
  const [imagePath, setImagePath] = useState('');
  const [stops, setStops] = useState([]);
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
          setTitle(c.title || ''); setPrefecture(c.prefecture || '도쿄도'); setDuration(c.duration || '');
          setTags((c.tags || []).join(', ')); setImagePath(c.imagePath || '');
          setStops(c.timeline || []);
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

  useEffect(() => {
    if (tab !== 'favorites' || favs !== null) return;
    setFavsLoading(true);
    (async () => {
      try {
        const list = await api.listFavorites();
        const enriched = await Promise.all(list
          .filter((f) => f.targetType !== 'course')
          .map(async (f) => {
            try {
              const fn = f.targetType === 'destination' ? api.getDestination : api.getFestival;
              return { ...f, detail: await fn(f.targetId) };
            } catch { return null; }
          }));
        setFavs(enriched.filter(Boolean));
      } catch { setFavs([]); }
      finally { setFavsLoading(false); }
    })();
  }, [tab, favs]);

  const stopsCount = stops.length;
  const stopSummary = useMemo(() => stops.map((s, i) => `${i + 1}. ${s.title}`).join(' → '), [stops]);

  function addStop(item, type = 'destination') {
    setStops((s) => [...s, {
      time: defaultTimeFor(s.length),
      title: item.name,
      desc: (item.description || '').slice(0, 80),
      lat: item.lat, lng: item.lng,
      targetType: type, targetId: item.id,
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

  async function save(status) {
    if (!validate()) {
      toast.warn('입력 내용을 확인해주세요');
      return;
    }
    setSaving(true);
    const body = {
      title, prefecture, duration, imagePath: imagePath || null,
      tags: tags.split(',').map((t) => t.trim()).filter(Boolean),
      centerLat: stops[0]?.lat, centerLng: stops[0]?.lng,
      timeline: stops, status,
    };
    try {
      if (editing) await api.updateCourse(id, body);
      else await api.createCourse(body);
      toast.success(status === 'draft' ? '임시 저장했어요' : '발행했어요');
      navigate('/mypage/courses');
    } catch (err) {
      toast.error('저장에 실패했습니다');
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
            <Button variant="secondary" onClick={() => save('draft')} loading={saving}>임시 저장</Button>
            <Button variant="primary" onClick={() => save('published')} loading={saving}>발행</Button>
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
              <Field label="지역" required>
                <select value={prefecture} onChange={(e) => setPrefecture(e.target.value)}>
                  {PREFECTURES.map((p) => <option key={p}>{p}</option>)}
                </select>
              </Field>
              <Field label="소요 시간">
                <input value={duration} onChange={(e) => setDuration(e.target.value)} placeholder="약 8시간" />
              </Field>
              <Field label="태그" helper="쉼표(,)로 구분">
                <input value={tags} onChange={(e) => setTags(e.target.value)} placeholder="역사, 당일여행" />
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
                    <button
                      key={`${d._type}-${d.id}`}
                      type="button"
                      className={styles.resultItem}
                      onClick={() => addStop(d, d._type)}
                    >
                      <span className={styles.resultName}>
                        <span className={d._type === 'festival' ? styles.badgeFest : styles.badgeDest}>
                          {d._type === 'festival' ? '축제' : '여행지'}
                        </span>
                        {d.name}
                      </span>
                      <span className={styles.resultAdd}>+ 추가</span>
                    </button>
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
                  <button
                    key={`${f.targetType}-${f.targetId}`}
                    type="button"
                    className={styles.resultItem}
                    onClick={() => addStop(f.detail, f.targetType)}
                  >
                    <span className={styles.resultName}>
                      <span className={f.targetType === 'festival' ? styles.badgeFest : styles.badgeDest}>
                        {f.targetType === 'festival' ? '축제' : '여행지'}
                      </span>
                      {f.detail.name}
                    </span>
                    <span className={styles.resultAdd}>+ 구간 추가</span>
                  </button>
                ))}
              </div>
            )}
          </div>
        </section>

        <section className={styles.right}>
          <div className={styles.card}>
            <header className={styles.timelineHead}>
              <h2 className={styles.cardTitle}>일정 <span className={styles.count}>{stopsCount}</span></h2>
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
                      <input
                        className={styles.timeInput}
                        value={s.time}
                        onChange={(e) => update(i, 'time', e.target.value)}
                        aria-label={`${i + 1}번 일정 시간`}
                        placeholder="HH:MM"
                      />
                      <input
                        className={styles.titleInput}
                        value={s.title}
                        onChange={(e) => update(i, 'title', e.target.value)}
                        aria-label={`${i + 1}번 일정 제목`}
                      />
                      <div className={styles.stopActions}>
                        <IconButton label="위로" size="sm" onClick={() => move(i, -1)} disabled={i === 0}>↑</IconButton>
                        <IconButton label="아래로" size="sm" onClick={() => move(i, 1)} disabled={i === stops.length - 1}>↓</IconButton>
                        <IconButton label="삭제" size="sm" onClick={() => setStops(stops.filter((_, k) => k !== i))}>×</IconButton>
                      </div>
                    </div>
                    <textarea
                      className={styles.descInput}
                      value={s.desc}
                      onChange={(e) => update(i, 'desc', e.target.value)}
                      aria-label={`${i + 1}번 일정 설명`}
                      placeholder="이 장소에 대한 메모…"
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

function defaultTimeFor(idx) {
  const h = 9 + idx * 2;
  if (h >= 24) return '23:00';
  return `${String(h).padStart(2, '0')}:00`;
}
