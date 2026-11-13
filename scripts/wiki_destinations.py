"""여행지 시드를 Wikidata + ja 위키백과 원문 요약으로 만든다 (D-088 · D-089).

단계마다 결과를 파일로 남기고, 네트워크 응답은 캐시에 쌓는다. 다시 돌리면 캐시로 같은 결과가 나온다.

    python scripts/wiki_destinations.py candidates   # 1. Wikidata 후보 + 현마다 고르기 + 기존 장소 짝짓기
    python scripts/wiki_destinations.py extracts     # 2. 고른 장소의 ja 위키 원문 → scripts/wiki/extracts.json
    python scripts/wiki_destinations.py batches      # 3. 요약할 장소를 25건씩 scripts/wiki/batches/ 에 나눈다
        (서브에이전트가 scripts/wiki/summary_prompt.md 대로 scripts/wiki/summaries/ 에 이름 · 설명을 쓴다)
    python scripts/wiki_destinations.py check        # 4. 요약 검수 보고서 → scripts/wiki/summary_check.md

결과
- scripts/wiki/candidates.json : 현마다 후보 전체(유명도 순)와 이번에 고른 장소. 건수를 늘릴 때는 PER_PREF 만 바꾼다
- scripts/wiki/report.md       : 고른 장소 · 기존 장소 짝짓기 결과 (눈으로 검수할 것)
- scripts/wiki/cache.json      : SPARQL · API 응답 캐시. 지우면 처음부터 다시 조회한다
"""
import json
import math
import re
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8')

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / 'scripts/wiki'
CACHE = OUT / 'cache.json'
CANDIDATES = OUT / 'candidates.json'
REPORT = OUT / 'report.md'
DATA_SQL = ROOT / 'backend/src/main/resources/data.sql'
DEMO_SQL = ROOT / 'scripts/demo_seed.sql'
IMAGES_REPORT = ROOT / 'scripts/commons_images_report.md'
JA_NAMES = json.loads((ROOT / 'scripts/ja_names.json').read_text(encoding='utf-8'))

# Wikimedia API 정책상 연락처가 있는 User-Agent 가 필요하다 (fetch_commons_images.py 와 같다)
UA = 'JapanTravelCapstone/1.0 (student project; https://github.com/young-sang)'

# 이번에 넣는 건수 (D-089). 후보는 현마다 전부 받아 두므로 늘릴 때는 이 값만 바꿔 다시 돌린다
PER_PREF = 20
# 원문이 이보다 짧은 문서(위키 문법 포함 바이트)는 500~1,000자 요약을 만들 수 없어 고르지 않는다
MIN_BYTES = 6000
# 기존 장소와 Wikidata 항목이 같은 곳인지 — 좌표가 이 거리(km) 안이어야 짝으로 본다
MATCH_KM = 15

# 관광 유형 (D-089). P31 이 이 유형이거나 그 바로 아래 유형이면 후보로 본다
TYPES = {
    'Q845945': '신사', 'Q5393308': '절', 'Q92026': '성', 'Q23413': '성',
    'Q15835': '정원', 'Q1107656': '정원', 'Q167346': '식물원', 'Q22698': '공원', 'Q22746': '공원',
    'Q655311': '온천', 'Q177380': '온천', 'Q8502': '산', 'Q8072': '산', 'Q23397': '호수', 'Q34038': '폭포',
    'Q150784': '협곡', 'Q39816': '협곡', 'Q23442': '섬', 'Q40080': '해변', 'Q185113': '곶',
    'Q33506': '박물관', 'Q207694': '박물관', 'Q43501': '동물원', 'Q2281788': '수족관',
    'Q570116': '관광명소', 'Q1440300': '전망대', 'Q839954': '사적',
}
# 옛 거리 — 유형(P31)이 아니라 문화재 지정(P1435)으로 붙는다
PRESERVATION_DISTRICT = 'Q850649'
# 여행자가 갈 수 없는 곳 — 검수하다 찾으면 여기에 더한다 (일본어 문서 제목)
EXCLUDE = {
    '沖ノ鳥島',   # 사람이 살지 않는 산호초, 일반 방문 불가
    '沖ノ島',     # 무나카타 대사의 신역, 일반인 상륙 금지
    '硫黄島 (東京都)',   # 자위대 기지, 일반 방문 불가
    '北海道',     # 현 전체 (섬 유형으로 걸린다)
    '鎌倉市',     # 시 전체 (사적 유형으로 걸린다)
    '琉球王国のグスク及び関連遺産群',   # 유산 묶음 — 슈리성 등 개별 구스쿠가 따로 있다
    '南鳥島',     # 기상청 · 자위대 시설만 있고 일반 방문 불가
}
# 이름 보정 (ja 문서 제목 → 한국어 이름). 요약 때 정한 이름이 다른 산 · 섬과 표기가 어긋난 경우.
# 岳 은 "~다케" 로 맞추고, "다케산" · "시마섬" 처럼 겹친 표기를 걷어낸다. 운젠산 · 유후산은 굳은 이름이라 둔다
NAME_FIX = {
    '谷川岳': '다니가와다케', '剱岳': '쓰루기다케', '荒島岳': '아라시마다케', '北岳': '기타다케',
    '穂高岳': '호타카다케', '新燃岳': '신모에다케', '大久野島': '오쿠노섬',
}

