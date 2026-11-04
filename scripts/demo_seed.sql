-- 시연 데이터 (D-070). 여행지 · 축제 시드(data.sql) 가 들어간 뒤에 손으로 돌린다. 몇 번을 돌려도 같은 상태가 된다.
--
--   mysql -uroot -p --default-character-set=utf8mb4 japan_travel < scripts/demo_seed.sql
--
-- 계정 6개, 비밀번호는 모두 demo1234. 메인 시연 계정은 demo — 교토 · 오사카 여행을 준비하는 사람이다.
-- admin 은 뉴스를 쓰는 관리자다 (D-079).
-- 목록은 id 내림차순으로 보이므로(리뷰 · 코스 등) INSERT 마다 ORDER BY 로 오래된 것부터 id 를 받게 한다.
-- 장소는 이름으로 찾는다. 시각은 NOW() 기준으로 두어 언제 돌려도 "최근" 으로 보인다.

SET NAMES utf8mb4;

-- 다시 돌릴 때: 시연 계정을 지우면 코스 · 리뷰 · 즐겨찾기 · 최근 본 장소가 FK CASCADE 로 함께 지워진다.
-- 뉴스는 작성자를 지워도 남으므로(ON DELETE SET NULL) admin 이 쓴 글을 먼저 지운다
DELETE n FROM news n JOIN users u ON u.id = n.author_user_id WHERE u.username = 'admin';
DELETE FROM users WHERE username IN ('demo', 'kyoto_lover', 'foodie_park', 'photo_bear', 'weekend_trip', 'admin');

-- BCrypt("demo1234") — 회원가입 API 가 만든 해시를 그대로 옮겼다
SET @pw = '$2a$10$nEA1HxHVCVOpeGkN2/LK2uwGplF7Ty.73iAXXzBigYKL0WCz/JLVG';

INSERT INTO users (username, password_hash, nickname, role, created_at) VALUES
  ('demo',         @pw, '하루',       'USER', NOW() - INTERVAL 30 DAY),
  ('kyoto_lover',  @pw, '교토덕후',   'USER', NOW() - INTERVAL 60 DAY),
  ('foodie_park',  @pw, '먹방여행러', 'USER', NOW() - INTERVAL 45 DAY),
  ('photo_bear',   @pw, '사진찍는곰', 'USER', NOW() - INTERVAL 40 DAY),
  ('weekend_trip', @pw, '주말여행자', 'USER', NOW() - INTERVAL 20 DAY),
  ('admin',        @pw, '편집부',     'ADMIN', NOW() - INTERVAL 90 DAY);

-- 뉴스 4건 — 직접 쓴 글. 사진은 여행지의 Commons 사진과 출처를 그대로 쓴다 (D-063 · D-079)
INSERT INTO news (title, body, image_path, image_credit, author_user_id, created_at)
SELECT v.title, v.body, d.image_path, d.image_credit, u.id, NOW(6) - INTERVAL v.days DAY
FROM (VALUES
  ROW('여행지 TOP 25 를 새로 열었습니다',
      '여행자들이 많이 둘러보고, 찜하고, 리뷰를 남긴 여행지를 점수로 모아 TOP 25 페이지를 만들었습니다.\n본 사람은 1점, 찜은 2점, 리뷰는 3점입니다. 순위는 기록이 쌓이는 대로 바로 바뀝니다.\n홈 화면의 빠른 메뉴에서 들어갈 수 있습니다.',
      '후시미 이나리 타이샤', 1),
  ROW('교토 단풍 시즌, 붐비지 않게 다니는 법',
      '11월 중순부터 12월 초까지 교토는 단풍을 보러 온 사람으로 가득합니다.\n기요미즈데라와 후시미 이나리는 아침 8시 전에 가면 한결 여유롭습니다. 아라시야마는 오후보다 오전이 낫고, 저녁에는 사찰마다 여는 야간 라이트업을 노려 보세요.\n이동은 버스보다 전철이 덜 막힙니다.',
      '기요미즈데라', 4),
  ROW('삿포로 눈축제 준비 체크리스트',
      '2월 초 삿포로 눈축제는 오도리 공원 일대에 커다란 눈 조각이 늘어서는 홋카이도의 대표 겨울 행사입니다.\n밤 기온은 영하 10도 가까이 내려가니 방한화와 장갑, 핫팩을 꼭 챙기세요. 축제 기간에는 숙소가 빨리 차므로 일찍 예약하는 편이 좋습니다.\n오타루 눈빛 거리와 묶으면 이틀 일정으로 알맞습니다.',
      '삿포로 오도리 공원', 9),
  ROW('겨울에 떠나는 따뜻한 오키나와',
      '12월에서 2월 사이에도 오키나와 본섬의 낮 기온은 20도 안팎입니다.\n바다 수영은 어렵지만 추라우미 수족관, 슈리성, 국제거리처럼 실내외를 오가는 일정에는 오히려 걷기 좋은 계절입니다.\n렌터카를 빌리면 북부 해안 드라이브까지 하루에 둘러볼 수 있습니다.',
      '추라우미 수족관', 15)
) AS v(title, body, place, days)
JOIN users u ON u.username = 'admin'
JOIN destinations d ON d.name = v.place
ORDER BY v.days DESC;

