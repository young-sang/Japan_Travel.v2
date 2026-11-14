"""장소 하나와 가장 비슷한 여행지 4곳 + 축제 1곳을 보여 준다 — 임베딩이 쓸 만한지 사람이 직접 확인하는 명령 (M1-2).

    python scripts/similar_places.py "오타루 운하"            # 여러 개를 한 번에 넣어도 된다
    python scripts/similar_places.py "오타루 운하" "후지산"

입력은 scripts/embed_places.py 가 만든 scripts/embedding/places.json 이다.
벡터가 정규화돼 있어 두 벡터의 내적이 곧 코사인 유사도다.
"""
import json
import sys
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8')

ROOT = Path(__file__).resolve().parent.parent
PLACES = ROOT / 'scripts/embedding/places.json'

TOP = {'destination': 4, 'festival': 1}
KIND = {'destination': '여행지', 'festival': '축제'}


def label(p):
    return f"{p['name']} ({p['pref']}, {KIND[p['type']]})"


def similar(base, places):
    scored = [(sum(a * b for a, b in zip(base['vector'], p['vector'])), p) for p in places if p is not base]
    scored.sort(key=lambda t: -t[0])
    picked = []
    for kind, n in TOP.items():
        picked += [t for t in scored if t[1]['type'] == kind][:n]
    return sorted(picked, key=lambda t: -t[0])


def show(query, places):
    found = [p for p in places if p['name'] == query]
    if not found:
        hints = [label(p) for p in places if query in p['name']][:10]
        print(f'## {query}: 없는 이름')
        print('  후보: ' + (', '.join(hints) if hints else '없음'))
        return
    base = found[0]
    print(f'## {label(base)}')
    if len(found) > 1:
        print('  (같은 이름이 더 있음: ' + ', '.join(label(p) for p in found[1:]) + ')')
    for score, p in similar(base, places):
        same = '  ★같은 현' if p['pref'] == base['pref'] else ''
        print(f'  {score:.3f}  {label(p)}{same}')


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    if not PLACES.exists():
        sys.exit(f'{PLACES.relative_to(ROOT)} 가 없다. 먼저 `python scripts/embed_places.py` 를 실행하세요')
    places = json.loads(PLACES.read_text(encoding='utf-8'))['places']
    for i, query in enumerate(sys.argv[1:]):
        if i:
            print()
        show(query, places)


if __name__ == '__main__':
    main()
