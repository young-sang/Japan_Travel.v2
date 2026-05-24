// API wrapper using fetch. Vite proxy routes /api → http://localhost:8080.

async function req(path, opts = {}) {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json; charset=utf-8', ...(opts.headers || {}) },
    ...opts,
    body: opts.body ? JSON.stringify(opts.body) : undefined,
  });
  if (res.status === 204) return null;
  const text = await res.text();
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}: ${text}`);
  return text ? JSON.parse(text) : null;
}

export const api = {
  // destinations
  listDestinations: ({ prefecture, tag } = {}) => {
    const q = new URLSearchParams();
    if (prefecture) q.set('prefecture', prefecture);
    if (tag) q.set('tag', tag);
    return req(`/api/destinations${q.toString() ? '?' + q : ''}`);
  },
  getDestination: (id) => req(`/api/destinations/${id}`),

  // festivals
  listFestivals: ({ prefecture, month } = {}) => {
    const q = new URLSearchParams();
    if (prefecture) q.set('prefecture', prefecture);
    if (month) q.set('month', month);
    return req(`/api/festivals${q.toString() ? '?' + q : ''}`);
  },
  getFestival: (id) => req(`/api/festivals/${id}`),

  // courses
  listCourses: ({ prefecture, tag, ownerUserId, status } = {}) => {
    const q = new URLSearchParams();
    if (prefecture) q.set('prefecture', prefecture);
    if (tag) q.set('tag', tag);
    if (ownerUserId !== undefined) q.set('ownerUserId', ownerUserId);
    if (status) q.set('status', status);
    return req(`/api/courses${q.toString() ? '?' + q : ''}`);
  },
  getCourse: (id) => req(`/api/courses/${id}`),
  createCourse: (body) => req('/api/courses', { method: 'POST', body }),
  updateCourse: (id, body) => req(`/api/courses/${id}`, { method: 'PUT', body }),
  deleteCourse: (id) => req(`/api/courses/${id}`, { method: 'DELETE' }),

  // favorites
  listFavorites: () => req('/api/favorites'),
  addFavorite: (targetType, targetId) => req('/api/favorites', { method: 'POST', body: { targetType, targetId } }),
  removeFavorite: (targetType, targetId) => req(`/api/favorites/${targetType}/${targetId}`, { method: 'DELETE' }),

  // reviews
  reviewsFor: (targetType, targetId) => req(`/api/reviews?targetType=${targetType}&targetId=${targetId}`),
  myReviews: () => req('/api/reviews?mine=true'),
  allReviews: () => req('/api/reviews'),
  addReview: (body) => req('/api/reviews', { method: 'POST', body }),
  updateReview: (id, body) => req(`/api/reviews/${id}`, { method: 'PUT', body }),
  deleteReview: (id) => req(`/api/reviews/${id}`, { method: 'DELETE' }),

  // history
  history: () => req('/api/history'),
  touchHistory: (targetType, targetId) => req('/api/history', { method: 'POST', body: { targetType, targetId } }),
  clearHistory: () => req('/api/history', { method: 'DELETE' }),

  // search
  search: (q) => req(`/api/search?q=${encodeURIComponent(q)}`),

  // proxy
  weather: (prefecture) => req(`/api/weather?prefecture=${encodeURIComponent(prefecture || '도쿄도')}`),
  fx: () => req('/api/fx'),

  // admin
  stats: () => req('/api/admin/stats'),
  runCollector: (type, prefecture) => req('/api/admin/collector/run', { method: 'POST', body: { type, prefecture } }),
  collectorRun: (id) => req(`/api/admin/collector/runs/${id}`),
  recentCollectorRuns: (limit = 20) => req(`/api/admin/collector/runs?limit=${limit}`),
  cacheStats: () => req('/api/admin/cache/stats'),
  invalidateCache: (name) => req(`/api/admin/cache/invalidate${name ? '?name=' + name : ''}`, { method: 'POST' }),
};
