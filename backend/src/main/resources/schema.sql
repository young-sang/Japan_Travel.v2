-- Japan Travel v2 schema (SQLite). 시드 없음. Collector가 외부 API에서 적재.

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY,
  nickname TEXT NOT NULL DEFAULT '관리자',
  avatar_path TEXT,
  bio TEXT,
  default_prefecture TEXT,
  theme TEXT DEFAULT 'light',
  created_at TEXT DEFAULT (datetime('now'))
);
INSERT OR IGNORE INTO users (id, nickname) VALUES (1, '관리자');

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
  finished_at TEXT
);
