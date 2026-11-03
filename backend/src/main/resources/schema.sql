-- destination · festival · user · favorite · review · course 도메인 (MySQL)

CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,          -- BCrypt 는 60자 고정
  nickname      VARCHAR(50)  NOT NULL,
  role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
  created_at    DATETIME     NOT NULL,
  CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS prefectures (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS destinations (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(200) NOT NULL,
  prefecture_id BIGINT NOT NULL,
  description   TEXT,
  lat           DOUBLE,
  lng           DOUBLE,
  image_path    VARCHAR(500),
  image_credit  VARCHAR(300),                    -- 사진 출처 "작가 / 라이선스 / Wikimedia Commons" (D-063)
  image_source  VARCHAR(500),                    -- 사진 원본 페이지 주소
  created_at    DATETIME NOT NULL,
  UNIQUE (name, prefecture_id),
  FOREIGN KEY (prefecture_id) REFERENCES prefectures(id)
);

CREATE TABLE IF NOT EXISTS festivals (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(200) NOT NULL,
  prefecture_id BIGINT NOT NULL,
  month         INT NOT NULL,
  date_text     VARCHAR(100),
  description   TEXT,
  lat           DOUBLE,
  lng           DOUBLE,
  image_path    VARCHAR(500),
  image_credit  VARCHAR(300),
  image_source  VARCHAR(500),
  created_at    DATETIME NOT NULL,
  UNIQUE (name, prefecture_id),
  FOREIGN KEY (prefecture_id) REFERENCES prefectures(id),
  -- MySQL 은 8.0.16 부터 CHECK 를 실제로 강제한다 (그 이전은 파싱만 하고 무시)
  CHECK (month BETWEEN 1 AND 12)
);

-- 사진 출처 컬럼은 나중에 생겼다 (D-063). 위 CREATE 는 이미 있는 테이블을 건드리지 않으므로, 기존 DB 에는
-- 컬럼이 없을 때만 ALTER 한다. MySQL 8 에는 ADD COLUMN IF NOT EXISTS 가 없어 information_schema 로 확인한다.
SET @ddl = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE destinations ADD COLUMN image_credit VARCHAR(300) AFTER image_path, ADD COLUMN image_source VARCHAR(500) AFTER image_credit',
  'DO 0') FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'destinations' AND column_name = 'image_credit');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE festivals ADD COLUMN image_credit VARCHAR(300) AFTER image_path, ADD COLUMN image_source VARCHAR(500) AFTER image_credit',
  'DO 0') FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'festivals' AND column_name = 'image_credit');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- favorite (Task 3) — 대상별로 테이블을 나눈다 (D-022). 키는 id + UNIQUE (D-036)
