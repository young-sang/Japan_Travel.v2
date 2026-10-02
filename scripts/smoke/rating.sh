#!/usr/bin/env bash
# 평균 별점 · 리뷰 수 스모크 (D-057). 앱이 8080 에 떠 있어야 한다.
# 사용: bash scripts/smoke/rating.sh
#
# 기존 리뷰가 있을 수 있으므로 기대값은 "시작 시점의 리뷰 목록 + 이번에 쓴 리뷰" 로 계산한다.
# 응답 JSON 은 node 로 읽는다 (목록 본문이 커서 sed 로 다루기 어렵다).

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

# js <식>  — BODY 를 파싱한 d(= data) 로 식을 평가해 출력한다
js() {
  node -e "let s='';process.stdin.on('data',c=>s+=c).on('end',()=>{const d=JSON.parse(s).data;const v=($1);console.log(v===undefined?'undefined':JSON.stringify(v))})" < "$TMP"
}

# same <라벨> <기대> <실제>
same() {
  if [[ "$2" == "$3" ]]; then
    pass=$((pass + 1)); echo "  OK    $1 ($3)"
  else
    fail=$((fail + 1)); echo "  FAIL  $1 (기대 $2, 실제 $3)"
  fi
}

# avg1 <별점 합> <개수>  — 소수 첫째 자리 반올림, 0건이면 null
avg1() { node -e "const s=$1,n=$2;console.log(n?JSON.stringify(Math.round(s/n*10)/10):'null')"; }

JSON=(-H 'Content-Type: application/json')
RUN="$$$RANDOM"

signup() {
  req POST /api/auth/signup "${JSON[@]}" -d "{\"username\":\"smoke_rate_${1}_$RUN\",\"password\":\"pw_$RUN\",\"nickname\":\"smoke\"}"
  sed -n 's/.*"token":"\([^"]*\)".*/\1/p' <<< "$BODY"
}

echo "== 준비"
curl -s -o /dev/null "$BASE/api/destinations" || { echo "  $BASE 에 연결할 수 없습니다. 백엔드를 먼저 띄우세요."; exit 1; }
TA="$(signup a)"; TB="$(signup b)"
A=(-H "Authorization: Bearer $TA"); B=(-H "Authorization: Bearer $TB")
[[ -n "$TA" && -n "$TB" ]] && { pass=$((pass + 1)); echo "  OK    계정 2개"; } || { fail=$((fail + 1)); echo "  FAIL  가입"; }

req GET /api/destinations
DID="$(js 'd[0].id')"
same "목록 요소에 avgRating 키가 있음" true "$(js "d.every(x => 'avgRating' in x)")"
same "목록 요소에 reviewCount 키가 있음" true "$(js "d.every(x => typeof x.reviewCount === 'number')")"
same "리뷰 0건이면 avgRating null" true "$(js "d.filter(x => x.reviewCount === 0).every(x => x.avgRating === null)")"
same "리뷰 있으면 avgRating 은 소수 첫째 자리" true "$(js "d.filter(x => x.reviewCount > 0).every(x => Math.round(x.avgRating*10)/10 === x.avgRating)")"

req GET "/api/destinations/$DID/reviews"
SUM0="$(js 'd.reduce((a, r) => a + r.rating, 0)')"; N0="$(js 'd.length')"
echo "  여행지 id=$DID · 시작 리뷰 $N0 건, 합 $SUM0"

echo "== 리뷰 작성 → 집계 반영"
req POST "/api/destinations/$DID/reviews" "${A[@]}" "${JSON[@]}" -d '{"rating":4,"comment":"smoke_rate"}'
RA="$(js 'd.id')"
req POST "/api/destinations/$DID/reviews" "${B[@]}" "${JSON[@]}" -d '{"rating":5,"comment":"smoke_rate"}'
RB="$(js 'd.id')"
N=$((N0 + 2)); EXP="$(avg1 "$SUM0 + 9" "$N")"