-- 여행지 리뷰. 주요 장소마다 2~4건이 보이도록 나눠 쓴다
INSERT INTO review_destinations (user_id, destination_id, rating, comment, created_at)
SELECT u.id, d.id, v.rating, v.comment, NOW(6) - INTERVAL v.days DAY - INTERVAL v.seq SECOND
FROM (VALUES
  ROW(1,  'kyoto_lover',  '기요미즈데라', 5, '아침 7시에 가면 사람이 거의 없어요. 무대에서 내려다보는 교토 시내가 정말 예쁩니다.', 40),
  ROW(2,  'photo_bear',   '기요미즈데라', 4, '산넨자카 · 니넨자카 골목과 같이 걸으면 좋아요. 낮에는 수학여행 학생이 많습니다.', 25),
  ROW(3,  'demo',         '기요미즈데라', 5, '가을 단풍 시기에 갔는데 야간 라이트업이 최고였어요.', 3),
  ROW(4,  'kyoto_lover',  '후시미 이나리 타이샤', 5, '센본도리이는 입구 쪽만 붐비고, 30분만 올라가도 한적해져요.', 38),
  ROW(5,  'weekend_trip', '후시미 이나리 타이샤', 4, '정상까지는 왕복 2~3시간. 편한 신발 필수입니다.', 12),
  ROW(6,  'photo_bear',   '후시미 이나리 타이샤', 5, '해 질 무렵 도리이 사이로 들어오는 빛이 사진 찍기 제일 좋아요.', 22),
  ROW(7,  'photo_bear',   '아라시야마 대나무 숲', 4, '이른 아침이 아니면 사람 없는 사진은 어렵습니다. 길이는 생각보다 짧아요.', 21),
  ROW(8,  'kyoto_lover',  '아라시야마 대나무 숲', 4, '도게쓰교와 묶어서 반나절 코스로 좋습니다.', 35),
  ROW(9,  'kyoto_lover',  '킨카쿠지', 5, '맑은 날 연못에 비친 금각이 정말 사진 그대로예요.', 33),
  ROW(10, 'weekend_trip', '킨카쿠지', 3, '예쁘지만 관람 동선이 짧아서 금방 끝나요. 료안지와 같이 가세요.', 10),
  ROW(11, 'kyoto_lover',  '기온 하나미코지', 4, '저녁에 가면 등불이 켜져서 분위기가 좋아요. 사유지 촬영 금지 표지는 꼭 지키세요.', 30),
  ROW(12, 'foodie_park',  '니시키 시장', 5, '두부 도넛, 계란말이 꼬치, 문어 사탕까지. 점심을 여기서 해결했어요.', 28),
  ROW(13, 'demo',         '니시키 시장', 4, '오후 5시면 문 닫는 가게가 많아요. 일찍 가는 걸 추천합니다.', 2),
  ROW(14, 'foodie_park',  '도톤보리', 5, '타코야키는 줄 긴 집보다 골목 안쪽 가게가 더 맛있었어요.', 27),
  ROW(15, 'weekend_trip', '도톤보리', 4, '글리코 간판 앞은 밤에 가야 제맛. 사람은 정말 많습니다.', 9),
  ROW(16, 'photo_bear',   '도톤보리', 4, '강 쪽에서 찍으면 간판이 물에 비쳐서 예뻐요.', 19),
  ROW(17, 'weekend_trip', '오사카성', 4, '천수각 안은 박물관이라 호불호가 있지만 공원 산책은 누구나 좋아할 듯.', 8),
  ROW(18, 'foodie_park',  '구로몬 시장', 3, '맛은 좋은데 가격이 관광지 가격이에요.', 26),
  ROW(19, 'photo_bear',   '나라 공원', 5, '사슴이 정말 많아요. 센베이를 들고 있으면 순식간에 둘러싸입니다.', 18),
  ROW(20, 'kyoto_lover',  '나라 공원', 4, '교토에서 전철로 45분. 당일치기로 충분합니다.', 32),
  ROW(21, 'kyoto_lover',  '도다이지', 5, '대불전 크기에 압도돼요. 기둥 구멍 통과는 아이들에게 인기.', 31),
  ROW(22, 'weekend_trip', '센소지', 4, '나카미세 거리 군것질이 재미있어요. 밤에 가면 조용하고 조명이 예쁩니다.', 15),
  ROW(23, 'photo_bear',   '센소지', 4, '인력거 타고 한 바퀴 도는 것도 추천.', 17),
  ROW(24, 'foodie_park',  '쓰키지 장외시장', 5, '아침에 참치 덮밥 한 그릇. 이게 도쿄 여행의 시작입니다.', 24),
  ROW(25, 'weekend_trip', '시부야 스크램블 교차로', 3, '한 번은 건너볼 만해요. 위에서 보려면 스타벅스보다 시부야 스카이가 낫습니다.', 14),
  ROW(26, 'photo_bear',   '오타루 운하', 5, '겨울 저녁 가스등 켜질 때가 제일 예뻐요.', 16),
  ROW(27, 'weekend_trip', '삿포로 오도리 공원', 4, '눈축제 기간이 아니어도 텔레비전 탑 전망이 좋아요.', 11),
  ROW(28, 'foodie_park',  '나카스 포장마차 거리', 5, '돈코츠 라멘과 교자. 옆 사람과 금방 친해지는 분위기예요.', 23),
  ROW(29, 'photo_bear',   '추라우미 수족관', 5, '고래상어 수조 앞에서 한참 서 있었어요.', 13)
) AS v(seq, username, place, rating, comment, days)
JOIN users u ON u.username = v.username
JOIN destinations d ON d.name = v.place
ORDER BY v.days DESC, v.seq;

