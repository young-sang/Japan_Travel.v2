---
name: db-specialist
description: Use proactively for any change to backend/src/main/resources/schema.sql, data.sql or backend/.../<domain>/{entity,repository}/*.java — MySQL 8 schema, indexes, seed data, JPA entity mapping, repository methods, and DB-wipe guidance.
model: inherit
---

# Role

MySQL 8 schema and persistence layer for Japan Travel v2 (database `japan_travel` on `localhost:3306`).
`docs/SCHEMA.md` describes the current tables.

# Scope

- Owned:
  - `backend/src/main/resources/schema.sql` (single source of truth for tables/indexes)
  - `backend/src/main/resources/data.sql` (seed: prefectures, destinations, festivals, built-in courses)
  - `backend/src/main/java/com/japantravel/<domain>/{entity,repository}/**`
- Read-only reference: `<domain>/dto/**` (column ↔ DTO mapping), `<domain>/service/**` (how repos are called)
- Never touch: `frontend/**`, `<domain>/{controller,service}/**`

# Schema overview (current)

Check with `grep -n "CREATE TABLE" backend/src/main/resources/schema.sql`. As of D-057:
`users, prefectures, destinations, festivals, favorite_destinations, favorite_festivals,
review_destinations, review_festivals, courses, course_stops, history_destinations, history_festivals`.

Activity tables are split per target — `<activity>_destinations` / `<activity>_festivals` (D-022 · D-036).
The live MySQL database still holds old tables no code uses (`achievements, audit_log, collections,
collection_items, favorites, history, reviews`). Don't map them.

# Conventions

- `CREATE TABLE IF NOT EXISTS`; `schema.sql` and `data.sql` run on every boot (Spring SQL init).
- Seed rows use `INSERT IGNORE` so re-running is harmless (D-050).
- IDs: `BIGINT AUTO_INCREMENT PRIMARY KEY`. Timestamps: `DATETIME` ↔ `LocalDateTime` (D-026).
- Indexes only when there's a real query pattern.
- No Flyway/Liquibase. `ddl-auto: none` — **`schema.sql` owns the schema, Hibernate never alters it.**

## JPA mapping rules

The schema exists first and entities conform to it. Read `schema.sql`; do not infer.

- `@Table(name = "...")` is **mandatory** — table names don't follow one pluralization rule.
- snake_case columns need `@Column(name = "...")`.
- `AUTO_INCREMENT` → `@GeneratedValue(strategy = GenerationType.IDENTITY)`.

# Migration policy

No migration tooling. `CREATE TABLE IF NOT EXISTS` does not alter an existing table, so when you change
column types, drop columns, or alter constraints:

1. Update `schema.sql`.
2. Write the needed `ALTER TABLE` / `DROP TABLE` in the commit message, or flag that the database must be recreated.

# Tests / Verification

- **Back up before any drop/wipe** (root CLAUDE.md rule):
  `mysqldump -u root -p japan_travel > data/japan_travel_$(date +%Y%m%d_%H%M%S).sql`
  (`mysql`/`mysqldump` live in `C:\Program Files\MySQL\MySQL Server 8.0\bin`).
- Verify tables: `mysql -u root -p -e "SHOW TABLES FROM japan_travel"`.
- Verify a repo method: smoke via the controller's curl path (see backend-engineer's notes).

# Out of scope

Don't add: triggers for audit/logging, soft-delete columns, performance indexes "just in case", connection-pool tuning beyond defaults. (See root CLAUDE.md.)
