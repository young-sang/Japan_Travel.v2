# REST API 엔드포인트

Japan Travel v2 백엔드 (`com.japantravel.controller`)
세션 쿠키 기반 인증, SQLite, Spring Boot 3.3 / Java 17.

권한 범례:
- 🌐 공개 (비로그인 가능)
- 🔑 로그인 필요
- 👤 본인 검증 (작성자만)
- 🛠 관리자 (ROLE_ADMIN)

---

## 🔐 인증 — `AuthController`

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | 🌐 | 회원가입 |
| POST | `/api/auth/login` | 🌐 | 로그인 (세션 쿠키 발급) |
| POST | `/api/auth/logout` | 🔑 | 로그아웃 |
| GET | `/api/auth/me` | 🔑 | 현재 사용자 정보 |

## 🗾 콘텐츠 조회

### Destinations — `DestinationController`
| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/destinations?prefecture=&tag=` | 🌐 | 여행지 목록 (필터) |
| GET | `/api/destinations/{id}` | 🌐 | 여행지 상세 |

### Festivals — `FestivalController`
| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/festivals` | 🌐 | 축제 목록 |
| GET | `/api/festivals/{id}` | 🌐 | 축제 상세 |

### Courses — `CourseController`
| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/courses` | 🌐 | 코스 목록 |
| GET | `/api/courses/{id}` | 🌐 | 코스 상세 |
| POST | `/api/courses` | 🔑 | 코스 생성 |
| PUT | `/api/courses/{id}` | 👤 | 코스 수정 |
| DELETE | `/api/courses/{id}` | 👤 | 코스 삭제 |

### Search — `SearchController`
| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/search?q=` | 🌐 | 통합 검색 (destinations + festivals + courses) |

## 📝 자유게시판 — `PostController`

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/posts?page=&size=` | 🌐 | 게시글 목록 (PostPage: items/total/page/size) |
| GET | `/api/posts/{id}` | 🌐 | 게시글 상세 |
| POST | `/api/posts` | 🔑 | 글 작성 (body: PostCreate) |
| PUT | `/api/posts/{id}` | 👤 | 글 수정 |
| DELETE | `/api/posts/{id}` | 👤 | 글 삭제 |
| GET | `/api/posts/{id}/comments` | 🌐 | 댓글 목록 |
| POST | `/api/posts/{id}/comments` | 🔑 | 댓글 작성 (body: CommentCreate) |
| DELETE | `/api/comments/{id}` | 👤 | 댓글 삭제 |

상태코드: 200 (조회/수정), 201 (생성), 204 (삭제), 401 (비로그인), 403 (타인 글), 404 (없음).

## 💗 사용자 데이터 — `UserDataController`

### Favorites
| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/favorites` | 🔑 | 내 즐겨찾기 |
| POST | `/api/favorites` | 🔑 | 즐겨찾기 추가 |
| DELETE | `/api/favorites/{type}/{id}` | 🔑 | 즐겨찾기 제거 |

### Reviews
| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/reviews?type=&id=` | 🌐 | 대상별 리뷰 목록 |
| POST | `/api/reviews` | 🔑 | 리뷰 작성 |
| PUT | `/api/reviews/{id}` | 👤 | 리뷰 수정 |
| DELETE | `/api/reviews/{id}` | 👤 / 🛠 | 리뷰 삭제 (관리자도 가능) |

### History
| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/history` | 🔑 | 방문 기록 |
| POST | `/api/history` | 🔑 | 방문 기록 추가 |
| DELETE | `/api/history` | 🔑 | 방문 기록 전체 삭제 |

## 🌤 외부 프록시 — `ProxyController`

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/weather?lat=&lng=` | 🌐 | Open-Meteo 현재 날씨 (7일 캐시) |
| GET | `/api/fx` | 🌐 | Frankfurter 100 JPY → KRW (7일 캐시) |

---

## 🛠 관리자 — `AdminController`

모든 엔드포인트는 ROLE_ADMIN 필요.

### 게시판 모더레이션
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/admin/posts?page=&size=` | 전체 글 목록 |
| DELETE | `/api/admin/posts/{id}` | 관리자 글 삭제 (audit_log: POST_DELETE) |
| DELETE | `/api/admin/comments/{id}` | 관리자 댓글 삭제 (audit_log: POST_COMMENT_DELETE) |