INSERT INTO review_festivals (user_id, festival_id, rating, comment, created_at)
SELECT u.id, f.id, v.rating, v.comment, NOW(6) - INTERVAL v.days DAY - INTERVAL v.seq SECOND
FROM (VALUES
  ROW(1, 'kyoto_lover',  '기온 마쓰리', 5, '야마보코 순행은 7월 17일 오전. 전날 밤 요이야마 거리도 꼭 걸어 보세요.', 37),
  ROW(2, 'photo_bear',   '기온 마쓰리', 4, '유카타 입은 사람이 많아 사진이 잘 나와요. 대신 정말 덥습니다.', 20),
  ROW(3, 'weekend_trip', '삿포로 눈축제', 5, '대형 눈 조각이 밤에 조명 받으면 장관이에요. 방한화 꼭 챙기세요.', 7),
  ROW(4, 'photo_bear',   '아오모리 네부타 마쓰리', 5, '등불 수레가 바로 앞을 지나갈 때 소름 돋았어요.', 15),
  ROW(5, 'foodie_park',  '덴진 마쓰리', 4, '강 위 배 행렬과 불꽃놀이. 노점 음식도 많아요.', 25),
  ROW(6, 'kyoto_lover',  '지다이 마쓰리', 4, '시대별 의상 행렬이 2시간 넘게 이어져요. 헤이안 신궁 근처 자리가 좋습니다.', 29)
) AS v(seq, username, place, rating, comment, days)
JOIN users u ON u.username = v.username
JOIN festivals f ON f.name = v.place
ORDER BY v.days DESC, v.seq;

