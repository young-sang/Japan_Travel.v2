"""여행지·축제 시드에 쓸 사진을 Wikimedia Commons 에서 찾아 images.sql 을 만든다 (D-063).

data.sql 의 이름으로 한국어 위키백과를 검색해 대표 이미지를 고르고, Commons 에서 라이선스를 확인한다.
자유 라이선스(CC0 · 퍼블릭 도메인 · CC BY · CC BY-SA)만 쓰고, 출처 문구와 원본 페이지 주소를 같이 저장한다.

    python scripts/fetch_commons_images.py

결과
- backend/src/main/resources/images.sql : 기동 때 image_path 가 빈 행만 채우는 UPDATE 문
- scripts/commons_images_report.md      : 어떤 위키 문서의 어떤 사진을 골랐는지 (눈으로 검수할 것)
- scripts/commons_images_cache.json     : API 응답 캐시. 지우면 처음부터 다시 조회한다
"""
import json
import re
import sys
import time
import urllib.parse
import urllib.request
from html import unescape
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8')

ROOT = Path(__file__).resolve().parent.parent
DATA_SQL = ROOT / 'backend/src/main/resources/data.sql'
OUT_SQL = ROOT / 'backend/src/main/resources/images.sql'
REPORT = ROOT / 'scripts/commons_images_report.md'
CACHE = ROOT / 'scripts/commons_images_cache.json'

# Wikimedia API 정책상 연락처가 있는 User-Agent 가 필요하다
UA = 'JapanTravelCapstone/1.0 (student project; https://github.com/young-sang)'
THUMB_WIDTH = 800
# 대표 이미지가 사진이 아닌 경우 — 지도 · 깃발 · 문장 · 로고 · 도식(svg)
NOT_A_PHOTO = re.compile(r'(\.svg$|\.gif$|(^|[\W_])map([\W_]|$)|flag|emblem|symbol|logo|location|地図|位置図|市章|町章|村章|紋章)',
                         re.I)
FREE_LICENSE = re.compile(r'^(cc0|public domain|pd\b|cc by(-sa)? \d|cc by(-sa)?$)', re.I)

cache = json.loads(CACHE.read_text(encoding='utf-8')) if CACHE.exists() else {}


def api(host, **params):
    params.update(format='json', formatversion='2')
    url = f'https://{host}/w/api.php?' + urllib.parse.urlencode(params)
    if url in cache:
        return cache[url]
    req = urllib.request.Request(url, headers={'User-Agent': UA})
    for wait in (2, 5, 15, 30):
        with urllib.request.urlopen(req, timeout=30) as r:
            data = json.load(r)
        if 'error' not in data:
            break
        time.sleep(wait)   # 검색 서버가 바쁘다는 응답(cirrussearch-too-busy-error)은 잠시 뒤 다시 묻는다
    else:
        raise RuntimeError(f'{url} → {data["error"]}')
    cache[url] = data
    time.sleep(0.3)
    return data


def parse_seed():
    """data.sql 에서 (table, 도도부현, 이름) 을 순서대로 꺼낸다."""
    rows, table = [], None
    for line in DATA_SQL.read_text(encoding='utf-8').splitlines():
        m = re.match(r'INSERT IGNORE INTO (destinations|festivals)\b', line)
        if m:
            table = m.group(1)
            continue
        if line.startswith('INSERT INTO'):
            table = None
        m = re.match(r"\s*(?:UNION ALL )?SELECT '([^']+)'(?: AS pref)?, '((?:[^']|'')+)'", line)
        if table and m:
            rows.append((table, m.group(1), m.group(2).replace("''", "'")))
    return rows


def norm(s):
    return re.sub(r'[\s·・()（）\-]', '', s)


# 이 낱말 하나만 겹치는 문서는 장소 자체가 아니다 (예: "기시와다 단지리 마쓰리" → 「마쓰리」 문서)
GENERIC = {'마쓰리', '축제', '불꽃놀이', '공원', '신사', '절', '성', '온천', '시장', '전망대', '야경', '정원',
           '박물관', '미술관', '거리', '호수', '폭포', '섬', '산', '해변', '등대', '다리', '타워', '일본', '오도리', '칠석'}
# 시·정 같은 행정구역 문서는 도시 전경이고, 역·공항·대교는 근처의 다른 시설이라 장소 사진으로 쓰지 않는다
NOT_A_PLACE = re.compile(r'(시|현|부|구|정|촌|군|역|공항|대교)$')
# 「기노사키 (열차)」 처럼 괄호가 장소가 아닌 동음이의 문서를 가리키는 경우
NOT_A_PLACE_PAREN = re.compile(r'\((열차|철도|영화|드라마|애니메이션|노래|음반|소설|게임)\)')