req GET "/api/destinations/$DID"
same "상세 reviewCount" "$N" "$(js 'd.reviewCount')"
same "상세 avgRating" "$EXP" "$(js 'd.avgRating')"

req GET /api/destinations
same "목록에서도 같은 값" "[$N,$EXP]" "$(js "(x => [x.reviewCount, x.avgRating])(d.find(x => x.id === $DID))")"

req POST "/api/me/favorites/destinations/$DID" "${A[@]}"
req GET /api/me/favorites "${A[@]}"
same "찜 목록에서도 같은 값" "[$N,$EXP]" "$(js "(x => [x.reviewCount, x.avgRating])(d.destinations.find(x => x.id === $DID))")"

req GET "/api/destinations/$DID"
NAME_Q="$(js 'encodeURIComponent(d.name)' | tr -d '"')"
req GET "/api/search?q=$NAME_Q"
same "검색 결과에서도 같은 값" "[$N,$EXP]" "$(js "(x => x && [x.reviewCount, x.avgRating])(d.destinations.find(x => x.id === $DID))")"

echo "== 수정 · 삭제 → 다시 반영"
req PUT "/api/destinations/$DID/reviews/$RA" "${A[@]}" "${JSON[@]}" -d '{"rating":1,"comment":"smoke_rate"}'
req GET "/api/destinations/$DID"
same "수정(4→1) 후 avgRating" "$(avg1 "$SUM0 + 6" "$N")" "$(js 'd.avgRating')"

req DELETE "/api/destinations/$DID/reviews/$RB" "${B[@]}"
req GET "/api/destinations/$DID"
same "삭제 후 reviewCount" "$((N0 + 1))" "$(js 'd.reviewCount')"
same "삭제 후 avgRating" "$(avg1 "$SUM0 + 1" "$((N0 + 1))")" "$(js 'd.avgRating')"

echo "== 축제"
req GET /api/festivals
FID="$(js 'd[0].id')"
same "축제 목록 요소에 두 키가 있음" true "$(js "d.every(x => 'avgRating' in x && typeof x.reviewCount === 'number')")"
req GET "/api/festivals/$FID/reviews"
FSUM0="$(js 'd.reduce((a, r) => a + r.rating, 0)')"; FN0="$(js 'd.length')"
req POST "/api/festivals/$FID/reviews" "${A[@]}" "${JSON[@]}" -d '{"rating":3,"comment":"smoke_rate"}'
req GET "/api/festivals/$FID"
same "축제 상세 [reviewCount, avgRating]" "[$((FN0 + 1)),$(avg1 "$FSUM0 + 3" "$((FN0 + 1))")]" "$(js '[d.reviewCount, d.avgRating]')"
req GET /api/festivals
same "축제 목록에서도 같은 값" "[$((FN0 + 1)),$(avg1 "$FSUM0 + 3" "$((FN0 + 1))")]" "$(js "(x => [x.reviewCount, x.avgRating])(d.find(x => x.id === $FID))")"

echo "== 정리 (탈퇴하면 리뷰도 사라진다, D-056)"
req DELETE /api/me "${A[@]}" "${JSON[@]}" -d "{\"password\":\"pw_$RUN\"}"
req DELETE /api/me "${B[@]}" "${JSON[@]}" -d "{\"password\":\"pw_$RUN\"}"
req GET "/api/destinations/$DID"
same "여행지 집계가 시작 값으로" "[$N0,$(avg1 "$SUM0" "$N0")]" "$(js '[d.reviewCount, d.avgRating]')"
req GET "/api/festivals/$FID"
same "축제 집계가 시작 값으로" "[$FN0,$(avg1 "$FSUM0" "$FN0")]" "$(js '[d.reviewCount, d.avgRating]')"

echo
echo "pass=$pass fail=$fail"
[[ "$fail" -eq 0 ]]