PREF_JA = {
    '홋카이도': '北海道', '아오모리현': '青森県', '이와테현': '岩手県', '미야기현': '宮城県', '아키타현': '秋田県',
    '야마가타현': '山形県', '후쿠시마현': '福島県', '이바라키현': '茨城県', '도치기현': '栃木県', '군마현': '群馬県',
    '사이타마현': '埼玉県', '지바현': '千葉県', '도쿄도': '東京都', '가나가와현': '神奈川県', '니가타현': '新潟県',
    '도야마현': '富山県', '이시카와현': '石川県', '후쿠이현': '福井県', '야마나시현': '山梨県', '나가노현': '長野県',
    '기후현': '岐阜県', '시즈오카현': '静岡県', '아이치현': '愛知県', '미에현': '三重県', '시가현': '滋賀県',
    '교토부': '京都府', '오사카부': '大阪府', '효고현': '兵庫県', '나라현': '奈良県', '와카야마현': '和歌山県',
    '돗토리현': '鳥取県', '시마네현': '島根県', '오카야마현': '岡山県', '히로시마현': '広島県', '야마구치현': '山口県',
    '도쿠시마현': '徳島県', '가가와현': '香川県', '에히메현': '愛媛県', '고치현': '高知県', '후쿠오카현': '福岡県',
    '사가현': '佐賀県', '나가사키현': '長崎県', '구마모토현': '熊本県', '오이타현': '大分県', '미야자키현': '宮崎県',
    '가고시마현': '鹿児島県', '오키나와현': '沖縄県',
}

OUT.mkdir(exist_ok=True)
cache = json.loads(CACHE.read_text(encoding='utf-8')) if CACHE.exists() else {}


def save_cache():
    CACHE.write_text(json.dumps(cache, ensure_ascii=False), encoding='utf-8')


def fetch(url, keep=True):
    """keep=False 는 응답이 커서 캐시에 두지 않는 요청(원문 본문). 결과 파일이 캐시 역할을 한다."""
    if url in cache:
        return cache[url]
    req = urllib.request.Request(url, headers={'User-Agent': UA, 'Accept': 'application/json'})
    for wait in (5, 15, 30, 60, None):
        try:
            with urllib.request.urlopen(req, timeout=120) as r:
                data = json.load(r)
            break
        except urllib.error.HTTPError as e:
            if e.code not in (429, 500, 502, 503, 504) or wait is None:
                raise
            time.sleep(wait)   # 요청이 많다는 응답(429)은 잠시 뒤 다시 묻는다
    if keep:
        cache[url] = data
        save_cache()
    time.sleep(1)
    return data


def sparql(query):
    return fetch('https://query.wikidata.org/sparql?' + urllib.parse.urlencode({'query': query, 'format': 'json'}))


def api(host, keep=True, **params):
    params.update(format='json', formatversion='2')
    return fetch(f'https://{host}/w/api.php?' + urllib.parse.urlencode(params), keep)


def km(a, b):
    """두 (위도, 경도) 사이 거리. 짝짓기 확인용이라 구면 근사로 충분하다."""
    la1, lo1, la2, lo2 = map(math.radians, (*a, *b))
    h = math.sin((la2 - la1) / 2) ** 2 + math.cos(la1) * math.cos(la2) * math.sin((lo2 - lo1) / 2) ** 2
    return 6371 * 2 * math.asin(math.sqrt(h))


# ---- 1. 후보 ----

def parse_point(value):
    """'Point(경도 위도)' → (위도, 경도). 값이 '알 수 없음'(빈 노드 주소)이면 None."""
    m = re.match(r'Point\(([-\d.]+) ([-\d.]+)\)', value)
    return (float(m.group(2)), float(m.group(1))) if m else None