def relevant(title, name):
    """찾은 문서 제목이 장소 이름과 같은 대상인지 거른다. 엉뚱한 사진보다 플레이스홀더가 낫다."""
    base = re.sub(r'\s*\(.*?\)', '', title).strip()
    t, n = norm(base), norm(name)
    if t == n:
        return True
    if base in GENERIC or NOT_A_PLACE.search(t) or NOT_A_PLACE_PAREN.search(title):
        return False
    # 한쪽이 다른 쪽을 품고, 길이가 절반 이상 겹쳐야 한다 (「돗토리시」 ⊂ 「돗토리시안가사오도리」 같은 우연을 막는다)
    return (n in t or t in n) and min(len(t), len(n)) >= 0.5 * max(len(t), len(n))


def pref_key(pref):
    """'후쿠시마현' → '후쿠시마'. 본문에 이 낱말이 나오는지로 같은 이름의 다른 곳(대한민국 철도박물관 등)을 거른다."""
    return pref if pref == '홋카이도' else re.sub(r'(도|부|현)$', '', pref)


def mentions_pref(title, pref):
    data = api('ko.wikipedia.org', action='query', prop='extracts', exintro=1, explaintext=1, titles=title,
               redirects=1)
    pages = data['query']['pages']
    return bool(pages) and pref_key(pref) in pages[0].get('extract', '')


def page_image(title, host='ko.wikipedia.org'):
    data = api(host, action='query', prop='pageimages', piprop='name', titles=title, redirects=1)
    pages = data['query']['pages']
    return pages[0].get('pageimage') if pages else None


# 일본어 경로 (D-064). 한국어 위키백과에 없는 축제·소도시 명소는 일본어 위키백과에 거의 있다.
# 이름표는 scripts/ja_names.json — 한국어 이름 → 일본어 위키백과 문서 제목에 가까운 표기.
JA_NAMES = json.loads((ROOT / 'scripts/ja_names.json').read_text(encoding='utf-8'))
PREF_JA = {
    '홋카이도': '北海道', '아오모리현': '青森', '이와테현': '岩手', '미야기현': '宮城', '아키타현': '秋田',
    '야마가타현': '山形', '후쿠시마현': '福島', '이바라키현': '茨城', '도치기현': '栃木', '군마현': '群馬',
    '사이타마현': '埼玉', '지바현': '千葉', '도쿄도': '東京', '가나가와현': '神奈川', '니가타현': '新潟',
    '도야마현': '富山', '이시카와현': '石川', '후쿠이현': '福井', '야마나시현': '山梨', '나가노현': '長野',
    '기후현': '岐阜', '시즈오카현': '静岡', '아이치현': '愛知', '미에현': '三重', '시가현': '滋賀',
    '교토부': '京都', '오사카부': '大阪', '효고현': '兵庫', '나라현': '奈良', '와카야마현': '和歌山',
    '돗토리현': '鳥取', '시마네현': '島根', '오카야마현': '岡山', '히로시마현': '広島', '야마구치현': '山口',
    '도쿠시마현': '徳島', '가가와현': '香川', '에히메현': '愛媛', '고치현': '高知', '후쿠오카현': '福岡',
    '사가현': '佐賀', '나가사키현': '長崎', '구마모토현': '熊本', '오이타현': '大分', '미야자키현': '宮崎',
    '가고시마현': '鹿児島', '오키나와현': '沖縄',
}


def ja_relevant(title, ja):
    """일본어 제목이 이름표의 표기와 같은 대상인지. 괄호 안 구분어는 비교하지 않는다."""
    t = norm(re.sub(r'\s*[(（].*?[)）]', '', title))
    n = norm(re.sub(r'\s*[(（].*?[)）]', '', ja))
    return t == n or ((n in t or t in n) and min(len(t), len(n)) >= 0.5 * max(len(t), len(n)))


def find_ja(pref, name):
    """이름표 값은 표기 하나 또는 후보 목록. 앞 후보부터 위키 문서 → Commons 검색 순으로 찾는다."""
    candidates = JA_NAMES.get(name) or []
    for ja in [candidates] if isinstance(candidates, str) else candidates:
        hit = find_ja_one(pref, ja)
        if hit:
            return hit
    return None


# 역 · 휴게소 · 시정촌 문서는 근처의 다른 시설이거나 지도 · 문장(紋章) 이미지라 쓰지 않는다.
# 이름표 표기 자체가 그 낱말로 끝나면(예: 「旧軽井沢」 아님, 「直島町」 을 일부러 적은 경우) 허용한다.
JA_NOT_A_PLACE = re.compile(r'(駅|町|市|村|区)$')


