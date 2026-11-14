"""시연 계정 보강 — 취향이 다른 계정 44개와 활동 · 리뷰를 만드는 SQL 을 생성한다 (D-102).

    python scripts/demo_users.py        # → scripts/demo_users.sql (커밋한다)

손으로 쓴 scripts/demo_seed.sql(D-070)의 계정 6개는 건드리지 않는다. seed 서비스가 demo_seed.sql 다음에 실행한다.
random 시드가 고정이라 다시 만들어도 같은 SQL 이다. 장소 시드가 바뀌면 다시 만든다.

취향 그룹은 장소 이름 · 설명 첫 문장의 키워드로 고른다. 추천은 임베딩으로 고르므로, 데이터까지 임베딩으로 고르면
"추천이 취향에 맞는가"를 같은 잣대로 확인하게 되어 의미가 없다.
"""
import random
import re
import sys
from pathlib import Path

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parent))
import embed_places  # noqa: E402  시드 SQL 파서(rows) · 장소 시드 위치(SOURCES)를 같이 쓴다

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / 'scripts/demo_users.sql'
SEED = 20261114
PASSWORD_HASH = '$2a$10$nEA1HxHVCVOpeGkN2/LK2uwGplF7Ty.73iAXXzBigYKL0WCz/JLVG'   # BCrypt("demo1234"), demo_seed.sql 과 같다
USERS = 44

# 그룹마다 (이름 정규식, 설명 첫 문장 정규식). 축제 그룹은 축제 전체가 풀이다
GROUPS = {
    '온천': (r'온천', r'온천'),
    '자연': (r'(산|다케|폭포|호수|고원|협곡|국립공원|습원|해안|곶)$',
           r'화산|폭포|호수|고원|협곡|국립공원|습원|봉우리|산이다|산으로'),
    '축제': None,
    '도시': (r'(거리|시장|타워|요코초|상점가|도리|스카이트리|항|미나토|역)$|거리|시장',
           r'번화가|상점가|쇼핑|시장|환락가|전망대|타워|야경|도심|항구 도시|거리이다|지구이다|지역이다'),
    '절신사': (r'(신사|신궁|타이샤|데라|지)$', r'사찰|절이다|신사이다|신사로|신궁|불교|사원'),
}

FIRST = ['여행하는', '산책하는', '떠나는', '걷는', '꿈꾸는', '헤매는', '노을보는', '기차타는', '느긋한', '부지런한', '조용한']
SECOND = ['곰', '수달', '여우', '고양이', '펭귄', '다람쥐', '토끼', '거북이', '참새', '너구리', '두루미', '사슴']

COMMENTS = {
    5: ['또 가고 싶은 곳이에요. 강력 추천합니다.', '사진보다 실제가 훨씬 좋았어요.', '일정에 넣길 정말 잘했어요.',
        '분위기가 최고였습니다. 시간 가는 줄 몰랐어요.', '아침 일찍 가니 한적하고 좋았어요.', '이번 여행에서 제일 기억에 남는 곳이에요.',
        '가족 모두 만족했어요.'],
    4: ['좋았어요. 사람이 조금 많은 것만 빼면요.', '천천히 둘러보기 좋은 곳이에요.', '기대한 만큼 괜찮았어요.',
        '교통이 조금 불편하지만 갈 만해요.', '계절을 잘 맞춰 가면 더 좋을 것 같아요.', '근처 맛집과 묶으면 좋아요.'],
    3: ['무난했어요. 한 번쯤은 가 볼 만해요.', '생각보다 볼거리가 적었어요.', '날씨 탓인지 그저 그랬어요.',
        '사람이 너무 많아서 제대로 못 봤어요.', '시간이 남으면 들러 볼 정도예요.', '나쁘진 않은데 다시 가진 않을 것 같아요.'],
    2: ['기대보다 아쉬웠어요.', '가는 길이 너무 멀고 힘들었어요.', '볼거리에 비해 붐볐어요.',
        '사진만큼 예쁘지 않았어요.', '굳이 일정에 넣을 필요는 없을 것 같아요.', '안내가 부족해서 헤맸어요.'],
    1: ['제 취향과는 맞지 않았어요.', '시간이 아까웠어요.', '다시는 안 갈 것 같아요.',
        '너무 붐비고 정신없었어요.', '기대가 커서인지 실망이 컸어요.', '추천하기 어려워요.'],
}