def prefecture_qids():
    rows = sparql("""
        SELECT ?p ?ja WHERE {
          ?p wdt:P31 wd:Q50337 ; rdfs:label ?ja . FILTER(LANG(?ja) = "ja")
        }""")['results']['bindings']
    by_ja = {r['ja']['value']: r['p']['value'].rsplit('/', 1)[1] for r in rows}
    return {ko: by_ja[ja] for ko, ja in PREF_JA.items()}


def prefecture_candidates(pref_qid):
    """현 안(P131 을 거슬러 올라가 그 현)에 있고, 좌표와 ja 위키 문서가 있는 관광 유형 항목. 유명도(언어 수) 순."""
    types = ' '.join(f'wd:{q}' for q in TYPES)
    rows = sparql(f"""
        SELECT ?item ?ja ?ko ?koLabel ?kana ?coord ?links ?img (GROUP_CONCAT(DISTINCT ?t) AS ?types) WHERE {{
          {{ VALUES ?t {{ {types} }} ?item wdt:P31/wdt:P279? ?t . }}
          UNION {{ ?item wdt:P1435 wd:{PRESERVATION_DISTRICT} . BIND(wd:{PRESERVATION_DISTRICT} AS ?t) }}
          ?item wdt:P131+ wd:{pref_qid} ; wdt:P625 ?coord ; wikibase:sitelinks ?links .
          ?jaArt schema:about ?item ; schema:isPartOf <https://ja.wikipedia.org/> ; schema:name ?ja .
          # 문을 닫거나 없어진 곳(폐쇄 · 해체일 P576). 성 · 문화재는 폐성 · 폐지일이 붙어도 유적으로 남아 있어 둔다
          FILTER NOT EXISTS {{ ?item wdt:P576 [] .
                               FILTER NOT EXISTS {{ ?item wdt:P1435 [] }}
                               FILTER NOT EXISTS {{ VALUES ?castle {{ wd:Q92026 wd:Q23413 }} ?item wdt:P31 ?castle }} }}
          OPTIONAL {{ ?koArt schema:about ?item ; schema:isPartOf <https://ko.wikipedia.org/> ; schema:name ?ko . }}
          OPTIONAL {{ ?item rdfs:label ?koLabel . FILTER(LANG(?koLabel) = "ko") }}
          OPTIONAL {{ ?item wdt:P1814 ?kana . }}
          OPTIONAL {{ ?item wdt:P18 ?img . }}
        }}
        GROUP BY ?item ?ja ?ko ?koLabel ?kana ?coord ?links ?img""")['results']['bindings']
    items = {}
    for r in rows:
        qid = r['item']['value'].rsplit('/', 1)[1]
        point = parse_point(r['coord']['value'])
        if qid in items or not point or r['ja']['value'] in EXCLUDE:
            continue   # 좌표 · 사진이 여러 개면 행이 여러 개 나온다 — 첫 행만 쓴다
        lat, lng = point
        type_ids = {t.rsplit('/', 1)[1] for t in r['types']['value'].split()}
        items[qid] = {
            'qid': qid,
            'ja': r['ja']['value'],
            'ko': r.get('ko', {}).get('value'),
            'ko_label': r.get('koLabel', {}).get('value'),
            'kana': r.get('kana', {}).get('value'),
            'lat': lat, 'lng': lng,
            'links': int(r['links']['value']),
            'image': r.get('img', {}).get('value'),
            'types': sorted({TYPES.get(t, '옛 거리') for t in type_ids}),
        }
    return sorted(items.values(), key=lambda x: (-x['links'], x['qid']))


def page_lengths(titles):
    """ja 위키 문서 길이(바이트). 50건씩 묻는다."""
    out = {}
    for i in range(0, len(titles), 50):
        chunk = titles[i:i + 50]
        pages = api('ja.wikipedia.org', action='query', prop='info', titles='|'.join(chunk))['query']['pages']
        for p in pages:
            out[p['title']] = p.get('length', 0)
    return out


# ---- 기존 장소 ----

def existing_destinations():
    """data.sql 의 여행지 (현, 이름, 좌표). 참조 여부는 기본 코스 정류장과 시연 시드에 이름이 나오는지로 본다."""
    sql = DATA_SQL.read_text(encoding='utf-8')
    dest_part = sql.split('INSERT IGNORE INTO festivals')[0]
    rows = re.findall(r"SELECT '([^']+)'(?: AS pref)?, '((?:[^']|'')+)'(?: AS name)?, '(?:[^']|'')*'(?: AS description)?, "
                      r"([\d.]+)(?: AS lat)?, ([\d.]+)", dest_part)
    stops = sql.split('INSERT IGNORE INTO course_stops')[1]
    demo = DEMO_SQL.read_text(encoding='utf-8')
    out = []
    for pref, name, lat, lng in rows:
        name = name.replace("''", "'")
        quoted = f"'{name}'"
        out.append({'pref': pref, 'name': name, 'lat': float(lat), 'lng': float(lng),
                    'referenced': quoted in stops or quoted in demo})
    return out


