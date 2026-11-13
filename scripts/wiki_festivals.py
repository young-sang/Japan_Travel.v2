"""축제 시드를 ja 위키 현별 축제 분류 + 원문 요약으로 만든다 (D-088 · D-092).

조회 · 캐시 · 원문 함수는 wiki_destinations.py 의 것을 쓴다(캐시 scripts/wiki/cache.json 공유).

    python scripts/wiki_festivals.py candidates   # 1. 현별 분류에서 후보 + 현마다 고르기 + 기존 축제 짝짓기

결과
- scripts/wiki_fest/candidates.json : 현마다 후보 전체(유명도 순)와 이번에 고른 축제
- scripts/wiki_fest/report.md       : 고른 축제 · 기존 축제 짝짓기 결과 (눈으로 검수할 것)
"""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import wiki_destinations as wd   # noqa: E402

ROOT = wd.ROOT
OUT = ROOT / 'scripts/wiki_fest'
CANDIDATES = OUT / 'candidates.json'
REPORT = OUT / 'report.md'
DATA_SQL = ROOT / 'backend/src/main/resources/data.sql'
IMAGES_REPORT = ROOT / 'scripts/commons_images_report.md'
OUT.mkdir(exist_ok=True)

# 이번에 넣는 건수 (D-092). 후보는 현마다 전부 받아 두므로 늘릴 때는 이 값만 바꾼다
PER_PREF = 10
# 원문이 이보다 짧은 문서는 고르지 않는다. 축제 문서는 여행지보다 짧아 3,000바이트(본문 약 1,000자)로 둔다
MIN_BYTES = 3000

# 하위 분류 중 축제가 아닌 것 — 신사 · 절 · 향토 예능 · 민요 분류
SKIP_SUBCAT = re.compile(r'神社|寺院|寺|郷土芸能|民謡|神楽')
# 관광 대상 축제가 아닌 문서 — 방송국 행사 · 영화제 · 음악 공연 · 경연 · 목록 · 묶음 문서
NOISE = re.compile(r'映画祭|番組|音楽祭|ミュージック|ジャズ|アニソン|カラオケ|ブックフェス|博覧会|一覧|三大|四大|五大|氏子|コンテスト'
                   r'|選手権|マラソン|RAB|ABA|BSS|ラジオ|テレビ|ロック|JAZZ|K-POP|ROCK|IDOL|アイドル', re.I)
# 사진 매칭 때 쓴 문서가 축제 전반(장르) 문서였던 경우 — 기존 이름 → 그 축제 자체의 ja 문서
MATCH_OVERRIDE = {
    '아오모리 네부타 마쓰리': '青森ねぶた',   # 사진 문서는 ねぶた(네부타 전반)
    '아와오도리': '徳島市阿波おどり',          # 사진 문서는 阿波踊り(춤 전반)
}


# ---- 1. 후보 ----

def members(cat):
    out, cont = [], {}
    while True:
        r = wd.api('ja.wikipedia.org', action='query', list='categorymembers', cmtitle=cat, cmlimit=500,
                   cmtype='page|subcat', **cont)
        out += r['query']['categorymembers']
        if 'continue' not in r:
            return [x['title'] for x in out if x['ns'] == 0], [x['title'] for x in out if x['ns'] == 14]
        cont = {'cmcontinue': r['continue']['cmcontinue']}


def pref_pages(pref_ja):
    """현 분류 문서 + 하위 분류(한 단계) 문서. 「北海道の祭り」 가 없으면 「…の祭」 를 본다."""
    pages, subs = members(f'Category:{pref_ja}の祭り')
    if not pages and not subs:
        pages, subs = members(f'Category:{pref_ja}の祭')
    found = set(pages)
    for s in subs:
        if not SKIP_SUBCAT.search(s):
            found |= set(members(s)[0])
    return sorted(t for t in found if not NOISE.search(t))