-- UNIQUE 가 user_id 로 시작하므로 "내 목록" 조회의 인덱스도 겸한다
CREATE TABLE IF NOT EXISTS favorite_destinations (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT   NOT NULL,
  destination_id BIGINT   NOT NULL,
  created_at     DATETIME NOT NULL,
  UNIQUE (user_id, destination_id),
  FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE CASCADE,
  FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS favorite_festivals (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT   NOT NULL,
  festival_id BIGINT   NOT NULL,
  created_at  DATETIME NOT NULL,
  UNIQUE (user_id, festival_id),
  FOREIGN KEY (user_id)     REFERENCES users(id)     ON DELETE CASCADE,
  FOREIGN KEY (festival_id) REFERENCES festivals(id) ON DELETE CASCADE
);

-- review (Task 3) — 대상별 테이블 (D-036). UNIQUE 없음 — 같은 대상에 여러 개 쓸 수 있다 (D-013)
-- updated_at 은 수정할 때만 채운다
CREATE TABLE IF NOT EXISTS review_destinations (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT   NOT NULL,
  destination_id BIGINT   NOT NULL,
  rating         INT      NOT NULL,
  comment        TEXT,
  created_at     DATETIME(6) NOT NULL,
  updated_at     DATETIME,
  FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE CASCADE,
  FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
  CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE IF NOT EXISTS review_festivals (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT   NOT NULL,
  festival_id BIGINT   NOT NULL,
  rating      INT      NOT NULL,
  comment     TEXT,
  created_at  DATETIME(6) NOT NULL,
  updated_at  DATETIME,
  FOREIGN KEY (user_id)     REFERENCES users(id)     ON DELETE CASCADE,
  FOREIGN KEY (festival_id) REFERENCES festivals(id) ON DELETE CASCADE,
  CHECK (rating BETWEEN 1 AND 5)
);

-- course (Task 4) — 정류장은 여행지·축제 중 하나를 가리킨다 (D-042 · D-044)
CREATE TABLE IF NOT EXISTS courses (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  title         VARCHAR(200) NOT NULL,
  description   TEXT,
  prefecture_id BIGINT       NOT NULL,
  image_path    VARCHAR(500),
  owner_user_id BIGINT,                          -- NULL = 기본 제공 코스 (D-041)
  is_public     BOOLEAN      NOT NULL,
  created_at    DATETIME     NOT NULL,
  updated_at    DATETIME,                        -- 수정할 때만 채운다
  FOREIGN KEY (prefecture_id) REFERENCES prefectures(id),
  FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- destination_id · festival_id 중 정확히 하나는 서비스가 판정한다.
-- MySQL 은 참조 동작(CASCADE)이 걸린 컬럼을 CHECK 에 쓸 수 없다 (에러 3823).
CREATE TABLE IF NOT EXISTS course_stops (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id      BIGINT       NOT NULL,
  day_no         INT          NOT NULL,
  seq            INT          NOT NULL,
  destination_id BIGINT,
  festival_id    BIGINT,
  memo           VARCHAR(500),
  UNIQUE (course_id, day_no, seq),
  FOREIGN KEY (course_id)      REFERENCES courses(id)      ON DELETE CASCADE,
  FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
  FOREIGN KEY (festival_id)    REFERENCES festivals(id)    ON DELETE CASCADE,
  CHECK (day_no >= 1),
  CHECK (seq >= 1)
);

-- history (최근 본 장소) — favorite 와 같은 모양 (D-036) + visited_at (D-052)
-- 대상당 1행. 다시 보면 visited_at 만 갱신한다
CREATE TABLE IF NOT EXISTS history_destinations (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT   NOT NULL,
  destination_id BIGINT   NOT NULL,
  visited_at     DATETIME(6) NOT NULL,
  UNIQUE (user_id, destination_id),
  FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE CASCADE,
  FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS history_festivals (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT   NOT NULL,
  festival_id BIGINT   NOT NULL,
  visited_at  DATETIME(6) NOT NULL,
  UNIQUE (user_id, festival_id),
  FOREIGN KEY (user_id)     REFERENCES users(id)     ON DELETE CASCADE,
  FOREIGN KEY (festival_id) REFERENCES festivals(id) ON DELETE CASCADE
);

-- 내 리뷰 · 최근 본 장소는 두 테이블을 합쳐 시각으로 정렬한다 (D-065). 초 단위면 같은 초의 기록끼리 순서가
-- 흔들리므로 마이크로초로 둔다. 위 CREATE 는 기존 테이블을 건드리지 않으니 정밀도가 0 일 때만 ALTER 한다.
SET @ddl = (SELECT IF(COUNT(*) = 1,
  'ALTER TABLE review_destinations MODIFY created_at DATETIME(6) NOT NULL', 'DO 0') FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'review_destinations' AND column_name = 'created_at' AND datetime_precision = 0);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) = 1,
  'ALTER TABLE review_festivals MODIFY created_at DATETIME(6) NOT NULL', 'DO 0') FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'review_festivals' AND column_name = 'created_at' AND datetime_precision = 0);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) = 1,
  'ALTER TABLE history_destinations MODIFY visited_at DATETIME(6) NOT NULL', 'DO 0') FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'history_destinations' AND column_name = 'visited_at' AND datetime_precision = 0);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) = 1,
  'ALTER TABLE history_festivals MODIFY visited_at DATETIME(6) NOT NULL', 'DO 0') FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'history_festivals' AND column_name = 'visited_at' AND datetime_precision = 0);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT IGNORE INTO prefectures(name) VALUES
  ('홋카이도'), ('아오모리현'), ('이와테현'), ('미야기현'),
  ('아키타현'), ('야마가타현'), ('후쿠시마현'), ('이바라키현'),
  ('도치기현'), ('군마현'), ('사이타마현'), ('지바현'),
  ('도쿄도'), ('가나가와현'), ('니가타현'), ('도야마현'),
  ('이시카와현'), ('후쿠이현'), ('야마나시현'), ('나가노현'),
  ('기후현'), ('시즈오카현'), ('아이치현'), ('미에현'),
  ('시가현'), ('교토부'), ('오사카부'), ('효고현'),
  ('나라현'), ('와카야마현'), ('돗토리현'), ('시마네현'),
  ('오카야마현'), ('히로시마현'), ('야마구치현'), ('도쿠시마현'),
  ('가가와현'), ('에히메현'), ('고치현'), ('후쿠오카현'),
  ('사가현'), ('나가사키현'), ('구마모토현'), ('오이타현'),
  ('미야자키현'), ('가고시마현'), ('오키나와현');