def image_pages():
    """사진 스크립트가 각 여행지에 짝지은 위키 문서 (commons_images_report.md). 이것으로 Wikidata 항목을 찾는다."""
    out = {}
    for line in IMAGES_REPORT.read_text(encoding='utf-8').splitlines():
        cells = [c.strip() for c in line.split('|')]
        if len(cells) > 4 and cells[1] == 'destinations':
            out[(cells[2], cells[3])] = cells[4]
    return out


def qid_of_page(host, title):
    pages = api(host, action='query', prop='pageprops', ppprop='wikibase_item', titles=title,
                redirects=1)['query']['pages']
    return pages[0].get('pageprops', {}).get('wikibase_item') if pages else None


def coords_of(qid):
    claims = api('www.wikidata.org', action='wbgetclaims', entity=qid, property='P625')['claims'].get('P625', [])
    v = claims[0]['mainsnak'].get('datavalue', {}).get('value') if claims else None
    return (v['latitude'], v['longitude']) if v else None


# 사진 매칭 때 쓴 문서가 장소 자체가 아니었던 경우 — 검수하다 찾으면 여기에 더한다 (기존 이름 → ja 문서 제목)
MATCH_OVERRIDE = {
    '아시노코': '芦ノ湖',                 # 사진 문서는 芦ノ湖遊覧船(유람선)
    '나카스 포장마차 거리': '中洲',        # 사진 문서는 이웃 동네 西中洲
}


def match_existing(dest, page):
    """기존 장소 → Wikidata 항목. 직접 정한 짝 → 사진 매칭 때 쓴 문서 → 일본어 이름표 순으로 찾고, 좌표가 가까워야 짝으로 본다."""
    tries = []
    if dest['name'] in MATCH_OVERRIDE:
        tries.append(('ja.wikipedia.org', MATCH_OVERRIDE[dest['name']]))
    elif page.startswith('wikidata:'):
        tries.append(('wikidata', page.split()[0].removeprefix('wikidata:')))
    elif page.startswith('ja:'):
        tries.append(('ja.wikipedia.org', page.removeprefix('ja:')))
    elif page and not page.startswith('commons:'):
        tries.append(('ko.wikipedia.org', page))
    names = JA_NAMES.get(dest['name']) or []
    tries += [('ja.wikipedia.org', ja) for ja in ([names] if isinstance(names, str) else names)]
    for host, key in tries:
        qid = key if host == 'wikidata' else qid_of_page(host, key)
        if not qid:
            continue
        c = coords_of(qid)
        if c and km(c, (dest['lat'], dest['lng'])) <= MATCH_KM:
            return qid, f'{host.split(".")[0]}:{key}', round(km(c, (dest['lat'], dest['lng'])), 1)
    return None, None, None


def item_details(qid):
    """후보 목록에 없는 짝(유형이 관광 유형 밖인 장소, 예: 도톤보리)의 정보를 따로 받는다."""
    rows = sparql(f"""
        SELECT ?ja ?ko ?koLabel ?kana ?coord ?links ?img WHERE {{
          BIND(wd:{qid} AS ?item)
          ?item wdt:P625 ?coord ; wikibase:sitelinks ?links .
          ?jaArt schema:about ?item ; schema:isPartOf <https://ja.wikipedia.org/> ; schema:name ?ja .
          OPTIONAL {{ ?koArt schema:about ?item ; schema:isPartOf <https://ko.wikipedia.org/> ; schema:name ?ko . }}
          OPTIONAL {{ ?item rdfs:label ?koLabel . FILTER(LANG(?koLabel) = "ko") }}
          OPTIONAL {{ ?item wdt:P1814 ?kana . }}
          OPTIONAL {{ ?item wdt:P18 ?img . }}
        }} LIMIT 1""")['results']['bindings']
    if not rows:
        return None   # ja 위키 문서가 없으면 원문이 없어 쓸 수 없다
    r = rows[0]
    if not parse_point(r['coord']['value']):
        return None
    lat, lng = parse_point(r['coord']['value'])
    return {'qid': qid, 'ja': r['ja']['value'], 'ko': r.get('ko', {}).get('value'),
            'ko_label': r.get('koLabel', {}).get('value'), 'kana': r.get('kana', {}).get('value'),
            'lat': lat, 'lng': lng, 'links': int(r['links']['value']), 'image': r.get('img', {}).get('value'),
            'types': ['기타']}


