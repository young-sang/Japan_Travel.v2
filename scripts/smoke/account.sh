#!/usr/bin/env bash
# 계정 관리 스모크 (D-056). 앱이 8080 에 떠 있어야 한다.
# 사용: bash scripts/smoke/account.sh

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
USER="smoke_acct_$RUN"
PASS="pw_$RUN"
NEWPASS="newpw_$RUN"

echo "== 준비"
req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\",\"nickname\":\"before\"}"
TOKEN="$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' <<< "$BODY")"
check "가입" 201 '"token":"' '"nickname":"before"'
AUTH=(-H "Authorization: Bearer $TOKEN")

echo "== 토큰 없이 → 401"
req PATCH /api/me/profile "${JSON[@]}" -d '{"nickname":"x"}'
check "닉네임 수정" 401 '"code":"UNAUTHORIZED"'
req PUT /api/me/password "${JSON[@]}" -d '{"currentPassword":"a","newPassword":"b"}'
check "비밀번호 변경" 401 '"code":"UNAUTHORIZED"'
req DELETE /api/me "${JSON[@]}" -d '{"password":"a"}'
check "탈퇴" 401 '"code":"UNAUTHORIZED"'

echo "== 닉네임 수정"
req PATCH /api/me/profile "${AUTH[@]}" "${JSON[@]}" -d '{"nickname":"after"}'
check "수정 → 바뀐 사용자" 200 "\"username\":\"$USER\"" '"nickname":"after"'
req GET /api/auth/me "${AUTH[@]}"
check "/me 에 반영" 200 '"nickname":"after"'

echo "== 비밀번호 변경"
req PUT /api/me/password "${AUTH[@]}" "${JSON[@]}" -d "{\"currentPassword\":\"wrong\",\"newPassword\":\"$NEWPASS\"}"
check "현재 비밀번호 틀림 → 400" 400 '"code":"WRONG_PASSWORD"'
req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\"}"
check "틀린 요청 뒤 옛 비밀번호 그대로 유효" 200 '"token":"'

req PUT /api/me/password "${AUTH[@]}" "${JSON[@]}" -d "{\"currentPassword\":\"$PASS\",\"newPassword\":\"$NEWPASS\"}"
check "변경" 200 '{"success":true,"data":null,"error":null}'
req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\"}"
check "옛 비밀번호 로그인 → 401" 401 '"code":"LOGIN_FAILED"'
req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$NEWPASS\"}"
check "새 비밀번호 로그인" 200 '"token":"'

echo "== 탈퇴"
req GET /api/destinations
DID="$(sed -n 's/^{"success":true,"data":\[{"id":\([0-9]*\),.*/\1/p' <<< "$BODY")"
req POST "/api/me/favorites/destinations/$DID" "${AUTH[@]}"
req POST "/api/destinations/$DID/reviews" "${AUTH[@]}" "${JSON[@]}" -d '{"rating":3,"comment":"smoke_acct"}'
check "탈퇴 전 리뷰 작성" 201
RID="$(sed -n 's/.*"data":{"id":\([0-9]*\),.*/\1/p' <<< "$BODY")"

req DELETE /api/me "${AUTH[@]}" "${JSON[@]}" -d '{"password":"wrong"}'
check "비밀번호 틀림 → 400" 400 '"code":"WRONG_PASSWORD"'
req GET /api/auth/me "${AUTH[@]}"
check "틀린 요청 뒤 계정 그대로" 200 "\"username\":\"$USER\""

req DELETE /api/me "${AUTH[@]}" "${JSON[@]}" -d "{\"password\":\"$NEWPASS\"}"
check "탈퇴" 200 '{"success":true,"data":null,"error":null}'
req GET /api/auth/me "${AUTH[@]}"
check "남은 토큰으로 /me → 401" 401 '"code":"UNAUTHORIZED"'
req POST /api/auth/login "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$NEWPASS\"}"
check "탈퇴 계정 로그인 → 401" 401 '"code":"LOGIN_FAILED"'
req GET "/api/destinations/$DID/reviews"
[[ "$BODY" != *"\"id\":$RID,"* ]] && { pass=$((pass + 1)); echo "  OK    리뷰도 함께 삭제 (CASCADE)"; } || { fail=$((fail + 1)); echo "  FAIL  리뷰 $RID 가 남아 있음"; }
req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"$USER\",\"password\":\"$PASS\",\"nickname\":\"again\"}"
check "같은 아이디로 재가입" 201 '"token":"'
TOKEN2="$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' <<< "$BODY")"
req DELETE /api/me -H "Authorization: Bearer $TOKEN2" "${JSON[@]}" -d "{\"password\":\"$PASS\"}"
check "정리: 재가입 계정 탈퇴" 200

echo
echo "pass=$pass fail=$fail"
[[ "$fail" -eq 0 ]]