def page_info(titles):
    """ja 문서마다 길이 · Wikidata ID · 문서 좌표. 50건씩 묻는다. 넘겨주기(redirect)는 따라간 제목으로 돌려준다."""
    out = {}
    for i in range(0, len(titles), 50):
        chunk = titles[i:i + 50]
        q = wd.api('ja.wikipedia.org', action='query', prop='info|pageprops|coordinates', ppprop='wikibase_item',
                   titles='|'.join(chunk), redirects=1, colimit=500)['query']
        for p in q['pages']:
            if p.get('missing'):
                continue
            c = (p.get('coordinates') or [None])[0]
            out[p['title']] = {'ja': p['title'], 'length': p.get('length', 0),
                               'qid': p.get('pageprops', {}).get('wikibase_item'),
                               'page_coord': (c['lat'], c['lon']) if c else None}
    return out


def wikidata_info(qids):
    """Wikidata 언어 수 · 사진 · 좌표(항목 → 개최 장소 → 소재 시정촌 순) · ko 이름 · 읽기. 100건씩 VALUES 로 묻는다."""
    out = {}
    qids = sorted(q for q in qids if q)
    for i in range(0, len(qids), 100):
        values = ' '.join(f'wd:{q}' for q in qids[i:i + 100])
        rows = wd.sparql(f"""
            SELECT ?item ?links ?img ?c1 ?c2 ?c3 ?ko ?koLabel ?kana WHERE {{
              VALUES ?item {{ {values} }}
              ?item wikibase:sitelinks ?links .
              OPTIONAL {{ ?item wdt:P18 ?img }}
              OPTIONAL {{ ?item wdt:P625 ?c1 }}
              OPTIONAL {{ ?item wdt:P276/wdt:P625 ?c2 }}
              OPTIONAL {{ ?item wdt:P131/wdt:P625 ?c3 }}
              OPTIONAL {{ ?koArt schema:about ?item ; schema:isPartOf <https://ko.wikipedia.org/> ; schema:name ?ko . }}
              OPTIONAL {{ ?item rdfs:label ?koLabel . FILTER(LANG(?koLabel) = "ko") }}
              OPTIONAL {{ ?item wdt:P1814 ?kana }}
            }}""")['results']['bindings']
        for r in rows:
            q = r['item']['value'].rsplit('/', 1)[1]
            o = out.setdefault(q, {'links': int(r['links']['value'])})
            for key, col in (('image', 'img'), ('ko', 'ko'), ('ko_label', 'koLabel'), ('kana', 'kana')):
                if col in r and not o.get(key):
                    o[key] = r[col]['value']
            for rank, col in enumerate(('c1', 'c2', 'c3')):
                p = wd.parse_point(r[col]['value']) if col in r else None
                if p and (o.get('coord_rank') is None or rank < o['coord_rank']):
                    o['coord'], o['coord_rank'] = p, rank
    return out


def municipality_coord(title, pref_ja):
    """좌표가 하나도 없을 때 — 원문 첫 문단에 나오는 시 · 정 · 촌 문서의 좌표 (D-092: 개최 시정촌 중심)."""
    pages = wd.api('ja.wikipedia.org', action='query', prop='extracts', exintro=1, explaintext=1, titles=title,
                   redirects=1)['query']['pages']
    intro = pages[0].get('extract', '') if pages else ''
#   「…県西磐井郡平泉町」「札幌市中央区」 처럼 현 · 군 뒤, 또는 문장 경계 뒤에 오는 시 · 정 · 촌 이름을 앞에서부터 본다
    found = []
    for pattern in (r'(?<=[都道府県郡])([一-龥ァ-ヶ][一-龥ぁ-んァ-ヶ]{0,4}?[市町村])',      # 「…県」「…郡」 바로 뒤
                    r'(?<![一-龥ァ-ヶ])([一-龥ァ-ヶ][一-龥ぁ-んァ-ヶ]{0,4}?[市町村])'):   # 한자 · 가타카나가 아닌 글자 뒤
        found += [(m.start(1), m.group(1)) for m in re.finditer(pattern, intro)]
    names = []
    for _, name in sorted(found):
        if name not in names:
            names.append(name)
    for name in names[:12]:
        for title in (name, pref_ja + name):
            info = next(iter(page_info([title]).values()), None)
            if info and info['page_coord']:
                return info['page_coord'], name
    return None, None