def candidates():
    prefs = prefecture_qids()
    by_pref, seen = {}, set()
#   여러 현에 걸친 항목(후지산 등)은 현 순서상 먼저 나온 현에만 둔다
    for i, (pref, qid) in enumerate(prefs.items(), 1):
        items = [x for x in prefecture_candidates(qid) if x['qid'] not in seen]
        seen.update(x['qid'] for x in items)
        by_pref[pref] = items
        print(f'[{i}/47] {pref} 후보 {len(items)}')

    pages = image_pages()
    existing = existing_destinations()
    for d in existing:
        d['qid'], d['via'], d['km'] = match_existing(d, pages.get((d['pref'], d['name']), ''))

#   현마다: 짝이 있는 참조 장소를 먼저 넣고, 유명도 순으로 원문이 충분한 문서를 PER_PREF 까지 채운다
    selected = {}
    for pref, items in by_pref.items():
        by_qid = {x['qid']: x for x in items}
        picked = []
        for d in existing:
            if d['pref'] == pref and d['referenced'] and d['qid']:
                item = by_qid.get(d['qid']) or item_details(d['qid'])
                if item and item['qid'] not in {p['qid'] for p in picked}:
                    picked.append({**item, 'keep_name': d['name']})
        head = items[:PER_PREF * 4]
        lengths = page_lengths([x['ja'] for x in head])
        for x in head:
            if len(picked) >= PER_PREF:
                break
            if x['qid'] not in {p['qid'] for p in picked} and lengths.get(x['ja'], 0) >= MIN_BYTES:
                picked.append(x)
        selected[pref] = picked

    CANDIDATES.write_text(json.dumps({'per_pref': PER_PREF, 'min_bytes': MIN_BYTES, 'selected': selected,
                                      'existing': existing, 'all': by_pref}, ensure_ascii=False, indent=1),
                          encoding='utf-8')
    write_report(selected, existing, by_pref)


def write_report(selected, existing, by_pref):
    total = sum(len(v) for v in selected.values())
    matched = [d for d in existing if d['qid']]
    ref = [d for d in existing if d['referenced']]
    ref_lost = [d for d in ref if not d['qid']]
    kept = {p['qid'] for v in selected.values() for p in v}
    lines = [
        '# 여행지 후보 (Wikidata)', '',
        f'- 후보 전체 {sum(len(v) for v in by_pref.values())}건 · 이번에 고른 장소 {total}건 (현당 {PER_PREF}건 목표, 원문 {MIN_BYTES}바이트 이상)',
        f'- 기존 여행지 {len(existing)}건 중 Wikidata 와 짝 {len(matched)}건 · 그중 이번 목록에 남는 것 {sum(d["qid"] in kept for d in matched)}건',
        f'- 기본 코스 · 시연 시드가 참조하는 기존 장소 {len(ref)}건 중 짝 없음 {len(ref_lost)}건 → 지우고 참조를 다른 장소로 고친다', '',
        '## 짝이 없는 참조 장소', '',
    ]
    lines += [f'- {d["pref"]} · {d["name"]}' for d in ref_lost] or ['- 없음']
    lines += ['', '## 기존 장소 짝짓기', '', '| 현 | 기존 이름 | 참조 | 짝 | 찾은 경로 | 거리(km) |', '|---|---|---|---|---|---|']
    lines += [f'| {d["pref"]} | {d["name"]} | {"참조" if d["referenced"] else ""} | {d["qid"] or "없음"} | {d["via"] or ""} | '
              f'{d["km"] if d["km"] is not None else ""} |' for d in existing]
    lines += ['', '## 고른 장소', '', '| 현 | ja 문서 | ko 문서 / 라벨 | 유형 | 언어 수 | 기존 이름 유지 |', '|---|---|---|---|---|---|']
    for pref, items in selected.items():
        lines += [f'| {pref} | {x["ja"]} | {x["ko"] or x["ko_label"] or ""} | {"·".join(x["types"])} | {x["links"]} | '
                  f'{x.get("keep_name", "")} |' for x in items]
    REPORT.write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print(f'\n고른 장소 {total} · 짝 없는 참조 장소 {len(ref_lost)}')


# ---- 2. 원문 ----

