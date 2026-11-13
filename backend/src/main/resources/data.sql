-- 기본 제공 코스 시드 (D-050 · D-051)
-- 여행지는 destinations.sql (D-089), 축제는 festivals.sql (D-092) 에 있다 — 둘 다 이 파일보다 먼저 돈다.
-- 기동할 때마다 실행된다. INSERT IGNORE + UNIQUE(name, prefecture_id) 로 중복을 막는다.
-- 이미 있는 행은 바뀌지 않으므로 문구를 고쳐도 기존 DB 에는 반영되지 않는다 (D-050 따라오는 것).
-- 도도부현은 이름으로 조인한다. 이름이 schema.sql 의 prefectures 시드와 다르면 그 행은 조용히 빠진다.

-- ============================================================
-- 기본 제공 코스 (D-041 · D-051)
-- courses 에는 UNIQUE 가 없으므로 "주인 없는 같은 제목" 이 없을 때만 넣는다.
-- 기본 제공 코스의 제목이 사실상 키다 — 제목을 고치면 새 코스가 생긴다.
-- ============================================================
INSERT INTO courses (title, description, prefecture_id, image_path, owner_user_id, is_public, created_at)
SELECT v.title, v.description, p.id, NULL, NULL, TRUE, NOW()
FROM (
            SELECT '도쿄 첫 방문 2박 3일' AS title, '아사쿠사와 스카이트리, 하라주쿠·시부야·신주쿠, 쓰키지와 도쿄 타워까지 도쿄의 대표 명소를 사흘에 나눠 도는 기본 코스.' AS description, '도쿄도' AS pref
  UNION ALL SELECT '가마쿠라·에노시마 당일치기', '도쿄에서 전철로 1시간. 무사의 도시 가마쿠라의 신사와 대불을 보고 바닷가 섬 에노시마에서 해 질 녘을 맞는다.', '가나가와현'
  UNION ALL SELECT '하코네·후지 5호 1박 2일', '첫날은 하코네의 화산 계곡과 호수, 둘째 날은 가와구치코 주변에서 후지산을 여러 각도로 본다.', '가나가와현'
  UNION ALL SELECT '닛코 1박 2일', '세계유산 닛코 도쇼구를 보고 하루 묵은 뒤 오쿠닛코의 폭포와 호수로 올라간다.', '도치기현'
  UNION ALL SELECT '교토 핵심 2박 3일', '후시미 이나리와 기요미즈데라, 아라시야마와 금각사, 은각사와 철학의 길까지 교토를 동·서·남으로 나눠 도는 코스.', '교토부'
  UNION ALL SELECT '교토 기온 마쓰리 여름 코스', '7월 교토. 야마보코 수레가 도는 기온 마쓰리를 중심으로 시장과 기온 거리를 함께 즐긴다.', '교토부'
  UNION ALL SELECT '나라 당일치기', '사슴이 노니는 나라 공원을 걸으며 도다이지 대불과 가스가 타이샤를 한 번에 둘러본다.', '나라현'
  UNION ALL SELECT '오사카 먹방 1박 2일', '오사카성에서 시작해 구로몬 시장과 도톤보리, 신세카이의 쿠시카쓰까지 먹으며 걷는 코스.', '오사카부'
  UNION ALL SELECT '고베·히메지 1박 2일', '백로성 히메지성을 보고 고베의 이국적인 언덕과 항구 야경을 즐긴 뒤 아리마 온천에서 쉰다.', '효고현'
  UNION ALL SELECT '삿포로·오타루 2박 3일', '삿포로 시내와 야경, 운하 도시 오타루, 마지막 날 노보리베쓰 온천까지 이어지는 홋카이도 입문 코스.', '홋카이도'
  UNION ALL SELECT '삿포로 눈축제 겨울 코스', '2월 홋카이도. 삿포로 눈축제와 오타루 눈빛 거리를 이틀에 나눠 본다.', '홋카이도'
  UNION ALL SELECT '후라노·비에이 여름 코스', '7월 전후 라벤더 밭과 푸른 호수, 아사히야마 동물원을 묶은 홋카이도 중부 드라이브 코스.', '홋카이도'
  UNION ALL SELECT '하코다테 1박 2일', '별 모양 성곽과 100만 달러 야경, 다음 날 아침시장의 해산물 덮밥까지.', '홋카이도'
  UNION ALL SELECT '도호쿠 여름 축제 순례', '8월 초 도호쿠 3대 축제를 사흘에 몰아 본다. 아오모리 네부타, 아키타 간토, 센다이 칠석 순서.', '아오모리현'
  UNION ALL SELECT '히로사키 벚꽃과 오이라세 계류', '4월 말 히로사키 공원의 벚꽃 축제를 보고 도와다호와 오이라세 계류의 신록 속을 걷는다.', '아오모리현'
  UNION ALL SELECT '가나자와 1박 2일', '겐로쿠엔과 가나자와성, 찻집 거리를 돌고 다음 날 겐로쿠엔 옆 21세기 미술관을 둘러본다.', '이시카와현'
  UNION ALL SELECT '다카야마·시라카와고 1박 2일', '에도 시대 상가 거리 다카야마에서 묵고 합장 마을 시라카와고로 넘어간다.', '기후현'
  UNION ALL SELECT '히로시마·미야지마 1박 2일', '평화 기념 공원에서 역사를 돌아보고 다음 날 바다 위의 도리이 이쓰쿠시마 신사로 간다.', '히로시마현'
  UNION ALL SELECT '아와오도리와 나루토 소용돌이', '8월 중순 도쿠시마. 나루토 소용돌이를 보고 밤에는 아와오도리에 뛰어든 뒤 이야 계곡으로 들어간다.', '도쿠시마현'
  UNION ALL SELECT '세토우치 예술의 섬', '다카마쓰의 다이묘 정원을 보고 배로 현대미술의 섬 나오시마에 건너간다.', '가가와현'
  UNION ALL SELECT '후쿠오카 1박 2일', '하카타의 신사와 나카스 포장마차, 다음 날 다자이후 텐만구와 바로 옆 규슈 국립박물관.', '후쿠오카현'
  UNION ALL SELECT '나가사키 1박 2일', '개항기 서양 건물이 남은 언덕과 세계 3대 야경, 다음 날 군함도 상륙 투어.', '나가사키현'
  UNION ALL SELECT '벳푸·유후인 온천 1박 2일', '벳푸의 지옥 순례로 온천의 여러 얼굴을 보고 유후인의 조용한 온천 마을에서 쉰다.', '오이타현'
  UNION ALL SELECT '구마모토·아소 1박 2일', '구마모토성과 정원을 보고 아소산 화구를 거쳐 구로카와 온천에서 묵는다.', '구마모토현'
  UNION ALL SELECT '가고시마·사쿠라지마 1박 2일', '사쿠라지마를 바라보는 정원과 화산섬을 둘러보고 이부스키에서 모래찜질을 한다.', '가고시마현'
  UNION ALL SELECT '오키나와 본섬 2박 3일', '나하 시내에서 시작해 북부의 수족관과 바다, 남부의 성지까지 본섬을 한 바퀴 도는 코스.', '오키나와현'
) v
JOIN prefectures p ON p.name = v.pref
WHERE NOT EXISTS (SELECT 1 FROM courses c WHERE c.owner_user_id IS NULL AND c.title = v.title);