def load_places():
    """시드 SQL 에서 (kind, pref, name, 설명 첫 문장)."""
    out = []
    for kind, (path, desc_at) in embed_places.SOURCES.items():
        for v in embed_places.rows(path.read_text(encoding='utf-8')):
            first = re.split(r'(?<=다)\.\s', v[desc_at] or '', maxsplit=1)[0][:200]
            out.append((kind, v[0], v[1], first))
    return out


def pools(places):
    destinations = [p for p in places if p[0] == 'destination']
    festivals = [p for p in places if p[0] == 'festival']
    out = {}
    for group, rule in GROUPS.items():
        if rule is None:
            out[group] = festivals
        else:
            name_re, desc_re = rule
            out[group] = [p for p in destinations if re.search(name_re, p[2]) or re.search(desc_re, p[3])]
    return out, destinations, festivals


def nicknames(rng):
    names = [a + b for a in FIRST for b in SECOND]
    rng.shuffle(names)
    return names[:USERS]


def make_users(rng, group_pools, destinations, festivals):
    users = []
    groups = list(GROUPS)
    for i, nickname in enumerate(nicknames(rng)):
        group = groups[i % len(groups)]
        pool = group_pools[group]
        total = rng.randint(20, 30)
        liked_n = min(round(total * 0.8), int(len(pool) * 0.7))
        liked = rng.sample(pool, liked_n)
        festival_n = 0 if group == '축제' else rng.randint(0, 2)   # 축제 그룹이 아니어도 축제를 0~2곳 섞는다
        extra = [p for p in rng.sample(destinations, total * 2) if p not in liked][:total - liked_n - festival_n]
        places = liked + extra + rng.sample(festivals, festival_n)
        rng.shuffle(places)
        joined = rng.randint(10, 90)

        favorites = rng.sample(liked, min(rng.randint(5, 8), len(liked)))
        reviewed = rng.sample(places, rng.randint(6, 8))
        reviews = []
        for p in reviewed:
            if p in liked:
                rating = rng.choices([5, 4, 3], weights=[5, 4, 1])[0]
            else:
                rating = rng.choices([4, 3, 2, 1], weights=[2, 3, 3, 2])[0]
            reviews.append((p, rating, rng.choice(COMMENTS[rating])))
        users.append({
            'username': f'traveler{i + 1:02d}', 'nickname': nickname, 'group': group, 'joined': joined,
            'history': [(p, rng.randint(1, joined * 24 * 60)) for p in places],
            'favorites': [(p, rng.randint(1, joined)) for p in favorites],
            'reviews': [(p, r, c, rng.randint(1, joined)) for p, r, c in reviews],
        })
    return users


def q(s):
    return "'" + s.replace("'", "''") + "'"


def insert(table, cols, select_cols, kind, rows, row_alias):
    """(VALUES ROW(...)) 를 사용자 · 장소 이름으로 JOIN 해 넣는다. 장소는 (현, 이름) 으로 찾는다."""
    place_table, id_col = ('destinations', 'destination_id') if kind == 'destination' else ('festivals', 'festival_id')
    return '\n'.join([
        f'INSERT INTO {table} (user_id, {id_col}, {cols})',
        f'SELECT u.id, x.id, {select_cols}',
        'FROM (VALUES',
        ',\n'.join(rows),
        f') AS v({row_alias})',
        'JOIN users u ON u.username = v.username',
        'JOIN prefectures p ON p.name = v.pref',
        f'JOIN {place_table} x ON x.name = v.place AND x.prefecture_id = p.id;',
        '',
    ])


