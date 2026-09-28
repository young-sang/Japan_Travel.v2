#!/usr/bin/env bash
# 에러 응답 스모크 (D-016, D-034). 앱이 8080 에 떠 있어야 한다.
# 사용: bash scripts/smoke/error.sh
#
# 판정 기준 (docs/ERRORS.md)
#   - 상태 코드가 ErrorCode 의 상태와 같다
#   - Content-Type 이 application/json
#   - 본문이 정확히 {"code":"<코드>","message":"<고정 문구>"} — 키는 두 개뿐, 한글이 깨지지 않는다
#
# 한글은 URL 에서는 퍼센트 인코딩하고, 요청 본문에는 쓰지 않는다 — Git Bash curl 에서 깨져 400 이 난다 (D-016 환경 함정).
#   없는현 = %EC%97%86%EB%8A%94%ED%98%84 · 도쿄도 = %EB%8F%84%EC%BF%84%EB%8F%84

BASE="${BASE:-http://localhost:8080}"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
pass=0
fail=0

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

ok()  { pass=$((pass + 1)); echo "  OK    $1"; }
bad() { fail=$((fail + 1)); echo "  FAIL  $1"; echo "        status=$STATUS ctype=$CTYPE"; echo "        body=$BODY"; }

# expect_error <라벨> <상태> <코드>  — 직전 req 의 결과를 판정한다
expect_error() {
  local label="$1" status="$2" code="$3"
  local want="{\"code\":\"$code\",\"message\":\"${MSG[$code]}\"}"
  if [[ "$STATUS" == "$status" && "$CTYPE" == application/json* && "$BODY" == "$want" ]]; then
    ok "$label → $status $code"
  else
    bad "$label → 기대 $status $want"
  fi
}

# expect_status <라벨> <상태> [본문에 포함돼야 할 문자열]  — 정상 흐름 회귀용
expect_status() {
  local label="$1" status="$2" contains="${3:-}"
  if [[ "$STATUS" == "$status" && "$BODY" == *"$contains"* ]]; then
    ok "$label → $status"
  else
    bad "$label → 기대 $status${contains:+ (본문에 $contains)}"
  fi
}

JSON=(-H 'Content-Type: application/json')
RUN="$$$RANDOM"            # 실행마다 겹치지 않는 아이디
USER="smoke_err_$RUN"
PASS="pw_$RUN"

echo "== 준비: 테스트 사용자 가입"
req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\",\"nickname\":\"smoke\"}"
TOKEN="$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' <<< "$BODY")"
expect_status "가입" 201 '"token":"'
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

echo "== 회귀: 정상 흐름은 그대로"
req GET /api/destinations
expect_status "여행지 목록" 200 '['
req GET "/api/destinations?prefecture=%EB%8F%84%EC%BF%84%EB%8F%84"
expect_status "여행지 목록, 도쿄도" 200 '['
req GET "/api/festivals?month=4"
expect_status "축제 목록, month=4" 200 '['
req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\"}"
expect_status "로그인" 200 '"token":"'
req GET /api/auth/me "${AUTH[@]}"
expect_status "/me" 200 "\"username\":\"$USER\""

echo
if (( fail == 0 )); then
  echo "== 전부 통과 ($pass)"
else
  echo "== 실패 $fail / $((pass + fail))"
fi
exit $(( fail > 0 ))