### 통계 · 수집 매트릭스
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/admin/stats` | 6종 카운트 (여행지·축제·코스·즐겨찾기·리뷰·도도부현) |
| GET | `/api/admin/collection-matrix` | 47×2 도도부현×타입 보유/실패 매트릭스 |

### Wikipedia 수집기
| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/admin/collector/run` | 단일 (도도부현+타입) 수집 |
| POST | `/api/admin/collector/bulk` | 47×2 = 94건 일괄 수집 |
| GET | `/api/admin/collector/runs?limit=` | 최근 수집 로그 |
| GET | `/api/admin/collector/runs/{id}` | 단일 수집 상세 |
| POST | `/api/admin/collector/runs/{id}/retry` | 단일 수집 재시도 |
| GET | `/api/admin/collector/bulk-runs` | bulk 실행 목록 |
| GET | `/api/admin/collector/bulk-runs/{id}` | bulk 1건 상세 (children 포함) |
| POST | `/api/admin/collector/bulk-runs/{id}/retry-failed` | 실패분만 재시도 |

### 콘텐츠 CRUD
| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/admin/destinations` | 여행지 추가 |
| PUT | `/api/admin/destinations/{id}` | 여행지 수정 |
| DELETE | `/api/admin/destinations/{id}` | 여행지 삭제 |
| POST | `/api/admin/festivals` | 축제 추가 |
| PUT | `/api/admin/festivals/{id}` | 축제 수정 |
| DELETE | `/api/admin/festivals/{id}` | 축제 삭제 |

### 사용자 관리
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/admin/users` | 전체 사용자 + 사용량 통계 (즐겨찾기/리뷰/코스/히스토리 수) |
| PATCH | `/api/admin/users/{id}/role` | 역할 변경 (USER↔ADMIN) |
| DELETE | `/api/admin/users/{id}` | 계정 + 전체 사용자 데이터 삭제 |

### 감사 로그 · 캐시
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/admin/audit?userId=&action=&from=&to=&page=&size=` | 감사 로그 (필터 + 페이지네이션) |
| GET | `/api/admin/cache/stats` | Caffeine 5종 캐시 통계 (히트/미스/히트율/크기) |
| POST | `/api/admin/cache/invalidate?name=` | 캐시 무효화 (name 생략 시 전체) |

---

## DTO (`com.japantravel.dto.Dtos`)

### 자유게시판
```java
record Post(Long id, Long userId, String userName, String title, String body,
            String createdAt, String updatedAt, int commentCount)
record PostCreate(String title, String body)
record PostComment(Long id, Long postId, Long userId, String userName, String body, String createdAt)
record CommentCreate(String body)
record PostPage(List<Post> items, int total, int page, int size)
```

### 그 외 주요 record
`Destination`, `Festival`, `Course`, `Review`, `Favorite`, `HistoryEntry`, `User`, `AuditEntry`, `CollectorRun`, `BulkRun`, `Stats`, `CollectionMatrix` 등.

---

## 캐시 (`CacheConfig`)

Caffeine 인메모리, `expireAfterWrite=7d`, `maximumSize=2000`.

| 이름 | 키 | 용도 |
|---|---|---|
| `wikiSummary` | 페이지 제목 | Wikipedia 페이지 요약 (extract, 좌표, 썸네일) |
| `wikiCategoryMembers` | 카테고리명 | 카테고리 내 페이지 제목 목록 |
| `nominatimSearch` | 검색어 | 장소명 → 좌표 변환 (Nominatim 1요청/초 제한 회피) |
| `weather` | `"lat,lng"` | Open-Meteo 현재 날씨 |
| `fx` | `'jpy_krw'` | 100 JPY → KRW 환율 |

---

## 외부 API 클라이언트

| 클라이언트 | 베이스 URL | 용도 | Throttle |
|---|---|---|---|
| `WikipediaClient` | `https://ko.wikipedia.org/api/rest_v1`, `/w/api.php` | 관광지·축제 수집 | summary 600ms, action 1100ms |
| `NominatimClient` | OSM Nominatim | 좌표 백필 | 1요청/초 (이용약관) |
| `OpenMeteoClient` | open-meteo.com | 날씨 | - |
| `FrankfurterClient` | frankfurter.app | 환율 | - |

---

## 합계

**총 53개 엔드포인트** / 9개 컨트롤러

| 권한 | 개수 |
|---|---|
| 🌐 공개 | 11 |
| 🔑 로그인 | 12 |
| 👤 본인 | 6 |
| 🛠 관리자 | 24 |