def render(users):
    out = [
        '-- 시연 계정 보강 (D-102). scripts/demo_users.py 가 만든다 — 손으로 고치지 말 것.',
        '-- demo_seed.sql 다음에 실행한다. 몇 번을 돌려도 같은 상태가 된다 — traveler 계정을 지우면 활동이 FK CASCADE 로 함께 지워진다.',
        '-- 계정 ' + str(len(users)) + '개, 비밀번호는 모두 demo1234. 취향 그룹: ' + ' · '.join(
            f"{g} {sum(u['group'] == g for u in users)}" for g in GROUPS),
        '',
        'SET NAMES utf8mb4;',
        "DELETE FROM users WHERE username LIKE 'traveler%';",
        f'SET @pw = {q(PASSWORD_HASH)};',
        '',
        'INSERT INTO users (username, password_hash, nickname, role, created_at) VALUES',
        ',\n'.join(f"  ({q(u['username'])}, @pw, {q(u['nickname'])}, 'USER', NOW() - INTERVAL {u['joined']} DAY)"
                   for u in users) + ';',
        '',
    ]
    for kind in ('destination', 'festival'):
        suffix = 'destinations' if kind == 'destination' else 'festivals'
        history = [f"  ROW({q(u['username'])}, {q(p[1])}, {q(p[2])}, {m})"
                   for u in users for p, m in u['history'] if p[0] == kind]
        favorites = [f"  ROW({q(u['username'])}, {q(p[1])}, {q(p[2])}, {d})"
                     for u in users for p, d in u['favorites'] if p[0] == kind]
        reviews = [f"  ROW({q(u['username'])}, {q(p[1])}, {q(p[2])}, {r}, {q(c)}, {d})"
                   for u in users for p, r, c, d in u['reviews'] if p[0] == kind]
        if history:
            out.append(insert(f'history_{suffix}', 'visited_at', 'NOW(6) - INTERVAL v.mins MINUTE', kind, history,
                              'username, pref, place, mins'))
        if favorites:
            out.append(insert(f'favorite_{suffix}', 'created_at', 'NOW() - INTERVAL v.days DAY', kind, favorites,
                              'username, pref, place, days'))
        if reviews:
            out.append(insert(f'review_{suffix}', 'rating, comment, created_at',
                              'v.rating, v.comment, NOW(6) - INTERVAL v.days DAY', kind, reviews,
                              'username, pref, place, rating, comment, days'))
    expected = {
        'users': len(users),
        'history': sum(len(u['history']) for u in users),
        'favorites': sum(len(u['favorites']) for u in users),
        'reviews': sum(len(u['reviews']) for u in users),
        'low': sum(r <= 2 for u in users for _, r, _, _ in u['reviews']),
    }
    out += [
        '-- 확인용 — 기대값: ' + ', '.join(f'{k} {v}' for k, v in expected.items()),
        'SELECT',
        "  (SELECT COUNT(*) FROM users WHERE username LIKE 'traveler%') AS users,",
        "  (SELECT COUNT(*) FROM history_destinations h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%')",
        "  + (SELECT COUNT(*) FROM history_festivals h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%') AS history,",
        "  (SELECT COUNT(*) FROM favorite_destinations h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%')",
        "  + (SELECT COUNT(*) FROM favorite_festivals h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%') AS favorites,",
        "  (SELECT COUNT(*) FROM review_destinations h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%')",
        "  + (SELECT COUNT(*) FROM review_festivals h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%') AS reviews,",
        "  (SELECT COUNT(*) FROM review_destinations h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%' AND h.rating <= 2)",
        "  + (SELECT COUNT(*) FROM review_festivals h JOIN users u ON u.id = h.user_id WHERE u.username LIKE 'traveler%' AND h.rating <= 2) AS low;",
        '',
    ]
    return '\n'.join(out), expected


def main():
    rng = random.Random(SEED)
    group_pools, destinations, festivals = pools(load_places())
    users = make_users(rng, group_pools, destinations, festivals)
    sql, expected = render(users)
    OUT.write_text(sql, encoding='utf-8')
    print('그룹별 장소 풀: ' + ' · '.join(f'{g} {len(p)}' for g, p in group_pools.items()))
    print('기대값: ' + ', '.join(f'{k} {v}' for k, v in expected.items()))
    print(f'→ {OUT.relative_to(ROOT)}')


if __name__ == '__main__':
    main()
