// API wrapper using fetch. Vite proxy routes /api → http://localhost:8080.
//
// 인증은 JWT (D-033). 토큰은 localStorage 에 두고 모든 요청에 Bearer 로 붙인다 (D-054).
// 서버 응답은 {success, data, error} 봉투 (D-035) — 여기서 data 만 꺼내 돌려주고,
// 실패면 error.code · error.message 를 실은 Error 를 던진다. 화면 코드는 봉투를 모른다.

const TOKEN_KEY = 'jt.token';

export const tokenStore = {
  get: () => { try { return localStorage.getItem(TOKEN_KEY); } catch { return null; } },
  set: (t) => { try { localStorage.setItem(TOKEN_KEY, t); } catch {} },
  clear: () => { try { localStorage.removeItem(TOKEN_KEY); } catch {} },
};

async function req(path, opts = {}) {
  const token = tokenStore.get();
  const res = await fetch(path, {
    ...opts,
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(opts.headers || {}),
    },
    body: opts.body ? JSON.stringify(opts.body) : undefined,
  });
  const text = await res.text();
  let envelope = null;
  try { envelope = text ? JSON.parse(text) : null; } catch {}
  if (!res.ok || !envelope?.success) {
    const err = new Error(envelope?.error?.message || `${res.status} ${res.statusText}`);
    err.status = res.status;
    err.code = envelope?.error?.code;
    throw err;
  }
  return envelope.data;
}

// 화면은 소문자 type('destination' | 'festival') 을 쓰고 경로는 복수형이다
const plural = (type) => `${type}s`;

// 서버 enum(DESTINATION · FESTIVAL) → 화면 type
export const typeSlug = (serverType) => String(serverType || '').toLowerCase();

function query(params) {
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
  const s = q.toString();
  return s ? `?${s}` : '';
}

export const api = {
  // destinations
  listDestinations: ({ prefecture } = {}) => req(`/api/destinations${query({ prefecture })}`),
  getDestination: (id) => req(`/api/destinations/${id}`),
  //  Top25 — 종합 점수 순위 (D-076)
  destinationRanking: () => req('/api/destinations/ranking'),

  // festivals
  listFestivals: ({ prefecture, month } = {}) => req(`/api/festivals${query({ prefecture, month })}`),
  getFestival: (id) => req(`/api/festivals/${id}`),

  // news — 조회는 공개, 쓰기 · 수정 · 삭제는 관리자만 (D-079)
  listNews: () => req('/api/news'),
  getNews: (id) => req(`/api/news/${id}`),
  createNews: (body) => req('/api/news', { method: 'POST', body }),
  updateNews: (id, body) => req(`/api/news/${id}`, { method: 'PUT', body }),
  deleteNews: (id) => req(`/api/news/${id}`, { method: 'DELETE' }),

  // courses — 목록은 공개 코스 요약, 상세는 정류장까지 (D-046)
  listCourses: ({ prefecture } = {}) => req(`/api/courses${query({ prefecture })}`),
  myCourses: () => req('/api/me/courses'),
  getCourse: (id) => req(`/api/courses/${id}`),
  createCourse: (body) => req('/api/courses', { method: 'POST', body }),
  updateCourse: (id, body) => req(`/api/courses/${id}`, { method: 'PUT', body }),
  deleteCourse: (id) => req(`/api/courses/${id}`, { method: 'DELETE' }),

  // favorites — {destinations, festivals} 두 배열 (D-039)
  listFavorites: () => req('/api/me/favorites'),
  addFavorite: (type, id) => req(`/api/me/favorites/${plural(type)}/${id}`, { method: 'POST' }),
  removeFavorite: (type, id) => req(`/api/me/favorites/${plural(type)}/${id}`, { method: 'DELETE' }),

  // reviews — 대상 아래 중첩 (D-037)
  reviewsFor: (type, id) => req(`/api/${plural(type)}/${id}/reviews`),
  addReview: (type, id, body) => req(`/api/${plural(type)}/${id}/reviews`, { method: 'POST', body }),
  updateReview: (type, id, reviewId, body) => req(`/api/${plural(type)}/${id}/reviews/${reviewId}`, { method: 'PUT', body }),
  deleteReview: (type, id, reviewId) => req(`/api/${plural(type)}/${id}/reviews/${reviewId}`, { method: 'DELETE' }),
  myReviews: () => req('/api/me/reviews'),

  // history — 최근 본 장소 (D-052)
  history: () => req('/api/me/history'),
  touchHistory: (type, id) => req(`/api/me/history/${plural(type)}/${id}`, { method: 'POST' }),
  clearHistory: () => req('/api/me/history', { method: 'DELETE' }),

  // search (D-053)
  search: (q) => req(`/api/search${query({ q })}`),

  // auth — login · signup 은 {token, user} 를 돌려준다
  me: () => req('/api/auth/me'),
  login: (username, password) => req('/api/auth/login', { method: 'POST', body: { username, password } }),
  signup: (username, password, nickname) => req('/api/auth/signup', { method: 'POST', body: { username, password, nickname } }),

  // account — 계정 관리 (D-056). 비밀번호 불일치는 code WRONG_PASSWORD (400)
  updateProfile: (nickname) => req('/api/me/profile', { method: 'PATCH', body: { nickname } }),
  changePassword: (currentPassword, newPassword) => req('/api/me/password', { method: 'PUT', body: { currentPassword, newPassword } }),
  checkPassword: (password) => req('/api/me/password/check', { method: 'POST', body: { password } }),
  withdraw: (password) => req('/api/me', { method: 'DELETE', body: { password } }),
};