-- demo 즐겨찾기 — 교토 · 오사카 여행 후보와 겨울 홋카이도
INSERT INTO favorite_destinations (user_id, destination_id, created_at)
SELECT u.id, d.id, NOW() - INTERVAL v.days DAY
FROM (VALUES
  ROW('기요미즈데라', 9), ROW('후시미 이나리 타이샤', 9), ROW('아라시야마 대나무 숲', 8),
  ROW('니시키 시장', 8), ROW('나라 공원', 7), ROW('도톤보리', 6), ROW('오사카성', 6), ROW('오타루 운하', 2)
) AS v(place, days)
JOIN users u ON u.username = 'demo'
JOIN destinations d ON d.name = v.place
ORDER BY v.days DESC;

INSERT INTO favorite_festivals (user_id, festival_id, created_at)
SELECT u.id, f.id, NOW() - INTERVAL v.days DAY
FROM (VALUES ROW('기온 마쓰리', 7), ROW('삿포로 눈축제', 2)) AS v(place, days)
JOIN users u ON u.username = 'demo'
JOIN festivals f ON f.name = v.place
ORDER BY v.days DESC;

-- demo 최근 본 장소 — mins 가 작을수록 최근
INSERT INTO history_destinations (user_id, destination_id, visited_at)
SELECT u.id, d.id, NOW(6) - INTERVAL v.mins MINUTE
FROM (VALUES
  ROW('도톤보리', 5), ROW('오사카성', 12), ROW('나라 공원', 30), ROW('니시키 시장', 55),
  ROW('기요미즈데라', 80), ROW('후시미 이나리 타이샤', 120), ROW('킨카쿠지', 1500), ROW('오타루 운하', 2900)
) AS v(place, mins)
JOIN users u ON u.username = 'demo'
JOIN destinations d ON d.name = v.place
ORDER BY v.mins DESC;

INSERT INTO history_festivals (user_id, festival_id, visited_at)
SELECT u.id, f.id, NOW(6) - INTERVAL v.mins MINUTE
FROM (VALUES ROW('기온 마쓰리', 20), ROW('삿포로 눈축제', 2950)) AS v(place, mins)
JOIN users u ON u.username = 'demo'
JOIN festivals f ON f.name = v.place
ORDER BY v.mins DESC;

-- 리뷰어들의 최근 본 장소 · 즐겨찾기 — Top25 종합 점수(D-074 · D-076)가 25곳 이상 채워지게 한다.
-- 리뷰를 쓴 곳은 본 곳이기도 하다. 그 밖에 둘러본 곳을 더한다
INSERT INTO history_destinations (user_id, destination_id, visited_at)
SELECT r.user_id, r.destination_id, MIN(r.created_at)
FROM review_destinations r
JOIN users u ON u.id = r.user_id AND u.username <> 'demo'
GROUP BY r.user_id, r.destination_id
ORDER BY MIN(r.created_at);