EXTRACTS = OUT / 'extracts.json'
# 요약에 넘기는 원문 상한(자). 길게 넘겨도 500~1,000자 요약에는 앞부분 섹션이 대부분 쓰인다
MAX_SOURCE = 8000
# 요약에 쓰지 않는 섹션 — 출처 목록 · 링크, 그리고 가는 법 · 주소는 S6 에서 따로 다룬다
SKIP_SECTION = re.compile(r'脚注|注釈|出典|参考|関連項目|外部リンク|文献|アクセス|交通|所在地|周辺|画像|ギャラリー|その他|登場する作品')


def split_sections(text):
    """'== 제목 ==' 단위로 나눈다. 첫 덩어리는 도입부."""
    parts = re.split(r'\n(={2,4})\s*(.+?)\s*\1\n', '\n' + text)
    out = [('導入', parts[0].strip())]
    for i in range(1, len(parts) - 2, 3):
        level = len(parts[i])
        out.append((parts[i + 1] if level == 2 else f'  {parts[i + 1]}', parts[i + 2].strip()))
    return out


def source_text(title):
    pages = api('ja.wikipedia.org', keep=False, action='query', prop='extracts', explaintext=1,
                exsectionformat='wiki', titles=title, redirects=1)['query']['pages']
    text = pages[0].get('extract', '') if pages else ''
    kept, skip_parent = [], False
    for heading, body in split_sections(text):
        if not heading.startswith('  '):
            skip_parent = bool(SKIP_SECTION.search(heading))
        if skip_parent or SKIP_SECTION.search(heading) or not body:
            continue
        kept.append(body if heading == '導入' else f'【{heading.strip()}】\n{body}')
    return '\n\n'.join(kept)[:MAX_SOURCE], len(text)


def extracts():
    selected = json.loads(CANDIDATES.read_text(encoding='utf-8'))['selected']
    items = [x for v in selected.values() for x in v]
    out = json.loads(EXTRACTS.read_text(encoding='utf-8')) if EXTRACTS.exists() else {}
    for i, x in enumerate(items, 1):
        if x['qid'] not in out:
            text, full = source_text(x['ja'])
            out[x['qid']] = {'ja': x['ja'], 'source': text, 'full_length': full}
        if i % 25 == 0 or i == len(items):
            EXTRACTS.write_text(json.dumps(out, ensure_ascii=False, indent=1), encoding='utf-8')
            print(f'[{i}/{len(items)}]')
    lens = sorted(len(out[x['qid']]['source']) for x in items)
    print(f'원문 {len(items)}건 · 넘기는 원문 중앙값 {lens[len(lens) // 2]}자 · 최소 {lens[0]}자')


# ---- 3. 요약 묶음 · 검수 ----

BATCHES = OUT / 'batches'
SUMMARIES = OUT / 'summaries'
BATCH_SIZE = 25


def ko_name(x):
    """한국어 이름 (D-089): 기존 이름 유지 → ko 위키 제목 → Wikidata ko 라벨. 셋 다 없으면 None — 요약 때 AI 가 음역한다."""
    if x.get('keep_name'):
        return x['keep_name']
    for v in (x.get('ko'), x.get('ko_label')):
        if v and re.search(r'[가-힣]', v):
            return re.sub(r'\s*\(.*?\)', '', v).strip()
    return None


def batches():
    """요약할 장소를 25건씩 나눠 서브에이전트에게 넘길 파일로 쓴다. 이미 요약한 장소는 빼므로 다시 돌려도 된다."""
    selected = json.loads(CANDIDATES.read_text(encoding='utf-8'))['selected']
    sources = json.loads(EXTRACTS.read_text(encoding='utf-8'))
    done = set()
    for f in SUMMARIES.glob('batch_*.json') if SUMMARIES.exists() else []:
        done.update(json.loads(f.read_text(encoding='utf-8')))
    todo = [{'qid': x['qid'], 'pref': pref, 'ja': x['ja'], 'kana': x.get('kana'), 'name': ko_name(x),
             'source': sources[x['qid']]['source']}
            for pref, items in selected.items() for x in items if x['qid'] not in done]
    BATCHES.mkdir(exist_ok=True)
    SUMMARIES.mkdir(exist_ok=True)
    start = len(list(BATCHES.glob('batch_*.json')))
    for i in range(0, len(todo), BATCH_SIZE):
        n = start + i // BATCH_SIZE + 1
        (BATCHES / f'batch_{n:02d}.json').write_text(
            json.dumps(todo[i:i + BATCH_SIZE], ensure_ascii=False, indent=1), encoding='utf-8')
    print(f'요약할 장소 {len(todo)}건 → 묶음 {math.ceil(len(todo) / BATCH_SIZE)}개 (batch_{start + 1:02d}~)')