def find_ja_one(pref, ja):
    hits = api('ja.wikipedia.org', action='query', list='search', srsearch=ja, srlimit=3)['query']['search']
    for hit in hits:
        base = norm(re.sub(r'\s*[(（].*?[)）]', '', hit['title']))
        ja_base = norm(re.sub(r'\s*[(（].*?[)）]', '', ja))
        if base.startswith('道の駅') or (JA_NOT_A_PLACE.search(base) and not ja_base.endswith(base[-1])):
            continue
        if not ja_relevant(hit['title'], ja):
            continue
        data = api('ja.wikipedia.org', action='query', prop='extracts', exintro=1, explaintext=1,
                   titles=hit['title'], redirects=1)
        pages = data['query']['pages']
        if not pages or PREF_JA[pref] not in pages[0].get('extract', ''):
            continue
        filename = page_image(hit['title'], 'ja.wikipedia.org')
        info = commons_info(filename) if filename else None
        if info:
            return {'page': f'ja:{hit["title"]}', 'file': filename, **info}
    return find_commons_search(ja)


def find_commons_search(ja):
    """위키 문서에 대표 이미지가 없으면 Commons 에서 일본어 이름으로 파일을 찾는다.
    파일 제목 · 설명 · 분류에 이름이 그대로 들어 있어야 쓴다."""
    key = norm(re.sub(r'\s*[(（].*?[)）]', '', ja))
    hits = api('commons.wikimedia.org', action='query', list='search', srsearch=f'{ja} filetype:bitmap',
               srnamespace=6, srlimit=5)['query']['search']
    for hit in hits:
        filename = hit['title'].removeprefix('File:')
        data = api('commons.wikimedia.org', action='query', prop='imageinfo', titles=hit['title'],
                   iiprop='extmetadata')
        meta = data['query']['pages'][0].get('imageinfo', [{}])[0].get('extmetadata', {})
        text = filename + ''.join(meta.get(k, {}).get('value', '') for k in ('ImageDescription', 'Categories', 'ObjectName'))
        if key not in norm(text):
            continue
        info = commons_info(filename)
        if info:
            return {'page': f'commons:{ja}', 'file': filename, **info}
    return None


#   Commons 가 작가 칸에 넣는 기본 문구를 걷어낸다 (D-081).
#   "No machine-readable author provided. X assumed (based on copyright claims)." → X / "Unknown authorUnknown author" → 작자 미상
ASSUMED_AUTHOR = re.compile(r'^No machine-readable author provided\. (.+?) assumed \(based on copyright claims\)\.?$')
UNKNOWN_AUTHOR = re.compile(r'^(Unknown author)+$')


def clean_artist(artist):
    m = ASSUMED_AUTHOR.match(artist)
    if m:
        return m.group(1)
    return '작자 미상' if UNKNOWN_AUTHOR.match(artist) else artist


def commons_info(filename):
    data = api('commons.wikimedia.org', action='query', prop='imageinfo', titles=f'File:{filename}',
               iiprop='url|extmetadata', iiurlwidth=THUMB_WIDTH)
    page = data['query']['pages'][0]
    if page.get('missing') or 'imageinfo' not in page:
        return None   # 위키백과 로컬 파일(비자유 이미지 등) — Commons 에 없으면 쓰지 않는다
    if NOT_A_PHOTO.search(filename):
        return None
    info = page['imageinfo'][0]
    meta = info.get('extmetadata', {})
    license_ = meta.get('LicenseShortName', {}).get('value', '').strip()
    if meta.get('NonFree', {}).get('value') == 'true' or not FREE_LICENSE.match(license_):
        return None
    artist = unescape(re.sub(r'<[^>]+>', '', meta.get('Artist', {}).get('value', ''))).strip()
    raw = re.sub(r'\s+', ' ', artist) or '작자 미상'
    artist = clean_artist(raw)
    return {
        'url': (info.get('thumburl') or info['url']).split('?')[0],   # 뒤에 붙는 utm_ 추적 파라미터는 뗀다
        'credit': f'{artist[:200]} / {license_} / Wikimedia Commons',
        'raw_credit': f'{raw[:200]} / {license_} / Wikimedia Commons',   # 정리 전 — 이미 DB 에 들어간 값을 고칠 때 쓴다
        'source': info['descriptionurl'],
    }