INSERT INTO history_destinations (user_id, destination_id, visited_at)
SELECT u.id, d.id, NOW(6) - INTERVAL v.days DAY - INTERVAL v.seq SECOND
FROM (VALUES
  ROW(1,  'weekend_trip', '도쿄 스카이트리', 12),      ROW(2,  'weekend_trip', '메이지 신궁', 12),
  ROW(3,  'weekend_trip', '우에노 공원', 11),          ROW(4,  'photo_bear',   '하코다테산 야경', 16),
  ROW(5,  'photo_bear',   '비에이 청의 호수', 15),     ROW(6,  'photo_bear',   '후라노 팜 도미타', 15),
  ROW(7,  'photo_bear',   '가마쿠라 대불', 9),         ROW(8,  'weekend_trip', '하코네 오와쿠다니', 8),
  ROW(9,  'photo_bear',   '슈리성', 13),               ROW(10, 'foodie_park',  '국제거리', 13),
  ROW(11, 'foodie_park',  '유니버설 스튜디오 재팬', 26), ROW(12, 'foodie_park',  '다자이후 텐만구', 23),
  ROW(13, 'kyoto_lover',  '이쓰쿠시마 신사', 34),      ROW(14, 'kyoto_lover',  '평화 기념 공원', 34),
  ROW(15, 'kyoto_lover',  '도쿄 스카이트리', 50),      ROW(16, 'foodie_park',  '도쿄 스카이트리', 24),
  ROW(17, 'kyoto_lover',  '기요미즈데라', 1),          ROW(18, 'foodie_park',  '도톤보리', 1)
) AS v(seq, username, place, days)
JOIN users u ON u.username = v.username
JOIN destinations d ON d.name = v.place
LEFT JOIN history_destinations h ON h.user_id = u.id AND h.destination_id = d.id
WHERE h.id IS NULL
ORDER BY v.days DESC, v.seq;

INSERT INTO favorite_destinations (user_id, destination_id, created_at)
SELECT u.id, d.id, NOW() - INTERVAL v.days DAY
FROM (VALUES
  ROW('kyoto_lover', '후시미 이나리 타이샤', 38), ROW('kyoto_lover', '킨카쿠지', 33),
  ROW('foodie_park', '니시키 시장', 28),          ROW('foodie_park', '도톤보리', 27),
  ROW('photo_bear',  '아라시야마 대나무 숲', 21), ROW('photo_bear',  '하코다테산 야경', 16),
  ROW('weekend_trip', '센소지', 15),              ROW('weekend_trip', '하코네 오와쿠다니', 8)
) AS v(username, place, days)
JOIN users u ON u.username = v.username
JOIN destinations d ON d.name = v.place
ORDER BY v.days DESC;

-- 코스 3개: demo 의 공개 · 비공개 코스, 다른 사용자의 공개 코스
INSERT INTO courses (title, description, prefecture_id, owner_user_id, is_public, created_at)
SELECT v.title, v.description, p.id, u.id, v.pub, NOW() - INTERVAL v.days DAY
FROM (VALUES
  ROW('demo', '교토 · 나라 · 오사카 3박 4일', '첫날은 히가시야마, 둘째 날은 이나리와 아라시야마, 셋째 날 나라 당일치기, 마지막 날 오사카.', '교토부', TRUE, 5),
  ROW('demo', '겨울 삿포로 · 오타루 (계획 중)', '2월 눈축제 기간에 맞춰 갈 예정. 숙소는 오도리 근처로.', '홋카이도', FALSE, 1),
  ROW('weekend_trip', '도쿄 주말 1박 2일', '금요일 밤 출발, 일요일 밤 귀국하는 짧은 일정.', '도쿄도', TRUE, 12)
) AS v(username, title, description, pref, pub, days)
JOIN users u ON u.username = v.username
JOIN prefectures p ON p.name = v.pref
ORDER BY v.days DESC;

