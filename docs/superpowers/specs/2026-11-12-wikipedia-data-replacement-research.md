# 여행지 · 축제 데이터 교체 사전 조사 (Wikidata + Wikipedia)

2026-11-12 · 백로그 S4c · S4d · S4e 의 근거 ([D-088](../../DECISIONS.md#d-088--ai-로-만든-여행지축제-데이터를-wikidata--wikipedia-기반으로-바꾸는-스프린트를-s4b-다음에-둔다))

지금 여행지 · 축제는 생성형 AI 가 쓴 데이터다 ([D-021](../../DECISIONS.md#d-021--데이터를-생성형-ai-로-만든다)). 사진만 Commons 에서 실제로 붙였다 (D-063).
문제는 둘이다 — **건수가 적고**(여행지 약 219 · 축제 약 84), **설명이 짧고 거짓일 수 있다**(여행지 설명 평균 37자, 최대 57자).

## 1. 외부 출처 비교

| 출처 | 규모 · 성격 | 판단 |
|---|---|---|
| **Wikidata** (SPARQL, CC0) | 일본 · 좌표 · ja 위키 문서 있음 기준: 신사 5,759 · 절 8,213 · 성 1,238 · 관광명소 579 등 약 15,800건. 좌표 · 소재지 · 사진(P18) · 다른 언어 문서 수 | **채택** — 목록 · 좌표 · 사진 |
| **Wikipedia** (ja/en, CC BY-SA) | Wikidata 와 1:1. 본문 섹션 구조가 있음 | **채택** — 설명 원문 |
| 지자체 오픈데이터 (イベント一覧 CSV) | 지자체마다 흩어져 형식이 다름 | 축제 날짜 보강용으로만 |
| OpenTripMap API (ODbL) | OSM · Wikidata 기반 | 기각 — 원본이 겹치고 ODbL 동일조건 |
| Foursquare OS Places (Apache 2.0) | 1억 건 이상 | 기각 — 가게 위주, 설명 · 사진 없음 |
| Google Places API | 가장 풍부 | 기각 — place_id 외 장기 저장 불가, 유료, 사진은 매번 API 호출. 시드 방식과 안 맞음 |
| 전국관광정보DB (일본관광진흥협회) | 약 12만 건 | 기각 — 공개 API 조건 확인 불가 |
| 국토수치정보 P12 관광자원 | 정부 데이터 | 기각 — 2014년 기준, 일본어만, 상업 이용 조건 불명 |
| 관광청 다언어 해설문 DB | 한국어 70개 지역분 | 기각 — 규모가 작고 개작 시 통지 필요 |

축제: Wikidata 에서 ja 문서가 있는 축제는 929건이지만 날짜 속성이 있는 것은 226건이다. 날짜는 원문에서 뽑아야 한다.

## 2. Wikipedia 설명 분량 (표본 실측)

유명도(다른 언어 문서 수) 순 15,783건에서 구간마다 40건 표본.

| 구간 | ja 도입부 중앙값 | en 도입부 중앙값 | ja 문서 전체 (어림) |
|---|---|---|---|
| 상위 300 | 140자 | 476자 (40/40 있음) | 약 7,800자 |
| 300~2000위 | 80자 | 298자 (22/40 있음) | 약 2,500자 |
| 2000위 아래 | 66자 | 거의 없음 | 약 1,250자 |

- ja 도입부는 정의 1~3문장이라 그것만 쓰면 지금과 비슷하다. **내용은 본문 섹션에 있다.**
- 예: 후시미 이나리 타이샤 — 도입부 3문장, 본문 15,482자. 섹션 `概要 · 祭神 · 歴史 · 境内 · 摂末社 · 祭事 · 文化財 · 鳥居 · アクセス …`

## 3. 여행 사이트 소개글 길이 (같은 장소 4곳 실측)

후시미 이나리 · 오사카성 · 겐로쿠엔 · 다카치호 협곡. 서술형 본문만, 기본정보(주소 · 시간 · 요금 · 가는 법) 제외, 공백 포함.

| 사이트 | 유형 | 평균 |
|---|---|---|
| GLTJP 기사 | 가이드 기사 전체 | 7,219자 |
| GLTJP 기사 | 개요 · 역사 · 볼거리만 | 2,921자 |
| japan-guide (영어) | 장소 페이지 | 408단어 (약 2,400자) |
| JNTO 공식 한국어 | 장소 페이지 | 약 1,280자 |
| LIVE JAPAN 한국어 | 장소 페이지 | 약 1,116자 (2곳만 있음) |
| ANA 한국어 | 장소 페이지 | 약 749자 |
| GLTJP 상세보기 | 장소 페이지 | 558자 |
| 지자체 공식 (교토 · 오사카 · 가나자와 · 미야자키) | 장소 페이지 | 488자 |
| **우리 (지금)** | | **37자** |

트립닷컴(432) · 트립어드바이저(403)는 봇 차단으로 측정하지 못했다. 표본이 4곳이라 경향만 본다.

관찰
- 장소 페이지는 한국어 500~1,300자. 가이드 기사는 맛집 · 숙박까지 다뤄 위키 원문으로는 못 만든다.
- 잘 만든 곳은 **소제목으로 나눈다** (JNTO: "오랜 역사 / 등산하기 좋은 날", GLTJP: "어떤 곳일까? / 추천 시즌 / 볼거리").
- 거의 모든 사이트가 **기본정보를 본문과 따로 블록 · 표로** 둔다 → S6 과 같은 방향.

## 4. 목표 길이 (초안 — 스프린트 결정 게이트에서 확정)

원문 분량에 비례시킨다. 원문보다 길게 쓰면 AI 가 지어내게 된다.

| 구간 | 목표 | 기준 |
|---|---|---|
| 상위 300 | 1,200~2,000자, 소제목 4~6개 | JNTO 와 GLTJP 순수 소개 사이 |
| 300~2000위 | 600~1,200자, 소제목 2~4개 | LIVE JAPAN · ANA |
| 2000위 아래 | 400~600자 | 지자체 공식 · GLTJP 상세보기 |

섹션 매핑 초안: 한 줄 소개(카드용 ~100자, 도입부) · 개요(`概要`) · 역사(`歴史` · `由緒`) · 볼거리(`境内` · `見どころ` · `文化財`) · 행사(`祭事` · `年中行事`) · 가는 법(`アクセス` · `交通`, S6 과 겹침). 원문에 없는 섹션은 숨긴다.

## 5. 과정 (스크립트 → SQL, `fetch_commons_images.py` 와 같은 방식)

1. **후보** — Wikidata SPARQL: QID, ja/en/ko 제목, 좌표, 소재 도도부현, P18 사진, 문서 수.
2. **고르기** — 도도부현마다 유명도 순 N건 (교토 · 나라 쏠림 방지), 중복 합치기, 본문이 너무 짧은 문서 제외.
3. **원문** — Wikipedia API 로 도입부 + 섹션 일반 텍스트. 요청 간격 + 캐시 파일 (조사 중 429 를 맞았다).
4. **한국어 이름** — ko 위키 제목 → Wikidata ko 라벨 → 후리가나 기반 AI 표기. 이름은 `UNIQUE(name, prefecture_id)` 키이고 시연 데이터 · 기본 제공 코스가 이름으로 찾으므로 신중히.
5. **AI 요약 · 번역** — "주어진 원문 안의 내용만으로" 섹션별 한국어 요약. 결과 캐시.
6. **검수 보고서** — 원문 링크 · 요약 · 글자 수. 요약의 숫자 · 연도가 원문에 있는지 자동 대조.
7. **SQL** — `data.sql` 재생성, `images.sql` 은 P18 사진에 기존 라이선스 확인 로직 재사용.

앱 쪽: 설명 출처 컬럼(원문 주소 · Wikidata ID), 상세 화면에 "설명 출처: 위키백과(일본어) · CC BY-SA 4.0" 표기, DB 백업 후 재생성, `demo_seed.sql` · 기본 제공 코스의 장소 이름 맞추기, 수천 건에서 목록 · 지도 확인.

축제: 같은 과정 + 원문(`祭事` · 도입부)에서 개최 월 · 날짜 문구 추출. 못 찾으면 제외. 유명한 축제는 지자체 오픈데이터로 날짜 보강.

## 6. 스프린트 결정 게이트에서 정할 것

- 도도부현당 건수 (= 총 건수)
- 섹션 구성과 구간별 목표 길이 (4절 초안)
- 장소 유형 범위 (신사 · 절 · 성 · 공원 · 온천 · 산 · 관광명소 …)
- 5단계 AI 처리 수단 (Claude API 키로 스크립트 / Claude Code 세션 안에서 나눠 처리)
- 기존 AI 데이터 완전 교체 / 일부 유지
- 설명 저장 구조 (섹션 테이블 / 컬럼 / 단일 `description` 유지 후 S4d 에서 나눔)

## 출처

- Wikidata Query Service, Wikipedia API (2026-11-12 직접 조회)
- [OpenTripMap API](https://dev.opentripmap.com) · [Foursquare OS Places](https://foursquare.com/resources/blog/products/foursquare-open-source-places-a-new-foundational-dataset-for-the-geospatial-community/) · [Google Maps Platform Service Specific Terms](https://cloud.google.com/archive/maps-platform/terms/maps-service-terms-20251001)
- [전국관광정보DB 기사](https://www.travelvoice.jp/20250526-157748) · [국토수치정보 P12 사양서](https://Nlftp.Mlit.Go.Jp/ksj/gml/product_spec/KS-PS-P12-v2_2.pdf) · [관광청 다언어 해설문 DB](https://www.mlit.go.jp/tagengo-db/about.html)
- 측정한 사이트: [JNTO 한국어](https://www.japan.travel/ko/spot/1128/) · [LIVE JAPAN](https://livejapan.com/ko/in-kansai/in-pref-kyoto/in-fushimi_uji/spot-lj0010317/) · [ANA](https://www.ana.co.jp/ko/kr/japan-travel-planner/kyoto/0000002.html) · [japan-guide](https://www.japan-guide.com/e/e3915.html) · [GLTJP](https://www.gltjp.com/ko/article/item/20446/) · [kyoto.travel](https://kyoto.travel/ko/destinations/fushimi-inaritaisha-shrine/) · [OSAKA-INFO](https://osaka-info.jp/ko/spot/osaka-castle-main-keep/) · [VISIT KANAZAWA](https://visitkanazawa.jp/kr/attractions/detail_10106.html) · [미야자키 관광나비](https://www.kanko-miyazaki.jp/ko/)