def existing_festivals():
    """data.sql 의 축제 (현, 이름). 참조 여부는 기본 코스 정류장과 시연 시드에 이름이 나오는지로 본다."""
    sql = DATA_SQL.read_text(encoding='utf-8')
    part = sql[sql.index('INSERT IGNORE INTO festivals'):sql.index('INSERT INTO courses')]
    rows = re.findall(r"SELECT '([^']+)'(?: AS pref)?, '((?:[^']|'')+)'", part)
    stops = sql.split('INSERT IGNORE INTO course_stops')[1]
    demo = (ROOT / 'scripts/demo_seed.sql').read_text(encoding='utf-8')
    out = []
    for pref, name in rows:
        name = name.replace("''", "'")
        out.append({'pref': pref, 'name': name, 'referenced': f"'{name}'" in stops or f"'{name}'" in demo})
    return out


def image_pages():
    out = {}
    for line in IMAGES_REPORT.read_text(encoding='utf-8').splitlines():
        cells = [c.strip() for c in line.split('|')]
        if len(cells) > 4 and cells[1] == 'festivals':
            out[(cells[2], cells[3])] = cells[4]
    return out


def ja_title_of(page):
    """사진 매칭 문서(ko 제목 · ja: · wikidata:) → ja 문서 제목."""
    if page.startswith('ja:'):
        return page.removeprefix('ja:')
    if page.startswith('wikidata:'):
        qid = page.split()[0].removeprefix('wikidata:')
    elif page and not page.startswith('commons:'):
        qid = wd.qid_of_page('ko.wikipedia.org', page)
    else:
        return None
    if not qid:
        return None
    links = wd.api('www.wikidata.org', action='wbgetentities', ids=qid, props='sitelinks',
                   sitefilter='jawiki')['entities'][qid].get('sitelinks', {})
    return links.get('jawiki', {}).get('title')


def match_existing(f, page):
    tries = []
    if f['name'] in MATCH_OVERRIDE:
        tries.append(MATCH_OVERRIDE[f['name']])
    t = ja_title_of(page) if page else None
    if t:
        tries.append(t)
    names = wd.JA_NAMES.get(f['name']) or []
    tries += [names] if isinstance(names, str) else names
    for t in tries:
        info = page_info([t]).get(t) or next(iter(page_info([t]).values()), None)
        if info:
            return info['ja']
    return None


def candidates():
    by_pref = {}
    for i, (pref, pref_ja) in enumerate(wd.PREF_JA.items(), 1):
        titles = pref_pages(pref_ja)
        info = page_info(titles)
        by_pref[pref] = list(info.values())
        print(f'[{i}/47] {pref} 후보 {len(info)}')

#   같은 문서가 여러 현 분류에 있으면 현 순서상 먼저 나온 현에 둔다 (축제는 드물다 — 보고서에 남긴다)
    seen = set()
    for pref in by_pref:
        by_pref[pref] = [x for x in by_pref[pref] if x['ja'] not in seen]
        seen.update(x['ja'] for x in by_pref[pref])

    pages = image_pages()
    existing = existing_festivals()
    for f in existing:
        f['ja'] = match_existing(f, pages.get((f['pref'], f['name']), ''))

