// API wrapper using fetch. Vite proxy routes /api → http://localhost:8080.

async function req(path, opts = {}) {
  const res = await fetch(path, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json; charset=utf-8', ...(opts.headers || {}) },
    ...opts,
    body: opts.body ? JSON.stringify(opts.body) : undefined,
  });
  if (res.status === 204) return null;
  const text = await res.text();
  if (!res.ok) {
    const err = new Error(`${res.status} ${res.statusText}: ${text}`);
    err.status = res.status;
    try { err.body = JSON.parse(text); } catch {}
    throw err;
  }
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
  listCourses: ({ prefecture, tag, mine, status } = {}) => {
    const q = new URLSearchParams();
    if (prefecture) q.set('prefecture', prefecture);
    if (tag) q.set('tag', tag);
    if (mine) q.set('mine', 'true');
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

  // posts (board)
  listPosts: (page = 0, size = 20) => req(`/api/posts?page=${page}&size=${size}`),
  getPost: (id) => req(`/api/posts/${id}`),
  createPost: (body) => req('/api/posts', { method: 'POST', body }),
  updatePost: (id, body) => req(`/api/posts/${id}`, { method: 'PUT', body }),
  deletePost: (id) => req(`/api/posts/${id}`, { method: 'DELETE' }),
  listComments: (postId) => req(`/api/posts/${postId}/comments`),
  createComment: (postId, body) => req(`/api/posts/${postId}/comments`, { method: 'POST', body: { body } }),
  deleteComment: (id) => req(`/api/comments/${id}`, { method: 'DELETE' }),
  adminListPosts: (page = 0, size = 20) => req(`/api/admin/posts?page=${page}&size=${size}`),
  adminDeletePost: (id) => req(`/api/admin/posts/${id}`, { method: 'DELETE' }),
  adminDeleteComment: (id) => req(`/api/admin/comments/${id}`, { method: 'DELETE' }),

  // history
  history: () => req('/api/history'),
  touchHistory: (targetType, targetId) => req('/api/history', { method: 'POST', body: { targetType, targetId } }),
  clearHistory: () => req('/api/history', { method: 'DELETE' }),

  // search
  search: (q) => req(`/api/search?q=${encodeURIComponent(q)}`),

  // proxy
  weather: (prefecture) => req(`/api/weather?prefecture=${encodeURIComponent(prefecture || '도쿄도')}`),
  fx: () => req('/api/fx'),

  // auth
  me: () => req('/api/auth/me'),
  login: (username, password) => req('/api/auth/login', { method: 'POST', body: { username, password } }),
  signup: (username, password, nickname) => req('/api/auth/signup', { method: 'POST', body: { username, password, nickname } }),
  logout: () => req('/api/auth/logout', { method: 'POST' }),

  // admin
  stats: () => req('/api/admin/stats'),
  runCollector: (type, prefecture) => req('/api/admin/collector/run', { method: 'POST', body: { type, prefecture } }),
  collectorRun: (id) => req(`/api/admin/collector/runs/${id}`),
  recentCollectorRuns: (limit = 20) => req(`/api/admin/collector/runs?limit=${limit}`),
  cacheStats: () => req('/api/admin/cache/stats'),
  invalidateCache: (name) => req(`/api/admin/cache/invalidate${name ? '?name=' + name : ''}`, { method: 'POST' }),

  collectionMatrix: () => req('/api/admin/collection-matrix'),

  // admin: bulk collector
  runBulkCollector: async () => {
    try {
      return await req('/api/admin/collector/bulk', { method: 'POST' });
    } catch (e) {
      if (e?.status === 409 && e?.body?.code === 'BULK_ALREADY_RUNNING') {
        return { conflict: true, bulkRunId: e.body.bulkRunId };
      }
      throw e;
    }
  },
  bulkRuns: (limit = 20) => req(`/api/admin/collector/bulk-runs?limit=${limit}`),
  bulkRunDetail: (id) => req(`/api/admin/collector/bulk-runs/${id}`),
  retryRun: (id) => req(`/api/admin/collector/runs/${id}/retry`, { method: 'POST' }),
  retryFailedInBulk: (id) => req(`/api/admin/collector/bulk-runs/${id}/retry-failed`, { method: 'POST' }),

  // admin: users
  adminListUsers: () => req('/api/admin/users'),
  adminSetUserRole: (id, role) => req(`/api/admin/users/${id}/role`, { method: 'PATCH', body: { role } }),
  adminDeleteUser: (id) => req(`/api/admin/users/${id}`, { method: 'DELETE' }),

  // admin: audit
  adminListAudit: ({ userId, action, from, to, page = 0, size = 50 } = {}) => {
    const q = new URLSearchParams();
    if (userId) q.set('userId', userId);
    if (action) q.set('action', action);
    if (from) q.set('from', from);
    if (to) q.set('to', to);
    q.set('page', String(page));
    q.set('size', String(size));
    return req(`/api/admin/audit?${q.toString()}`);
  },

  // admin: content CRUD
  adminCreateDestination: (body) => req('/api/admin/destinations', { method: 'POST', body }),
  adminUpdateDestination: (id, body) => req(`/api/admin/destinations/${id}`, { method: 'PUT', body }),
  adminDeleteDestination: (id) => req(`/api/admin/destinations/${id}`, { method: 'DELETE' }),
  adminCreateFestival: (body) => req('/api/admin/festivals', { method: 'POST', body }),
  adminUpdateFestival: (id, body) => req(`/api/admin/festivals/${id}`, { method: 'PUT', body }),
  adminDeleteFestival: (id) => req(`/api/admin/festivals/${id}`, { method: 'DELETE' }),
};
