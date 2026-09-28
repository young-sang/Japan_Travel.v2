#!/usr/bin/env bash
# 응답 봉투 스모크 (D-016, D-034, D-035). 앱이 8080 에 떠 있어야 한다.
# 사용: bash scripts/smoke/error.sh
#
# 판정 기준 (docs/ERRORS.md)
#   - Content-Type 이 application/json, 본문 키는 항상 success · data · error 세 개 (이 순서, 빈 쪽은 null)
#   - 에러: 상태 코드가 ErrorCode 의 상태와 같고, 본문이 정확히
#       {"success":false,"data":null,"error":{"code":"<코드>","message":"<고정 문구>"}}  — 한글이 깨지지 않는다
#   - 성공: {"success":true,"data":<배열 또는 객체>,"error":null}
#
# 한글은 URL 에서는 퍼센트 인코딩하고, 요청 본문에는 쓰지 않는다 — Git Bash curl 에서 깨져 400 이 난다 (D-016 환경 함정).
#   없는현 = %EC%97%86%EB%8A%94%ED%98%84 · 도쿄도 = %EB%8F%84%EC%BF%84%EB%8F%84

BASE="${BASE:-http://localhost:8080}"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
pass=0
fail=0
skipped=0

# ErrorCode 의 고정 문구. enum 과 docs/ERRORS.md 가 바뀌면 여기도 바뀌어야 한다 — 이것이 계약이다.
declare -A MSG=(
  [PREFECTURE_NOT_FOUND]="존재하지 않는 현입니다"
  [DESTINATION_NOT_FOUND]="여행지를 찾을 수 없습니다"
  [FESTIVAL_NOT_FOUND]="축제를 찾을 수 없습니다"
  [INVALID_MONTH]="month 는 1~12 여야 합니다"
  [USERNAME_TAKEN]="이미 사용 중인 아이디입니다"
  [LOGIN_FAILED]="아이디 또는 비밀번호가 올바르지 않습니다"
  [UNAUTHORIZED]="로그인이 필요합니다"
  [TYPE_MISMATCH]="요청 값의 형식이 올바르지 않습니다"
  [MALFORMED_REQUEST]="요청 본문을 읽을 수 없습니다"
  [UNSUPPORTED_MEDIA_TYPE]="지원하지 않는 Content-Type 입니다"
  [API_NOT_FOUND]="존재하지 않는 API 입니다"
  [METHOD_NOT_ALLOWED]="허용되지 않는 HTTP 메서드입니다"
  [INTERNAL_ERROR]="서버 오류가 발생했습니다"
)

# req <METHOD> <PATH> [curl 인자...]  →  STATUS · CTYPE · BODY
req() {
  local method="$1" path="$2"; shift 2
  local out
  out="$(curl -s -o "$TMP" -w '%{http_code} %{content_type}' -X "$method" "$@" "$BASE$path")"
  STATUS="${out%% *}"
  CTYPE="${out#* }"
  BODY="$(cat "$TMP")"
}

ok()   { pass=$((pass + 1)); echo "  OK    $1"; }
skip() { skipped=$((skipped + 1)); echo "  SKIP  $1"; }
bad() { fail=$((fail + 1)); echo "  FAIL  $1"; echo "        status=$STATUS ctype=$CTYPE"; echo "        body=$BODY"; }

# expect_error <라벨> <상태> <코드>  — 직전 req 의 결과를 판정한다
expect_error() {
  local label="$1" status="$2" code="$3"
  local want="{\"success\":false,\"data\":null,\"error\":{\"code\":\"$code\",\"message\":\"${MSG[$code]}\"}}"
  if [[ "$STATUS" == "$status" && "$CTYPE" == application/json* && "$BODY" == "$want" ]]; then
    ok "$label → $status $code"
  else
    bad "$label → 기대 $status $want"
  fi
}

# expect_ok <라벨> <상태> <list|object> [본문에 포함돼야 할 문자열...]  — 성공 봉투를 판정한다
#   list   → {"success":true,"data":[ ... ],"error":null}
#   object → {"success":true,"data":{ ... },"error":null}
expect_ok() {
  local label="$1" status="$2" kind="$3"; shift 3
  local open='['; [[ "$kind" == object ]] && open='{'
  local head="{\"success\":true,\"data\":$open" tail=',"error":null}'
  local missing="" s
  for s in "$@"; do [[ "$BODY" == *"$s"* ]] || missing+=" $s"; done
  if [[ "$STATUS" == "$status" && "$CTYPE" == application/json* \
        && "$BODY" == "$head"* && "$BODY" == *"$tail" && -z "$missing" ]]; then
    ok "$label → $status $kind"
  else
    bad "$label → 기대 $status ${head}…${tail}${missing:+ (본문에 없음:$missing)}"
  fi
}

# first_id  — 직전 req 의 목록 봉투에서 첫 원소의 id 를 뽑는다 (DTO 의 첫 필드가 id)
first_id() {
  sed -n 's/^{"success":true,"data":\[{"id":\([0-9]*\),.*/\1/p' <<< "$BODY"
}

JSON=(-H 'Content-Type: application/json')
RUN="$$$RANDOM"            # 실행마다 겹치지 않는 아이디
USER="smoke_err_$RUN"
PASS="pw_$RUN"

echo "== 준비: 테스트 사용자 가입"
req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\",\"nickname\":\"smoke\"}"
TOKEN="$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' <<< "$BODY")"
expect_ok "S6 가입 (201)" 201 object '"token":"' "\"user\":{" "\"username\":\"$USER\""
AUTH=(-H "Authorization: Bearer $TOKEN")