#   참조 축제는 분류에 없어도 문서가 있으면 넣는다 (예: 小樽雪あかりの路)
    for f in existing:
        if f['referenced'] and f['ja'] and f['ja'] not in seen:
            extra = page_info([f['ja']]).get(f['ja'])
            if extra:
                by_pref[f['pref']].append(extra)
                seen.add(f['ja'])

    wdi = wikidata_info({x['qid'] for v in by_pref.values() for x in v})
    for v in by_pref.values():
        for x in v:
            x.update({k: val for k, val in wdi.get(x['qid'], {}).items() if k != 'coord_rank'})
            x.setdefault('links', 0)
            x['coord'] = x.get('coord') or x.get('page_coord')
        v.sort(key=lambda x: (-x['links'], -x['length'], x['ja']))

    selected = {}
    for pref, items in by_pref.items():
        keep = {f['ja']: f['name'] for f in existing if f['pref'] == pref and f['referenced'] and f['ja']}
        picked = [{**x, 'keep_name': keep[x['ja']]} for x in items if x['ja'] in keep]
        for x in items:
            if len(picked) >= PER_PREF:
                break
            if x['ja'] not in keep and x['length'] >= MIN_BYTES:
                picked.append(dict(x))
        for x in picked:
            if not x['coord']:
                x['coord'], x['coord_from'] = municipality_coord(x['ja'], wd.PREF_JA[pref])
        selected[pref] = picked

    CANDIDATES.write_text(json.dumps({'per_pref': PER_PREF, 'min_bytes': MIN_BYTES, 'selected': selected,
                                      'existing': existing, 'all': by_pref}, ensure_ascii=False, indent=1),
                          encoding='utf-8')
    write_report(selected, existing, by_pref)


def write_report(selected, existing, by_pref):
    total = sum(len(v) for v in selected.values())
    ref = [f for f in existing if f['referenced']]
    kept = {x['ja'] for v in selected.values() for x in v}
    lines = [
        '# 축제 후보 (ja 위키 현별 분류)', '',
        f'- 후보 전체 {sum(len(v) for v in by_pref.values())}건 · 고른 축제 {total}건 (현당 최대 {PER_PREF}건, 원문 {MIN_BYTES}바이트 이상)',
        f'- 기존 축제 {len(existing)}건 중 ja 문서와 짝 {sum(1 for f in existing if f["ja"])}건 · 그중 이번 목록에 남는 것 '
        f'{sum(f["ja"] in kept for f in existing if f["ja"])}건',
        f'- 참조 축제 {len(ref)}건 중 짝 없음 {sum(1 for f in ref if not f["ja"])}건',
        f'- 좌표 없음 {sum(1 for v in selected.values() for x in v if not x["coord"])}건 · 시정촌 좌표로 채움 '
        f'{sum(1 for v in selected.values() for x in v if x.get("coord_from"))}건', '',
        '## 현별 건수', '', ' · '.join(f'{p} {len(v)}' for p, v in selected.items()), '',
        '## 기존 축제 짝짓기', '', '| 현 | 기존 이름 | 참조 | ja 문서 | 이번 목록 |', '|---|---|---|---|---|',
    ]
    lines += [f'| {f["pref"]} | {f["name"]} | {"참조" if f["referenced"] else ""} | {f["ja"] or "없음"} | '
              f'{"남음" if f["ja"] in kept else ""} |' for f in existing]
    lines += ['', '## 고른 축제', '', '| 현 | ja 문서 | ko | 언어 수 | 길이 | 좌표 | 기존 이름 유지 |', '|---|---|---|---|---|---|---|']
    for pref, items in selected.items():
        lines += [f'| {pref} | {x["ja"]} | {x.get("ko") or x.get("ko_label") or ""} | {x["links"]} | {x["length"]} | '
                  f'{"시정촌 " + x["coord_from"] if x.get("coord_from") else ("있음" if x["coord"] else "없음")} | '
                  f'{x.get("keep_name", "")} |' for x in items]
    REPORT.write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print('\n'.join(lines[2:7]))


# ---- 2. 고르기 (요약 결과를 반영해 다시 고른다) ----

