-- destination · festival · user 도메인 (MySQL)

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
  created_at    DATETIME NOT NULL,
  UNIQUE (name, prefecture_id),
  FOREIGN KEY (prefecture_id) REFERENCES prefectures(id),
  -- MySQL 은 8.0.16 부터 CHECK 를 실제로 강제한다 (그 이전은 파싱만 하고 무시)
  CHECK (month BETWEEN 1 AND 12)
);

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
