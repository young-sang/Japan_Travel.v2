"""추천용 임베딩을 만든다 — 여행지 · 축제의 "이름 + 지역 + 설명"을 로컬 Ollama bge-m3 로 벡터화 (D-095).

    ollama pull bge-m3                  # 처음 한 번 (약 1.2GB)
    python scripts/embed_places.py      # 전체 다시 계산 → scripts/embedding/places.json

입력은 시드 SQL(destinations.sql · festivals.sql)이다. 기동할 때 DB 에 그대로 들어가므로 DB 와 같은 장소 목록이다.
장소는 (type, pref, name) 으로 가리킨다 — DB 의 UNIQUE(name, prefecture_id) 와 같은 기준이라 환경마다 id 가 달라도 같은 장소다.
장소 시드가 바뀌면 다시 실행한다. 바뀐 것만 고르지 않고 항상 전체를 다시 계산한다.
"""
import json
import sys
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8')

ROOT = Path(__file__).resolve().parent.parent
RESOURCES = ROOT / 'backend/src/main/resources'
OUT = ROOT / 'scripts/embedding/places.json'

OLLAMA = 'http://localhost:11434/api/embed'
MODEL = 'bge-m3'
BATCH = 32

# 시드 파일과 ROW(...) 안에서 pref · name · description 이 몇 번째 값인지
SOURCES = {
    'destination': (RESOURCES / 'destinations.sql', 2),   # (pref, name, description, ...)
    'festival': (RESOURCES / 'festivals.sql', 4),         # (pref, name, month, date_text, description, ...)
}


def rows(sql):
    """`  ROW(` 로 시작하는 행마다 앞에서부터 값을 읽는다. 문자열은 '' 이스케이프를 풀고 줄바꿈을 그대로 둔다."""
    out = []
    i = sql.find('\n  ROW(')
    while i != -1:
        i += len('\n  ROW(')
        values = []
        while True:
            while sql[i] in ' \n':
                i += 1
            if sql[i] == "'":
                j = i + 1
                buf = []
                while True:
                    k = sql.index("'", j)
                    buf.append(sql[j:k])
                    if sql[k + 1:k + 2] == "'":      # '' → '
                        buf.append("'")
                        j = k + 2
                    else:
                        break
                values.append(''.join(buf))
                i = k + 1
            else:                                     # 숫자 · NULL
                j = i
                while sql[j] not in ',)':
                    j += 1
                values.append(sql[i:j].strip())
                i = j
            if sql[i] == ')':
                break
            i += 1                                    # ,
        out.append(values)
        i = sql.find('\n  ROW(', i)
    return out


def places():
    out = []
    for kind, (path, desc_at) in SOURCES.items():
        sql = path.read_text(encoding='utf-8')
        found = rows(sql)
        expected = sql.count('\n  ROW(')
        if len(found) != expected:
            sys.exit(f'{path.name}: ROW {expected}개 중 {len(found)}개만 읽었다')
        for v in found:
            out.append({'type': kind, 'pref': v[0], 'name': v[1], 'text': f'{v[1]}\n{v[0]}\n{v[desc_at]}'})
    return out


def embed(texts):
    body = json.dumps({'model': MODEL, 'input': texts}).encode('utf-8')
    req = urllib.request.Request(OLLAMA, data=body, headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req, timeout=600) as res:
            return json.load(res)['embeddings']
    except urllib.error.URLError as e:
        sys.exit(f'Ollama 에 연결하지 못했다 ({e.reason}). `ollama serve` 를 켜고 `ollama pull {MODEL}` 했는지 확인하세요')


def main():
    items = places()
    keys = {(p['type'], p['pref'], p['name']) for p in items}
    if len(keys) != len(items):
        sys.exit('같은 (type, pref, name) 장소가 두 번 나온다')

    vectors = []
    for start in range(0, len(items), BATCH):
        vectors += embed([p['text'] for p in items[start:start + BATCH]])
        print(f'\r임베딩 {len(vectors)}/{len(items)}', end='', flush=True)
    print()

    counts = {kind: sum(p['type'] == kind for p in items) for kind in SOURCES}
    result = {
        'model': MODEL,
        'dim': len(vectors[0]),
        'created_at': datetime.now().isoformat(timespec='seconds'),
        'counts': counts,
        'places': [{'type': p['type'], 'pref': p['pref'], 'name': p['name'], 'vector': [round(x, 6) for x in vec]}
                   for p, vec in zip(items, vectors)],
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(result, ensure_ascii=False, separators=(',', ':')), encoding='utf-8')
    print(f'여행지 {counts["destination"]} · 축제 {counts["festival"]} → {OUT.relative_to(ROOT)} ({result["dim"]}차원)')


if __name__ == '__main__':
    main()