EXTRACTS = OUT / 'extracts.json'
COORDS = OUT / 'coords.json'
BATCHES = OUT / 'batches'
SUMMARIES = OUT / 'summaries'
BATCH_SIZE = 25


def summaries():
    out = {}
    for f in sorted(SUMMARIES.glob('batch_*.json')) if SUMMARIES.exists() else []:
        out.update(json.loads(f.read_text(encoding='utf-8')))
    return out


def selection():
    """현마다: 참조 축제(짝) → 유명도 순 후보. 요약에서 skip 된 것은 건너뛰고, 쓸 수 있거나 아직 요약 전인 것이
    PER_PREF 건이 될 때까지 채운다 (D-092: 날짜를 못 찾으면 빼고 다음 후보로). 참조 축제는 skip 이어도 남긴다."""
    data = json.loads(CANDIDATES.read_text(encoding='utf-8'))
    done = summaries()
    out = {}
    for pref, items in data['all'].items():
        keep = {f['ja']: f['name'] for f in data['existing'] if f['pref'] == pref and f['referenced'] and f['ja']}
        picked = [{**x, 'keep_name': keep[x['ja']]} for x in items if x['ja'] in keep]
        for x in items:
            if sum(1 for p in picked if 'skip' not in done.get(p['ja'], {}) and p['ja'] not in DROP) >= PER_PREF:
                break
            if x['ja'] in keep or x['length'] < MIN_BYTES:
                continue
            picked.append(dict(x))
        out[pref] = picked
    return out


# ---- 3. 원문 · 좌표 ----

def extracts():
    sel = selection()
    src = json.loads(EXTRACTS.read_text(encoding='utf-8')) if EXTRACTS.exists() else {}
    coords = json.loads(COORDS.read_text(encoding='utf-8')) if COORDS.exists() else {}
    items = [(pref, x) for pref, v in sel.items() for x in v]
    for i, (pref, x) in enumerate(items, 1):
        if x['ja'] not in src:
            text, full = wd.source_text(x['ja'])
            src[x['ja']] = {'source': text, 'full_length': full}
        if x['ja'] not in coords:
            c, via = (x['coord'], None) if x.get('coord') else municipality_coord(x['ja'], wd.PREF_JA[pref])
            coords[x['ja']] = {'coord': c, 'via': via}
        if i % 25 == 0 or i == len(items):
            EXTRACTS.write_text(json.dumps(src, ensure_ascii=False, indent=1), encoding='utf-8')
            COORDS.write_text(json.dumps(coords, ensure_ascii=False, indent=1), encoding='utf-8')
            print(f'[{i}/{len(items)}]')
    print(f'고른 축제 {len(items)}건 · 좌표 없음 {sum(1 for _, x in items if not coords[x["ja"]]["coord"])}건 · '
          f'시정촌 좌표 {sum(1 for _, x in items if coords[x["ja"]]["via"])}건')


# ---- 4. 요약 묶음 · 검수 ----

def batches():
    sel = selection()
    src = json.loads(EXTRACTS.read_text(encoding='utf-8'))
    done = summaries()
    todo = [{'key': x['ja'], 'pref': pref, 'ja': x['ja'], 'kana': x.get('kana'), 'name': wd.ko_name(x),
             'source': src[x['ja']]['source']}
            for pref, v in sel.items() for x in v if x['ja'] not in done]
    BATCHES.mkdir(exist_ok=True)
    SUMMARIES.mkdir(exist_ok=True)
    start = len(list(BATCHES.glob('batch_*.json')))
    for i in range(0, len(todo), BATCH_SIZE):
        n = start + i // BATCH_SIZE + 1
        (BATCHES / f'batch_{n:02d}.json').write_text(
            json.dumps(todo[i:i + BATCH_SIZE], ensure_ascii=False, indent=1), encoding='utf-8')
    print(f'요약할 축제 {len(todo)}건 → 묶음 {-(-len(todo) // BATCH_SIZE)}개 (batch_{start + 1:02d}~)')