echo "== 서비스가 던지는 에러"
req GET "/api/destinations?prefecture=%EC%97%86%EB%8A%94%ED%98%84"
expect_error " 1 여행지 목록, 없는 현" 404 PREFECTURE_NOT_FOUND
req GET "/api/festivals?prefecture=%EC%97%86%EB%8A%94%ED%98%84"
expect_error " 2 축제 목록, 없는 현" 404 PREFECTURE_NOT_FOUND
req GET /api/destinations/999999
expect_error " 3 여행지 상세, 없는 id" 404 DESTINATION_NOT_FOUND
req GET /api/festivals/999999
expect_error " 4 축제 상세, 없는 id" 404 FESTIVAL_NOT_FOUND
req GET "/api/festivals?month=13"
expect_error " 5 month=13" 400 INVALID_MONTH
req GET "/api/festivals?month=0"
expect_error " 5 month=0" 400 INVALID_MONTH
req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"x\",\"nickname\":\"x\"}"
expect_error " 7 가입, 아이디 중복" 409 USERNAME_TAKEN

req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"no_such_$RUN\",\"password\":\"x\"}"
expect_error " 8 로그인, 없는 아이디" 401 LOGIN_FAILED
NO_USER_BODY="$BODY"
req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"wrong\"}"
expect_error " 8 로그인, 틀린 비밀번호" 401 LOGIN_FAILED
if [[ "$BODY" == "$NO_USER_BODY" ]]; then ok " 8 두 경우의 본문이 같다"; else bad " 8 두 경우의 본문이 같아야 한다"; fi

echo "== 필터 단계 401"
req GET /api/auth/me
expect_error " 9 /me, 토큰 없음" 401 UNAUTHORIZED
req GET /api/auth/me -H 'Authorization: Bearer not.a.jwt'
expect_error " 9 /me, 가짜 토큰" 401 UNAUTHORIZED
req GET /api/auth/me -H "Authorization: $TOKEN"
expect_error " 9 /me, Bearer 접두사 없음" 401 UNAUTHORIZED
req GET /api/nope
expect_error "13 없는 경로, 토큰 없음 (Security 가 먼저)" 401 UNAUTHORIZED
req DELETE /api/destinations/1
expect_error "15 틀린 메서드, 토큰 없음 (Security 가 먼저)" 401 UNAUTHORIZED

echo "== Spring 이 던지는 에러"
req GET /api/festivals?month=abc
expect_error " 6 month=abc" 400 TYPE_MISMATCH
req GET /api/destinations/abc
expect_error " 6 /api/destinations/abc" 400 TYPE_MISMATCH
req POST /api/auth/login "${JSON[@]}" -d '{broken'
expect_error "10 로그인, 깨진 JSON" 400 MALFORMED_REQUEST
req POST /api/auth/login "${JSON[@]}"
expect_error "10 로그인, 빈 본문" 400 MALFORMED_REQUEST
req GET /api/destinations/1/nope
expect_error "11 없는 경로, 공개 경로라 토큰 없이" 404 API_NOT_FOUND
req GET /api/nope "${AUTH[@]}"
expect_error "12 없는 경로, 토큰 있음" 404 API_NOT_FOUND
req DELETE /api/destinations/1 "${AUTH[@]}"
expect_error "14 틀린 메서드, 토큰 있음" 405 METHOD_NOT_ALLOWED
req POST /api/auth/login -H 'Content-Type: text/plain' -d "{\"username\":\"$USER\",\"password\":\"$PASS\"}"
expect_error "17 로그인, Content-Type: text/plain" 415 UNSUPPORTED_MEDIA_TYPE

echo "== 그 밖의 예외"
req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"smoke_null_$RUN\",\"password\":null,\"nickname\":\"x\"}"
expect_error "16 가입, password null (서버 콘솔에 스택트레이스가 찍혀야 한다)" 500 INTERNAL_ERROR

echo "== 성공 응답 (봉투)"
req GET /api/destinations
expect_ok "S1 여행지 목록" 200 list
DEST_ID="$(first_id)"
req GET "/api/destinations?prefecture=%EB%8F%84%EC%BF%84%EB%8F%84"
expect_ok "S2 여행지 목록, 도쿄도" 200 list
# 여행지·축제는 아직 시드가 없다 (TODO "테스트 데이터 추가"). 목록이 비면 상세는 확인할 수 없으므로 건너뛴다.
if [[ -n "$DEST_ID" ]]; then
  req GET "/api/destinations/$DEST_ID"
  expect_ok "S3 여행지 상세 (id=$DEST_ID)" 200 object "\"data\":{\"id\":$DEST_ID,"
else
  skip "S3 여행지 상세 — destinations 데이터 없음"
fi
req GET "/api/festivals?month=4"
expect_ok "S4 축제 목록, month=4" 200 list
req GET /api/festivals
FEST_ID="$(first_id)"
if [[ -n "$FEST_ID" ]]; then
  req GET "/api/festivals/$FEST_ID"
  expect_ok "S5 축제 상세 (id=$FEST_ID)" 200 object "\"data\":{\"id\":$FEST_ID,"
else
  skip "S5 축제 상세 — festivals 데이터 없음"
fi
req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\"}"
expect_ok "S7 로그인" 200 object '"token":"'
req GET /api/auth/me "${AUTH[@]}"
expect_ok "S8 /me" 200 object "\"username\":\"$USER\""

echo
suffix=""
(( skipped > 0 )) && suffix=" · 건너뜀 $skipped"
if (( fail == 0 )); then
  echo "== 전부 통과 ($pass)$suffix"
else
  echo "== 실패 $fail / $((pass + fail))$suffix"
fi
exit $(( fail > 0 ))