INSERT INTO course_stops (course_id, day_no, seq, destination_id, festival_id, memo)
SELECT c.id, v.day_no, v.seq, d.id, f.id, v.memo
FROM (VALUES
  ROW('교토 · 나라 · 오사카 3박 4일', 1, 1, '기요미즈데라',         NULL,           '아침 일찍. 산넨자카로 내려오기'),
  ROW('교토 · 나라 · 오사카 3박 4일', 1, 2, '기온 하나미코지',       NULL,           NULL),
  ROW('교토 · 나라 · 오사카 3박 4일', 1, 3, '니시키 시장',           NULL,           '5시 전에 도착'),
  ROW('교토 · 나라 · 오사카 3박 4일', 2, 1, '후시미 이나리 타이샤', NULL,           '요쓰쓰지 교차로까지만'),
  ROW('교토 · 나라 · 오사카 3박 4일', 2, 2, '아라시야마 대나무 숲', NULL,           NULL),
  ROW('교토 · 나라 · 오사카 3박 4일', 2, 3, '도게쓰교',             NULL,           '해 질 녘'),
  ROW('교토 · 나라 · 오사카 3박 4일', 3, 1, '나라 공원',             NULL,           '사슴 센베이 사기'),
  ROW('교토 · 나라 · 오사카 3박 4일', 3, 2, '도다이지',             NULL,           NULL),
  ROW('교토 · 나라 · 오사카 3박 4일', 4, 1, '오사카성',             NULL,           NULL),
  ROW('교토 · 나라 · 오사카 3박 4일', 4, 2, '도톤보리',             NULL,           '저녁은 타코야키'),
  ROW('겨울 삿포로 · 오타루 (계획 중)', 1, 1, NULL,                 '삿포로 눈축제', '오도리 회장'),
  ROW('겨울 삿포로 · 오타루 (계획 중)', 1, 2, '삿포로 오도리 공원', NULL,           NULL),
  ROW('겨울 삿포로 · 오타루 (계획 중)', 2, 1, '오타루 운하',         NULL,           NULL),
  ROW('겨울 삿포로 · 오타루 (계획 중)', 2, 2, NULL,                 '오타루 눈빛 거리', '저녁 5시 점등'),
  ROW('도쿄 주말 1박 2일',              1, 1, '센소지',               NULL,           NULL),
  ROW('도쿄 주말 1박 2일',              1, 2, '도쿄 스카이트리',     NULL,           '해 질 무렵 전망대'),
  ROW('도쿄 주말 1박 2일',              2, 1, '쓰키지 장외시장',     NULL,           '아침 식사'),
  ROW('도쿄 주말 1박 2일',              2, 2, '시부야 스크램블 교차로', NULL,         NULL)
) AS v(course, day_no, seq, dest, fest, memo)
JOIN courses c ON c.title = v.course AND c.owner_user_id IS NOT NULL
LEFT JOIN destinations d ON d.name = v.dest
LEFT JOIN festivals f ON f.name = v.fest
ORDER BY c.id, v.day_no, v.seq;

-- 확인용 — 기대값: 사용자 6, 뉴스 4, 여행지 리뷰 29, 축제 리뷰 6, 즐겨찾기 18, 최근 본 53, 코스 3, 정류장 18, 점수 있는 여행지 32
SELECT
  (SELECT COUNT(*) FROM users WHERE username IN ('demo', 'kyoto_lover', 'foodie_park', 'photo_bear', 'weekend_trip', 'admin')) AS users,
  (SELECT COUNT(*) FROM news) AS news,
  (SELECT COUNT(*) FROM review_destinations) AS review_dest,
  (SELECT COUNT(*) FROM review_festivals) AS review_fest,
  (SELECT COUNT(*) FROM favorite_destinations) + (SELECT COUNT(*) FROM favorite_festivals) AS favorites,
  (SELECT COUNT(*) FROM history_destinations) + (SELECT COUNT(*) FROM history_festivals) AS history,
  (SELECT COUNT(*) FROM courses WHERE owner_user_id IS NOT NULL) AS courses,
  (SELECT COUNT(*) FROM course_stops s JOIN courses c ON c.id = s.course_id WHERE c.owner_user_id IS NOT NULL) AS stops,
  (SELECT COUNT(DISTINCT x.id) FROM destinations x WHERE EXISTS (SELECT 1 FROM history_destinations WHERE destination_id = x.id)
     OR EXISTS (SELECT 1 FROM favorite_destinations WHERE destination_id = x.id)
     OR EXISTS (SELECT 1 FROM review_destinations WHERE destination_id = x.id)) AS ranked;