def check():
    sel = selection()
    src = json.loads(EXTRACTS.read_text(encoding='utf-8'))
    done = summaries()
    rows, missing, skipped, suspicious, lengths = [], [], [], [], []
    per_pref = {}
    for pref, v in sel.items():
        for x in v:
            s = done.get(x['ja'])
            if not s:
                missing.append(f'{pref} · {x["ja"]}')
                continue
            if 'skip' in s:
                skipped.append(f'{pref} · {x["ja"]}{" (참조)" if x.get("keep_name") else ""} — {s["skip"]}')
                continue
            per_pref[pref] = per_pref.get(pref, 0) + 1
            raw = src[x['ja']]['source']
            text = raw.replace(',', '') + ' ' + wd.kanji_numbers(raw)
            extra = sorted(n for n in wd.numbers(s['description']) if n not in text)
            lengths.append(len(s['description']))
            if extra:
                suspicious.append(f'{pref} · {s["name"]} ({x["ja"]}) — {" ".join(extra)}')
            rows.append(f'| {pref} | {s["name"]} | {x["ja"]} | {s["month"]} | {s["date_text"]} | {len(s["description"])} |')
    lengths.sort()
    short = [p for p in wd.PREF_JA if per_pref.get(p, 0) < PER_PREF]
    lines = ['# 축제 요약 검수', '',
             f'- 쓸 수 있는 축제 {len(rows)}건 · 제외 {len(skipped)}건 · 요약 전 {len(missing)}건',
             f'- 설명 길이 중앙값 {lengths[len(lengths) // 2] if lengths else 0}자 · 최소 {lengths[0] if lengths else 0} · '
             f'최대 {lengths[-1] if lengths else 0} · 500자 미만 {sum(n < 500 for n in lengths)}건',
             f'- 원문에 없는 숫자가 있는 요약 {len(suspicious)}건 · {PER_PREF}건이 안 되는 현 {len(short)}곳 '
             f'({", ".join(f"{p} {per_pref.get(p, 0)}" for p in short)})', '',
             '## 제외', ''] + ([f'- {s}' for s in skipped] or ['- 없음'])
    lines += ['', '## 원문에 없는 숫자', ''] + ([f'- {s}' for s in suspicious] or ['- 없음'])
    lines += ['', '## 요약 전', ''] + ([f'- {m}' for m in missing] or ['- 없음'])
    lines += ['', '## 전체', '', '| 현 | 이름 | ja 문서 | 월 | 날짜 | 설명 길이 |', '|---|---|---|---|---|---|'] + rows
    (OUT / 'summary_check.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print('\n'.join(lines[2:5]))


# ---- 5. SQL ----

FEST_SQL = ROOT / 'backend/src/main/resources/festivals.sql'
# 축제가 아닌 문서(정원 · 바위)를 요약 때 축제로 쓴 경우 — 축제 이름으로 고치거나 뺀다 (ja 문서 제목 기준)
NAME_FIX = {'偕楽園': '미토 매화 축제'}          # 정원 문서. 설명은 미토 매화 축제(2~3월) 기준으로 쓰였다
DROP = {'殺生石'}                                  # 바위 문서. 행사는 곁가지다


def build_sql():
    sel = selection()
    done = summaries()
    coords = json.loads(COORDS.read_text(encoding='utf-8'))
    rows, no_coord = [], []
    for pref, v in sel.items():
        for x in v:
            s = done.get(x['ja'])
            if not s or 'skip' in s or x['ja'] in DROP:
                continue
            c = coords.get(x['ja'], {}).get('coord')
            if not c and s.get('venue'):   # 원문의 개최 시정촌 (D-092)
                for title in (s['venue'], wd.PREF_JA[pref] + s['venue']):
                    info = next(iter(page_info([title]).values()), None)
                    if info and info['page_coord']:
                        c = info['page_coord']
                        break
            if not c:
                no_coord.append(f'{pref} · {x["ja"]}')
                continue
            rows.append({**x, 'pref': pref, 'coord': c, 'name': NAME_FIX.get(x['ja'], s['name'].strip()),
                         'description': s['description'].strip(), 'month': int(s['month']),
                         'date_text': s['date_text'].strip()})

#   UNIQUE(name, prefecture_id) — 같은 현에 같은 이름이면 ja 문서의 괄호 구분어를 붙인다
    seen = {}
    for r in rows:
        seen.setdefault((r['pref'], r['name']), []).append(r)
    for same in seen.values():
        if len(same) > 1:
            for r in same:
                if not r.get('keep_name'):
                    m = re.search(r'[(（](.+?)[)）]', r['ja'])
                    r['name'] = f'{r["name"]} ({m.group(1) if m else r["ja"]})'

    sys.path.insert(0, str(ROOT / 'scripts'))
    import fetch_commons_images as fc
    for r in rows:
        r['img'] = wd.commons_image(r['image']) if r.get('image') else None
    fc.CACHE.write_text(json.dumps(fc.cache, ensure_ascii=False), encoding='utf-8')

    def q(v):
        return 'NULL' if v is None else "'" + str(v).replace('\\', '\\\\').replace("'", "''") + "'"

    order = list(wd.PREF_JA)
    rows.sort(key=lambda r: (order.index(r['pref']), r['month'], -r['links'], r['ja']))
    out = [
        '-- 축제 시드 (D-092). scripts/wiki_festivals.py sql 이 만든다 — 손으로 고치지 말 것.',
        '-- 목록은 ja 위키백과 현별 축제 분류, 설명 · 개최 시기는 원문을 한국어로 요약한 것 (CC BY-SA 4.0, 출처는 source_url).',
        '-- 기동할 때마다 실행된다. INSERT IGNORE + UNIQUE(name, prefecture_id) 로 중복을 막는다 (D-050).',
        '',
        'INSERT IGNORE INTO festivals (name, prefecture_id, month, date_text, description, lat, lng, image_path, image_credit,',
        '                              image_source, wikidata_id, source_url, created_at)',
        'SELECT v.name, p.id, v.month, v.date_text, v.description, v.lat, v.lng, v.image_path, v.image_credit, v.image_source,',
        '       v.wikidata_id, v.source_url, NOW()',
        'FROM (VALUES',
    ]
    values = []
    for r in rows:
        img = r['img'] or {}
        url = 'https://ja.wikipedia.org/wiki/' + wd.urllib.parse.quote(r['ja'].replace(' ', '_'))
        values.append(f'  ROW({q(r["pref"])}, {q(r["name"])}, {r["month"]}, {q(r["date_text"])}, {q(r["description"])}, '
                      f'{r["coord"][0]:.6f}, {r["coord"][1]:.6f}, {q(img.get("url"))}, {q(img.get("credit"))}, '
                      f'{q(img.get("source"))}, {q(r.get("qid"))}, {q(url)})')
    out.append(',\n'.join(values))
    out += [') AS v(pref, name, month, date_text, description, lat, lng, image_path, image_credit, image_source, wikidata_id, source_url)',
            'JOIN prefectures p ON p.name = v.pref;', '']
    FEST_SQL.write_text('\n'.join(out), encoding='utf-8')

    per_month = {m: sum(1 for r in rows if r['month'] == m) for m in range(1, 13)}
    print(f'축제 {len(rows)}건 · 사진 {sum(1 for r in rows if r["img"])}건 · 좌표 없어 뺌 {len(no_coord)}건 {no_coord} · '
          f'이름 구분어 {sum(len(v) for v in seen.values() if len(v) > 1)}건')
    print('월별', per_month)


if __name__ == '__main__':
    {'candidates': candidates, 'extracts': extracts, 'batches': batches, 'check': check,
     'sql': build_sql}[sys.argv[1]]()