def summaries():
    out = {}
    for f in sorted(SUMMARIES.glob('batch_*.json')):
        out.update(json.loads(f.read_text(encoding='utf-8')))
    return out


def numbers(text):
    """검수용 — 두 자리 이상 숫자. 쉼표는 떼고 비교한다 (1,000 → 1000)."""
    return set(re.findall(r'\d{2,}', text.replace(',', '')))


KANJI_DIGIT = dict(zip('〇一二三四五六七八九', range(10)))
KANJI_UNIT = {'十': 10, '百': 100, '千': 1000}


def kanji_numbers(text):
    """원문의 한자 숫자(百 · 三十三 · 千二百 · 二〇一〇)를 아라비아 숫자로 덧붙인다. 「日本百名山」→ 100 같은 경우가 많다."""
    found = []
    for m in re.finditer(r'[〇一二三四五六七八九十百千]+', text):
        s = m.group()
        if all(c in KANJI_DIGIT for c in s):   # 二〇一〇 처럼 자리마다 쓴 경우
            found.append(''.join(str(KANJI_DIGIT[c]) for c in s))
            continue
        total, cur = 0, 0
        for c in s:
            if c in KANJI_DIGIT:
                cur = KANJI_DIGIT[c]
            else:
                total += (cur or 1) * KANJI_UNIT[c]
                cur = 0
        found.append(str(total + cur))
    return ' '.join(found)


