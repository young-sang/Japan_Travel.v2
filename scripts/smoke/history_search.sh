#!/usr/bin/env bash
# history · search 스모크 (D-052, D-053). 앱이 8080 에 떠 있어야 한다.
# 사용: bash scripts/smoke/history_search.sh
#
# 한글은 URL 에서 퍼센트 인코딩한다 — Git Bash curl 에서 깨진다 (D-016 환경 함정).
#   도쿄도 = %EB%8F%84%EC%BF%84%EB%8F%84

BASE="${BASE:-http://localhost:8080}"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
pass=0
fail=0

# req <METHOD> <PATH> [curl 인자...]  →  STATUS · BODY
req() {
  local method="$1" path="$2"; shift 2
  STATUS="$(curl -s -o "$TMP" -w '%{http_code}' -X "$method" "$@" "$BASE$path")"
  BODY="$(cat "$TMP")"
}

# check <라벨> <상태> [본문에 포함돼야 할 문자열...]
check() {
  local label="$1" status="$2"; shift 2
  local missing="" s
  for s in "$@"; do [[ "$BODY" == *"$s"* ]] || missing+=" $s"; done
  if [[ "$STATUS" == "$status" && -z "$missing" ]]; then
    pass=$((pass + 1)); echo "  OK    $label"
  else
    fail=$((fail + 1)); echo "  FAIL  $label (기대 $status${missing:+, 본문에 없음:$missing})"
    echo "        status=$STATUS body=${BODY:0:300}"
  fi
}

JSON=(-H 'Content-Type: application/json')
RUN="$$$RANDOM"
USER="smoke_hist_$RUN"
PASS="pw_$RUN"

echo "== 준비"
req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\",\"nickname\":\"smoke\"}"
TOKEN="$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' <<< "$BODY")"
check "가입" 201 '"token":"'
AUTH=(-H "Authorization: Bearer $TOKEN")

req GET /api/destinations
DID="$(sed -n 's/^{"success":true,"data":\[{"id":\([0-9]*\),.*/\1/p' <<< "$BODY")"
req GET /api/festivals
FID="$(sed -n 's/^{"success":true,"data":\[{"id":\([0-9]*\),.*/\1/p' <<< "$BODY")"
echo "  여행지 id=$DID · 축제 id=$FID"

echo "== history"
req GET /api/me/history
check "토큰 없이 목록 → 401" 401 '"code":"UNAUTHORIZED"'

req GET /api/me/history "${AUTH[@]}"
check "빈 목록" 200 '{"success":true,"data":[],"error":null}'

req POST "/api/me/history/destinations/$DID" "${AUTH[@]}"
check "여행지 기록" 200 '{"success":true,"data":null,"error":null}'
sleep 1
req POST "/api/me/history/festivals/$FID" "${AUTH[@]}"
check "축제 기록" 200 '"data":null'

req GET /api/me/history "${AUTH[@]}"
check "목록 2건, 최근(축제)이 먼저" 200 "\"data\":[{\"type\":\"FESTIVAL\",\"targetId\":$FID," "\"type\":\"DESTINATION\",\"targetId\":$DID," '"visitedAt":"' '"name":"'

sleep 1
req POST "/api/me/history/destinations/$DID" "${AUTH[@]}"
req GET /api/me/history "${AUTH[@]}"
check "같은 여행지 다시 기록 → 1행 유지, 맨 앞으로" 200 "\"data\":[{\"type\":\"DESTINATION\",\"targetId\":$DID,"
n="$(grep -o '"type":' <<< "$BODY" | wc -l)"
[[ "$n" -eq 2 ]] && { pass=$((pass + 1)); echo "  OK    여전히 2건"; } || { fail=$((fail + 1)); echo "  FAIL  건수 $n (기대 2)"; }

req POST /api/me/history/destinations/999999 "${AUTH[@]}"
check "없는 여행지 → 404" 404 '"code":"DESTINATION_NOT_FOUND"'
req POST /api/me/history/festivals/999999 "${AUTH[@]}"
check "없는 축제 → 404" 404 '"code":"FESTIVAL_NOT_FOUND"'
req POST /api/me/history/destinations/abc "${AUTH[@]}"
check "id 형식 오류 → 400" 400 '"code":"TYPE_MISMATCH"'

req DELETE /api/me/history "${AUTH[@]}"
check "전체 삭제" 200 '"data":null'
req GET /api/me/history "${AUTH[@]}"
check "삭제 후 빈 목록" 200 '"data":[]'

echo "== search"
req GET "/api/search?q=%EB%8F%84%EC%BF%84%EB%8F%84"
check "로그인 없이 '도쿄도' 검색" 200 '"destinations":[{' '"festivals":[' '"courses":[' '"prefecture":"도쿄도"'
n="$(grep -o '"imagePath":' <<< "$BODY" | wc -l)"
[[ "$n" -le 60 ]] && { pass=$((pass + 1)); echo "  OK    대상별 20건 상한 (총 $n)"; } || { fail=$((fail + 1)); echo "  FAIL  총 $n 건 (상한 60)"; }
[[ "$BODY" != *'"isPublic":false'* ]] && { pass=$((pass + 1)); echo "  OK    비공개 코스 없음"; } || { fail=$((fail + 1)); echo "  FAIL  비공개 코스가 섞임"; }

req GET "/api/search?q=%20%20"
check "공백 검색어 → 빈 결과" 200 '{"success":true,"data":{"destinations":[],"festivals":[],"courses":[]},"error":null}'
req GET /api/search
check "q 없음 → 빈 결과" 200 '"destinations":[]'
req GET "/api/search?q=zzqqxx_no_match"
check "결과 0건" 200 '{"destinations":[],"festivals":[],"courses":[]}'

echo
echo "pass=$pass fail=$fail"
[[ "$fail" -eq 0 ]]