-- 정류장 — UNIQUE(course_id, day_no, seq) 가 있어 INSERT IGNORE 로 다시 돌아도 안전하다.
-- kind: D = 여행지, F = 축제. 장소는 (이름, 현) 으로 찾는다. 이름이 시드와 다르면 그 정류장은 조용히 빠진다 (D-051).
INSERT IGNORE INTO course_stops (course_id, day_no, seq, destination_id, festival_id, memo)
SELECT c.id, v.day_no, v.seq, d.id, f.id, v.memo
FROM (
            SELECT '도쿄 첫 방문 2박 3일' AS title, 1 AS day_no, 1 AS seq, 'D' AS kind, '도쿄도' AS pref, '센소지' AS place, '이른 아침에 가면 가미나리몬 앞이 한산하다' AS memo
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 1, 2, 'D', '도쿄도', '도쿄 스카이트리', NULL
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 1, 3, 'D', '도쿄도', '아키하바라 전자상가', NULL
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 2, 1, 'D', '도쿄도', '메이지 신궁', NULL
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 2, 2, 'D', '도쿄도', '시부야 스크램블 교차로', '하라주쿠 다케시타 거리를 지나 걸어서 이동'
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 2, 3, 'D', '도쿄도', '신주쿠 교엔', NULL
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 3, 1, 'D', '도쿄도', '쓰키지 장외시장', '아침 식사는 여기서'
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 3, 2, 'D', '도쿄도', '고쿄 히가시교엔', '월·금요일 휴원'
  UNION ALL SELECT '도쿄 첫 방문 2박 3일', 3, 3, 'D', '도쿄도', '도쿄 타워', NULL

  UNION ALL SELECT '가마쿠라·에노시마 당일치기', 1, 1, 'D', '가나가와현', '쓰루가오카 하치만구', NULL
  UNION ALL SELECT '가마쿠라·에노시마 당일치기', 1, 2, 'D', '가나가와현', '가마쿠라 대불', '에노덴을 타고 하세역에서 내린다'
  UNION ALL SELECT '가마쿠라·에노시마 당일치기', 1, 3, 'D', '가나가와현', '에노시마', '전망등대에서 석양'

  UNION ALL SELECT '하코네·후지 5호 1박 2일', 1, 1, 'D', '가나가와현', '하코네 오와쿠다니', '검은 달걀 맛보기'
  UNION ALL SELECT '하코네·후지 5호 1박 2일', 1, 2, 'D', '가나가와현', '아시노코', '해적선 유람선으로 모토하코네까지'
  UNION ALL SELECT '하코네·후지 5호 1박 2일', 2, 1, 'D', '야마나시현', '가와구치코', NULL
  UNION ALL SELECT '하코네·후지 5호 1박 2일', 2, 2, 'D', '야마나시현', '아라쿠라야마 센겐 공원', '398단 계단을 오르면 오층탑 전망대'
  UNION ALL SELECT '하코네·후지 5호 1박 2일', 2, 3, 'D', '야마나시현', '오시노 핫카이', NULL

  UNION ALL SELECT '닛코 1박 2일', 1, 1, 'D', '도치기현', '닛코 도쇼구', NULL
  UNION ALL SELECT '닛코 1박 2일', 2, 1, 'D', '도치기현', '게곤 폭포', '이로하자카 굽잇길을 버스로 오른다'
  UNION ALL SELECT '닛코 1박 2일', 2, 2, 'D', '도치기현', '주젠지호', NULL

  UNION ALL SELECT '교토 핵심 2박 3일', 1, 1, 'D', '교토부', '후시미 이나리 타이샤', '붐비기 전 오전 일찍'
  UNION ALL SELECT '교토 핵심 2박 3일', 1, 2, 'D', '교토부', '기요미즈데라', NULL
  UNION ALL SELECT '교토 핵심 2박 3일', 1, 3, 'D', '교토부', '기온', '저녁 산책'
  UNION ALL SELECT '교토 핵심 2박 3일', 2, 1, 'D', '교토부', '아라시야마', NULL
  UNION ALL SELECT '교토 핵심 2박 3일', 2, 2, 'D', '교토부', '도게쓰교', NULL
  UNION ALL SELECT '교토 핵심 2박 3일', 2, 3, 'D', '교토부', '킨카쿠지', '버스로 이동'
  UNION ALL SELECT '교토 핵심 2박 3일', 2, 4, 'D', '교토부', '료안지', '킨카쿠지에서 걸어서 20분'
  UNION ALL SELECT '교토 핵심 2박 3일', 3, 1, 'D', '교토부', '긴카쿠지', NULL
  UNION ALL SELECT '교토 핵심 2박 3일', 3, 2, 'D', '교토부', '철학의 길', NULL
  UNION ALL SELECT '교토 핵심 2박 3일', 3, 3, 'D', '교토부', '니조성', NULL
  UNION ALL SELECT '교토 핵심 2박 3일', 3, 4, 'D', '교토부', '니시키 시장', '기념품과 간식'

  UNION ALL SELECT '교토 기온 마쓰리 여름 코스', 1, 1, 'D', '교토부', '니시키 시장', NULL
  UNION ALL SELECT '교토 기온 마쓰리 여름 코스', 1, 2, 'F', '교토부', '기온 마쓰리', '17일 야마보코 순행은 오전 9시부터'
  UNION ALL SELECT '교토 기온 마쓰리 여름 코스', 1, 3, 'D', '교토부', '기온', NULL
  UNION ALL SELECT '교토 기온 마쓰리 여름 코스', 2, 1, 'D', '교토부', '후시미 이나리 타이샤', '더위를 피해 아침 일찍'
  UNION ALL SELECT '교토 기온 마쓰리 여름 코스', 2, 2, 'D', '교토부', '기요미즈데라', NULL

  UNION ALL SELECT '나라 당일치기', 1, 1, 'D', '나라현', '나라 공원', '사슴 센베이는 공원 안에서 판다'
  UNION ALL SELECT '나라 당일치기', 1, 2, 'D', '나라현', '도다이지', NULL
  UNION ALL SELECT '나라 당일치기', 1, 3, 'D', '나라현', '가스가 타이샤', NULL

  UNION ALL SELECT '오사카 먹방 1박 2일', 1, 1, 'D', '오사카부', '오사카성', NULL
  UNION ALL SELECT '오사카 먹방 1박 2일', 1, 2, 'D', '오사카부', '구로몬 시장', '점심은 시장에서'
  UNION ALL SELECT '오사카 먹방 1박 2일', 1, 3, 'D', '오사카부', '도톤보리', '다코야키와 오코노미야키'
  UNION ALL SELECT '오사카 먹방 1박 2일', 2, 1, 'D', '오사카부', '신세카이 쓰텐카쿠', '쿠시카쓰 소스는 한 번만 찍는다'
  UNION ALL SELECT '오사카 먹방 1박 2일', 2, 2, 'D', '오사카부', '가이유칸', NULL
  UNION ALL SELECT '오사카 먹방 1박 2일', 2, 3, 'D', '오사카부', '우메다 스카이 빌딩', '야경으로 마무리'

  UNION ALL SELECT '고베·히메지 1박 2일', 1, 1, 'D', '효고현', '히메지성', NULL
  UNION ALL SELECT '고베·히메지 1박 2일', 1, 2, 'D', '효고현', '고베 기타노 이진칸', NULL
  UNION ALL SELECT '고베·히메지 1박 2일', 1, 3, 'D', '효고현', '고베 하버랜드', '저녁은 고베규'
  UNION ALL SELECT '고베·히메지 1박 2일', 2, 1, 'D', '효고현', '아리마 온천', NULL

  UNION ALL SELECT '삿포로·오타루 2박 3일', 1, 1, 'D', '홋카이도', '삿포로 오도리 공원', NULL
  UNION ALL SELECT '삿포로·오타루 2박 3일', 1, 2, 'D', '홋카이도', '삿포로 시계탑', NULL
  UNION ALL SELECT '삿포로·오타루 2박 3일', 1, 3, 'D', '홋카이도', '모이와산 전망대', '저녁은 수프카레나 징기스칸'
  UNION ALL SELECT '삿포로·오타루 2박 3일', 2, 1, 'D', '홋카이도', '오타루 운하', '삿포로에서 JR 로 40분'
  UNION ALL SELECT '삿포로·오타루 2박 3일', 3, 1, 'D', '홋카이도', '노보리베쓰 지옥계곡', NULL

  UNION ALL SELECT '삿포로 눈축제 겨울 코스', 1, 1, 'F', '홋카이도', '삿포로 눈축제', '눈 조각은 밤 조명이 켜진 뒤가 더 좋다'
  UNION ALL SELECT '삿포로 눈축제 겨울 코스', 1, 2, 'D', '홋카이도', '삿포로 시계탑', NULL
  UNION ALL SELECT '삿포로 눈축제 겨울 코스', 2, 1, 'D', '홋카이도', '오타루 운하', NULL
  UNION ALL SELECT '삿포로 눈축제 겨울 코스', 2, 2, 'F', '홋카이도', '오타루 눈빛 거리', '미끄럼 방지 신발 필수'

  UNION ALL SELECT '후라노·비에이 여름 코스', 1, 1, 'D', '홋카이도', '아사히야마 동물원', NULL
  UNION ALL SELECT '후라노·비에이 여름 코스', 1, 2, 'D', '홋카이도', '비에이 청의 호수', NULL
  UNION ALL SELECT '후라노·비에이 여름 코스', 2, 1, 'D', '홋카이도', '후라노 팜 도미타', '라벤더 절정은 7월 중순'

  UNION ALL SELECT '하코다테 1박 2일', 1, 1, 'D', '홋카이도', '고료카쿠 공원', NULL
  UNION ALL SELECT '하코다테 1박 2일', 1, 2, 'D', '홋카이도', '하코다테산 야경', '로프웨이는 해 지기 30분 전에'
  UNION ALL SELECT '하코다테 1박 2일', 2, 1, 'D', '홋카이도', '하코다테 아침시장', NULL

  UNION ALL SELECT '도호쿠 여름 축제 순례', 1, 1, 'D', '아오모리현', '산나이마루야마 유적', NULL
  UNION ALL SELECT '도호쿠 여름 축제 순례', 1, 2, 'F', '아오모리현', '아오모리 네부타 마쓰리', '하네토 의상을 빌리면 행렬에 참여할 수 있다'
  UNION ALL SELECT '도호쿠 여름 축제 순례', 2, 1, 'F', '아키타현', '아키타 간토 마쓰리', NULL
  UNION ALL SELECT '도호쿠 여름 축제 순례', 3, 1, 'F', '미야기현', '센다이 칠석 축제', NULL

  UNION ALL SELECT '히로사키 벚꽃과 오이라세 계류', 1, 1, 'F', '아오모리현', '히로사키 벚꽃 축제', NULL
  UNION ALL SELECT '히로사키 벚꽃과 오이라세 계류', 1, 2, 'D', '아오모리현', '히로사키 공원', '밤벚꽃 라이트업'
  UNION ALL SELECT '히로사키 벚꽃과 오이라세 계류', 2, 1, 'D', '아오모리현', '도와다호', NULL
  UNION ALL SELECT '히로사키 벚꽃과 오이라세 계류', 2, 2, 'D', '아오모리현', '오이라세 계류', '상류 쪽으로 걸으면 폭포가 많다'

  UNION ALL SELECT '가나자와 1박 2일', 1, 1, 'D', '이시카와현', '겐로쿠엔', NULL
  UNION ALL SELECT '가나자와 1박 2일', 1, 2, 'D', '이시카와현', '가나자와성 공원', NULL
  UNION ALL SELECT '가나자와 1박 2일', 1, 3, 'D', '이시카와현', '히가시 차야 거리', '금박 아이스크림'
  UNION ALL SELECT '가나자와 1박 2일', 2, 1, 'D', '이시카와현', '가나자와 21세기 미술관', NULL

  UNION ALL SELECT '다카야마·시라카와고 1박 2일', 1, 1, 'D', '기후현', '다카야마 옛 거리', '히다규 스시'
  UNION ALL SELECT '다카야마·시라카와고 1박 2일', 2, 1, 'D', '기후현', '시라카와고', '전망대에서 마을 전체를 내려다본다'

  UNION ALL SELECT '히로시마·미야지마 1박 2일', 1, 1, 'D', '히로시마현', '평화 기념 공원', NULL
  UNION ALL SELECT '히로시마·미야지마 1박 2일', 2, 1, 'D', '히로시마현', '이쓰쿠시마 신사', '밀물 시각을 확인하고 간다'

  UNION ALL SELECT '아와오도리와 나루토 소용돌이', 1, 1, 'D', '도쿠시마현', '나루토 소용돌이', '대조일 전후 오후가 크게 생긴다'
  UNION ALL SELECT '아와오도리와 나루토 소용돌이', 1, 2, 'F', '도쿠시마현', '아와오도리', NULL
  UNION ALL SELECT '아와오도리와 나루토 소용돌이', 2, 1, 'D', '도쿠시마현', '오보케 협곡', NULL
  UNION ALL SELECT '아와오도리와 나루토 소용돌이', 2, 2, 'D', '도쿠시마현', '이야 덩굴다리', NULL

  UNION ALL SELECT '세토우치 예술의 섬', 1, 1, 'D', '가가와현', '리쓰린 공원', '사누키 우동 한 그릇'
  UNION ALL SELECT '세토우치 예술의 섬', 2, 1, 'D', '가가와현', '나오시마', '지추 미술관은 예약 필수'

  UNION ALL SELECT '후쿠오카 1박 2일', 1, 1, 'D', '후쿠오카현', '구시다 신사', NULL
  UNION ALL SELECT '후쿠오카 1박 2일', 1, 2, 'D', '후쿠오카현', '나카스 포장마차 거리', NULL
  UNION ALL SELECT '후쿠오카 1박 2일', 2, 1, 'D', '후쿠오카현', '다자이후 텐만구', NULL
  UNION ALL SELECT '후쿠오카 1박 2일', 2, 2, 'D', '후쿠오카현', '규슈 국립박물관', NULL

  UNION ALL SELECT '나가사키 1박 2일', 1, 1, 'D', '나가사키현', '글로버 정원', NULL
  UNION ALL SELECT '나가사키 1박 2일', 1, 2, 'D', '나가사키현', '오우라 천주당', NULL
  UNION ALL SELECT '나가사키 1박 2일', 1, 3, 'D', '나가사키현', '이나사야마 전망대', NULL
  UNION ALL SELECT '나가사키 1박 2일', 2, 1, 'D', '나가사키현', '군함도', '파도가 높으면 상륙이 취소된다'

  UNION ALL SELECT '벳푸·유후인 온천 1박 2일', 1, 1, 'D', '오이타현', '벳푸 지옥 순례', NULL
  UNION ALL SELECT '벳푸·유후인 온천 1박 2일', 2, 1, 'D', '오이타현', '유후인', '긴린코 물안개는 가을·겨울 이른 아침'

  UNION ALL SELECT '구마모토·아소 1박 2일', 1, 1, 'D', '구마모토현', '구마모토성', NULL
  UNION ALL SELECT '구마모토·아소 1박 2일', 1, 2, 'D', '구마모토현', '스이젠지 조주엔', NULL
  UNION ALL SELECT '구마모토·아소 1박 2일', 2, 1, 'D', '구마모토현', '아소산 나카다케 화구', '화산 가스 상태에 따라 접근이 막힐 수 있다'
  UNION ALL SELECT '구마모토·아소 1박 2일', 2, 2, 'D', '구마모토현', '구로카와 온천', NULL

  UNION ALL SELECT '가고시마·사쿠라지마 1박 2일', 1, 1, 'D', '가고시마현', '센간엔', NULL
  UNION ALL SELECT '가고시마·사쿠라지마 1박 2일', 1, 2, 'D', '가고시마현', '사쿠라지마', '페리는 24시간 운항'
  UNION ALL SELECT '가고시마·사쿠라지마 1박 2일', 2, 1, 'D', '가고시마현', '이부스키 모래찜질', NULL

  UNION ALL SELECT '오키나와 본섬 2박 3일', 1, 1, 'D', '오키나와현', '슈리성', NULL
  UNION ALL SELECT '오키나와 본섬 2박 3일', 1, 2, 'D', '오키나와현', '국제거리', NULL
  UNION ALL SELECT '오키나와 본섬 2박 3일', 2, 1, 'D', '오키나와현', '만자모', '렌터카 추천'
  UNION ALL SELECT '오키나와 본섬 2박 3일', 2, 2, 'D', '오키나와현', '추라우미 수족관', '15시 고래상어 먹이 주기'
  UNION ALL SELECT '오키나와 본섬 2박 3일', 2, 3, 'D', '오키나와현', '고우리 대교', NULL
  UNION ALL SELECT '오키나와 본섬 2박 3일', 3, 1, 'D', '오키나와현', '세이파 우타키', NULL
  UNION ALL SELECT '오키나와 본섬 2박 3일', 3, 2, 'D', '오키나와현', '아메리칸 빌리지', '석양 보고 공항으로'
) v
JOIN courses c ON c.owner_user_id IS NULL AND c.title = v.title
JOIN prefectures p ON p.name = v.pref
LEFT JOIN destinations d ON v.kind = 'D' AND d.name = v.place AND d.prefecture_id = p.id
LEFT JOIN festivals    f ON v.kind = 'F' AND f.name = v.place AND f.prefecture_id = p.id
WHERE d.id IS NOT NULL OR f.id IS NOT NULL;