def check():
    """요약 검수 보고서. 요약의 숫자가 원문에 없으면 지어냈을 수 있어 목록에 올린다."""
    selected = json.loads(CANDIDATES.read_text(encoding='utf-8'))['selected']
    sources = json.loads(EXTRACTS.read_text(encoding='utf-8'))
    done = summaries()
    rows, missing, suspicious, lengths = [], [], [], []
    for pref, items in selected.items():
        for x in items:
            s = done.get(x['qid'])
            if not s:
                missing.append(f'{pref} · {x["ja"]}')
                continue
            raw = sources[x['qid']]['source']
            src = raw.replace(',', '') + ' ' + kanji_numbers(raw)
            extra = sorted(n for n in numbers(s['description']) if n not in src)
            lengths.append(len(s['description']))
            if extra:
                suspicious.append((pref, s['name'], x['ja'], extra))
            rows.append(f'| {pref} | {s["name"]} | [{x["ja"]}](https://ja.wikipedia.org/wiki/{urllib.parse.quote(x["ja"])}) | '
                        f'{len(sources[x["qid"]]["source"])} | {len(s["description"])} | {" ".join(extra)} |')
    lengths.sort()
    lines = ['# 여행지 요약 검수', '',
             f'- 요약 {len(rows)}건 · 빠짐 {len(missing)}건',
             f'- 설명 길이 중앙값 {lengths[len(lengths) // 2] if lengths else 0}자 · 최소 {lengths[0] if lengths else 0} · '
             f'최대 {lengths[-1] if lengths else 0} · 500자 미만 {sum(n < 500 for n in lengths)}건 · 1,000자 초과 {sum(n > 1000 for n in lengths)}건',
             f'- 원문에 없는 숫자가 있는 요약 {len(suspicious)}건 (눈으로 확인할 것)', '',
             '## 원문에 없는 숫자', '']
    lines += [f'- {p} · {n} ({ja}) — {" ".join(e)}' for p, n, ja, e in suspicious] or ['- 없음']
    lines += ['', '## 빠짐', ''] + ([f'- {m}' for m in missing] or ['- 없음'])
    lines += ['', '## 전체', '', '| 현 | 이름 | 원문 | 원문 길이 | 요약 길이 | 원문에 없는 숫자 |', '|---|---|---|---|---|---|'] + rows
    (OUT / 'summary_check.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print('\n'.join(lines[2:6]))


# ---- 4. SQL ----

DEST_SQL = ROOT / 'backend/src/main/resources/destinations.sql'


def prefecture_of(x, pref, where, source):
    """여러 현에 걸친 항목(경계의 산 등)은 원문에서 가장 먼저 나오는 현에 둔다. 후보를 받을 때는 현 순서상 먼저 나온 현에 붙어 있다."""
    prefs = where.get(x['qid'], [pref])
    if len(prefs) < 2:
        return pref
    pos = {p: source.find(PREF_JA[p]) for p in prefs if source.find(PREF_JA[p]) >= 0}
    return min(pos, key=pos.get) if pos else pref


def commons_image(url):
    """P18(Commons 파일 주소) → 사진 스크립트와 같은 라이선스 확인을 거친 썸네일 · 출처. 자유 라이선스가 아니면 None."""
    import fetch_commons_images as fc
    filename = urllib.parse.unquote(url.rsplit('/', 1)[1])
    return fc.commons_info(filename)


def build_sql():
    sys.path.insert(0, str(ROOT / 'scripts'))
    import fetch_commons_images as fc
    data = json.loads(CANDIDATES.read_text(encoding='utf-8'))
    sources = json.loads(EXTRACTS.read_text(encoding='utf-8'))
    done = summaries()
    prefs = prefecture_qids()
    where = {}
    for pref, q in prefs.items():
        for x in prefecture_candidates(q):
            where.setdefault(x['qid'], []).append(pref)

    rows = []
    for pref, items in data['selected'].items():
        for x in items:
            s = done[x['qid']]
            rows.append({**x, 'pref': prefecture_of(x, pref, where, sources[x['qid']]['source']),
                         'name': NAME_FIX.get(x['ja'], s['name'].strip()), 'description': s['description'].strip()})

#   UNIQUE(name, prefecture_id) — 같은 현에 같은 이름이 있으면 ja 문서의 괄호 구분어(시 이름 등)를 붙인다
    seen = {}
    for r in rows:
        seen.setdefault((r['pref'], r['name']), []).append(r)
    for (pref, name), same in seen.items():
        if len(same) > 1:
            for r in same:
                if not r.get('keep_name'):
                    m = re.search(r'[(（](.+?)[)）]', r['ja'])
                    r['name'] = f'{name} ({m.group(1) if m else r["ja"]})'

    for i, r in enumerate(rows, 1):
        r['img'] = commons_image(r['image']) if r.get('image') else None
        if i % 100 == 0:
            fc.CACHE.write_text(json.dumps(fc.cache, ensure_ascii=False), encoding='utf-8')
            print(f'[사진 {i}/{len(rows)}]')
    fc.CACHE.write_text(json.dumps(fc.cache, ensure_ascii=False), encoding='utf-8')

    def q(v):
        return 'NULL' if v is None else "'" + str(v).replace('\\', '\\\\').replace("'", "''") + "'"

    order = list(PREF_JA)
    rows.sort(key=lambda r: (order.index(r['pref']), -r['links'], r['qid']))
    out = [
        '-- 여행지 시드 (D-089). scripts/wiki_destinations.py sql 이 만든다 — 손으로 고치지 말 것.',
        '-- 목록 · 좌표 · 사진은 Wikidata, 설명은 ja 위키백과 원문을 한국어로 요약한 것 (CC BY-SA 4.0, 출처는 source_url).',
        '-- 기동할 때마다 실행된다. INSERT IGNORE + UNIQUE(name, prefecture_id) 로 중복을 막는다 (D-050).',
        '',
        'INSERT IGNORE INTO destinations (name, prefecture_id, description, lat, lng, image_path, image_credit, image_source,',
        '                                 wikidata_id, source_url, created_at)',
        'SELECT v.name, p.id, v.description, v.lat, v.lng, v.image_path, v.image_credit, v.image_source, v.wikidata_id, v.source_url, NOW()',
        'FROM (VALUES',
    ]
    values = []
    for r in rows:
        img = r['img'] or {}
        url = 'https://ja.wikipedia.org/wiki/' + urllib.parse.quote(r['ja'].replace(' ', '_'))
        values.append(f'  ROW({q(r["pref"])}, {q(r["name"])}, {q(r["description"])}, {r["lat"]:.6f}, {r["lng"]:.6f}, '
                      f'{q(img.get("url"))}, {q(img.get("credit"))}, {q(img.get("source"))}, {q(r["qid"])}, {q(url)})')
    out.append(',\n'.join(values))
    out += [') AS v(pref, name, description, lat, lng, image_path, image_credit, image_source, wikidata_id, source_url)',
            'JOIN prefectures p ON p.name = v.pref;', '']
    DEST_SQL.write_text('\n'.join(out), encoding='utf-8')

    per_pref = {}
    for r in rows:
        per_pref[r['pref']] = per_pref.get(r['pref'], 0) + 1
    print(f'여행지 {len(rows)}건 · 사진 {sum(1 for r in rows if r["img"])}건 · 현당 {min(per_pref.values())}~{max(per_pref.values())}건 · '
          f'이름 구분어 붙임 {sum(len(v) for v in seen.values() if len(v) > 1)}건')


if __name__ == '__main__':
    {'candidates': candidates, 'extracts': extracts, 'batches': batches, 'check': check, 'sql': build_sql}[sys.argv[1]]()
