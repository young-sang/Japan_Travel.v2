-- Japan Travel v2 schema (SQLite). 시드 없음. Collector가 외부 API에서 적재.

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  nickname TEXT NOT NULL DEFAULT '관리자',
  role TEXT NOT NULL DEFAULT 'USER',
  avatar_path TEXT,
  bio TEXT,
  default_prefecture TEXT,
  theme TEXT DEFAULT 'light',
  created_at TEXT DEFAULT (datetime('now'))
);
-- admin 시드는 StartupRunner에서 BCryptPasswordEncoder로 처리 (id=1, username=admin, password=admin1234)

CREATE TABLE IF NOT EXISTS destinations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  prefecture TEXT NOT NULL,
  tags TEXT NOT NULL DEFAULT '[]',
  lat REAL,
  lng REAL,
  image_path TEXT,
  description TEXT,
  wiki_title TEXT UNIQUE,
  last_refreshed_at TEXT
);
CREATE INDEX IF NOT EXISTS idx_dest_prefecture ON destinations(prefecture);

CREATE TABLE IF NOT EXISTS festivals (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  prefecture TEXT NOT NULL,
  month INTEGER,
  date_text TEXT,
  image_path TEXT,
  description TEXT,
  wiki_title TEXT UNIQUE,
  lat REAL,
  lng REAL,
  last_refreshed_at TEXT
);
CREATE INDEX IF NOT EXISTS idx_fest_prefecture ON festivals(prefecture);

CREATE TABLE IF NOT EXISTS courses (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  prefecture TEXT NOT NULL,
  tags TEXT NOT NULL DEFAULT '[]',
  duration TEXT,
  image_path TEXT,
  center_lat REAL,
  center_lng REAL,
  timeline_json TEXT NOT NULL DEFAULT '[]',
  is_user_created INTEGER DEFAULT 0,
  owner_user_id INTEGER,
  status TEXT DEFAULT 'published',
  created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS favorites (
  user_id INTEGER NOT NULL,
  target_type TEXT NOT NULL,
  target_id INTEGER NOT NULL,
  created_at TEXT DEFAULT (datetime('now')),
  PRIMARY KEY (user_id, target_type, target_id)
);

CREATE TABLE IF NOT EXISTS reviews (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  target_type TEXT NOT NULL,
  target_id INTEGER NOT NULL,
  rating INTEGER NOT NULL,
  comment TEXT,
  created_at TEXT DEFAULT (datetime('now'))
);
CREATE INDEX IF NOT EXISTS idx_reviews_target ON reviews(target_type, target_id);

CREATE TABLE IF NOT EXISTS history (
  user_id INTEGER NOT NULL,
  target_type TEXT NOT NULL,
  target_id INTEGER NOT NULL,
  visited_at TEXT DEFAULT (datetime('now')),
  PRIMARY KEY (user_id, target_type, target_id)
);

CREATE TABLE IF NOT EXISTS collections (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  cover_path TEXT,
  created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS collection_items (
  collection_id INTEGER NOT NULL,
  target_type TEXT NOT NULL,
  target_id INTEGER NOT NULL,
  added_at TEXT DEFAULT (datetime('now')),
  PRIMARY KEY (collection_id, target_type, target_id)
);

CREATE TABLE IF NOT EXISTS achievements (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  code TEXT NOT NULL,
  unlocked_at TEXT,
  progress_value INTEGER,
  UNIQUE(user_id, code)
);

CREATE TABLE IF NOT EXISTS bulk_runs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  started_at TEXT NOT NULL,
  finished_at TEXT,
  status TEXT NOT NULL,
  total_tasks INTEGER NOT NULL,
  tasks_success INTEGER NOT NULL DEFAULT 0,
  tasks_partial INTEGER NOT NULL DEFAULT 0,
  tasks_failed  INTEGER NOT NULL DEFAULT 0,
  tasks_aborted INTEGER NOT NULL DEFAULT 0,
  notes TEXT
);

CREATE TABLE IF NOT EXISTS collector_runs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  type TEXT NOT NULL,
  prefecture TEXT,
  source TEXT NOT NULL,
  status TEXT NOT NULL,
  items_added INTEGER DEFAULT 0,
  items_updated INTEGER DEFAULT 0,
  items_failed INTEGER DEFAULT 0,
  errors_json TEXT,
  started_at TEXT DEFAULT (datetime('now')),
  finished_at TEXT,
  bulk_run_id INTEGER REFERENCES bulk_runs(id),
  last_heartbeat_at TEXT,
  stage TEXT
);
CREATE INDEX IF NOT EXISTS idx_runs_bulk ON collector_runs(bulk_run_id);
CREATE INDEX IF NOT EXISTS idx_runs_heartbeat ON collector_runs(status, last_heartbeat_at);

CREATE TABLE IF NOT EXISTS audit_log (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER,                  -- NULL 가능(로그인 실패 등)
  username TEXT,                    -- 사용자 삭제 후에도 흔적 보존
  action TEXT NOT NULL,             -- LOGIN_SUCCESS / LOGIN_FAIL / SIGNUP / LOGOUT
                                    -- REVIEW_CREATE / REVIEW_UPDATE / REVIEW_DELETE
                                    -- COURSE_CREATE / COURSE_UPDATE / COURSE_DELETE
                                    -- COLLECTOR_RUN / COLLECTOR_BULK
                                    -- USER_ROLE_CHANGE / USER_DELETE
                                    -- CONTENT_CREATE / CONTENT_UPDATE / CONTENT_DELETE
  target_type TEXT,
  target_id INTEGER,
  detail TEXT,                      -- 자유 텍스트(JSON 가능)
  created_at TEXT DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_log(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action, created_at DESC);

CREATE TABLE IF NOT EXISTS posts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  created_at TEXT DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_posts_created ON posts(created_at DESC);

CREATE TABLE IF NOT EXISTS post_comments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  post_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  body TEXT NOT NULL,
  created_at TEXT DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(post_id) REFERENCES posts(id) ON DELETE CASCADE,
  FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_post_comments_post ON post_comments(post_id, created_at);