def find(pref, name):
    hits = api('ko.wikipedia.org', action='query', list='search', srsearch=name, srlimit=3)['query']['search']
    for hit in hits:
        if not relevant(hit['title'], name) or not mentions_pref(hit['title'], pref):
            continue
        filename = page_image(hit['title'])
        if not filename:
            continue
        info = commons_info(filename)
        if info:
            return {'page': hit['title'], 'file': filename, **info}
    return find_wikidata(name)


def find_wikidata(name):
    """한국어 위키백과에 문서가 없으면 Wikidata 의 한국어 이름·별칭으로 찾아 대표 이미지(P18)를 쓴다.
    본문으로 도도부현을 확인할 수 없으니 이름이 정확히 같을 때만 쓴다."""
    hits = api('www.wikidata.org', action='wbsearchentities', search=name, language='ko', uselang='ko',
               type='item', limit=3)['search']
    for hit in hits:
        label = hit.get('match', {}).get('text') or hit.get('label', '')
        if norm(label) != norm(name):
            continue
        claims = api('www.wikidata.org', action='wbgetclaims', entity=hit['id'], property='P18')['claims']
        for claim in claims.get('P18', []):
            filename = claim['mainsnak'].get('datavalue', {}).get('value')
            info = commons_info(filename) if filename else None
            if info:
                return {'page': f'wikidata:{hit["id"]} {label}', 'file': filename, **info}
    return None


def q(s):
    return "'" + s.replace("\\", "\\\\").replace("'", "''") + "'"


def main():
    rows = parse_seed()
    found, missing = [], []
    for i, (table, pref, name) in enumerate(rows, 1):
        try:
            hit = find(pref, name) or find_ja(pref, name)
        finally:
            CACHE.write_text(json.dumps(cache, ensure_ascii=False), encoding='utf-8')
        (found if hit else missing).append((table, pref, name, hit))
        print(f'[{i}/{len(rows)}] {table} {name} -> {hit["page"] if hit else "없음"}')

    sql = [
        '-- 여행지·축제 사진 (D-063). scripts/fetch_commons_images.py 가 만든다 — 손으로 고치지 말 것.',
        '-- image_path 가 빈 행만 채우므로 기동할 때마다 돌아도 되고, 이미 넣은 사진을 덮어쓰지 않는다.',
        '',
    ]
    for table, pref, name, hit in found:
        sql.append(
            f'UPDATE {table} t JOIN prefectures p ON p.id = t.prefecture_id\n'
            f'   SET t.image_path = {q(hit["url"])}, t.image_credit = {q(hit["credit"])}, t.image_source = {q(hit["source"])}\n'
            f' WHERE p.name = {q(pref)} AND t.name = {q(name)} AND t.image_path IS NULL;'
        )
    # 위 UPDATE 는 빈 행만 채우므로, 출처 문구를 정리하기 전에 들어간 행은 같은 사진일 때만 출처를 고친다 (D-081)
    fixes = [(table, pref, name, hit) for table, pref, name, hit in found if hit['raw_credit'] != hit['credit']]
    if fixes:
        sql += ['', '-- 출처 문구 정리 (D-081). 같은 사진(image_source)의 정리 전 출처만 바꾼다 — 다시 돌려도 같다.']
    for table, pref, name, hit in fixes:
        sql.append(
            f'UPDATE {table} t JOIN prefectures p ON p.id = t.prefecture_id\n'
            f'   SET t.image_credit = {q(hit["credit"])}\n'
            f' WHERE p.name = {q(pref)} AND t.name = {q(name)} AND t.image_source = {q(hit["source"])}'
            f' AND t.image_credit = {q(hit["raw_credit"])};'
        )
    OUT_SQL.write_text('\n'.join(sql) + '\n', encoding='utf-8')

    lines = ['# Commons 사진 매칭 결과', '',
             f'찾음 {len(found)} · 못 찾음 {len(missing)} (전체 {len(rows)})', '',
             '엉뚱한 사진이 있으면 `commons_images_cache.json` 은 두고 스크립트의 매칭 규칙을 고치거나, '
             '해당 행을 images.sql 생성에서 빼야 한다.', '',
             '| 종류 | 도도부현 | 이름 | 위키 문서 | 파일 | 라이선스 |', '|---|---|---|---|---|---|']
    for table, pref, name, hit in found:
        lic = hit['credit'].split(' / ')[-2]
        lines.append(f'| {table} | {pref} | {name} | {hit["page"]} | [{hit["file"]}]({hit["source"]}) | {lic} |')
    lines += ['', '## 못 찾음', '']
    lines += [f'- {table} · {pref} · {name}' for table, pref, name, _ in missing]
    REPORT.write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print(f'\n찾음 {len(found)} / 못 찾음 {len(missing)}')


if __name__ == '__main__':
    main()
