## Project Context

Internal tool for authenticated users only. Do NOT include security,
authentication, rate limiting, or input validation in specs or code
unless explicitly requested.

## Do NOT Add Unless Explicitly Requested

- Security
- Monitoring
- Logging
- Performance optimization
- Scalability considerations
- Error recovery / retry strategies

## 진행 방식: 하이퍼 애자일 (`docs/AGILE.md`)

졸업작품 보완 작업은 `docs/AGILE.md`의 규칙과 백로그를 따른다. 스프린트를 시작하기 전에 그 문서부터 읽는다. 핵심 규칙은 다음과 같다.
- 스프린트는 반나절에서 하루, 한 번에 하나만 한다. 끝날 때마다 시연할 수 있는 상태여야 한다.
- **결정 게이트는 매번 묻는다.** 갈림길이 나오면 크기와 상관없이 멈추고, 추천안을 맨 앞에 둔 선택지로 묻는다. 답을 받으면 DECISIONS에 기록한다.
- 완료 기준은 브라우저 확인, 백엔드 테스트 통과, 프론트 빌드, DECISIONS·README 반영, 커밋이다. push는 하지 않는다.
- 스프린트가 끝나면 데모·회고를 보고하고 AGILE.md의 백로그와 로그를 갱신한 뒤 **멈춘다.** 사용자가 승인해야 다음 스프린트에 들어간다.
- push와 태그 push는 매번 묻는다.

## Agent Team

이 프로젝트는 4종 역할의 서브에이전트가 정의되어 있다 (`.claude/agents/`):

- `api-designer` — REST 계약(`/api/...` 경로, DTO, 상태코드) 설계자. 새 기능의 **첫 번째** 산출물 담당.
- `backend-engineer` — Spring Boot 3.3 / Java 17 / Gradle. 코드는 `backend/src/main/java/com/japantravel/<domain>/{controller,service}/**` (D-029 로 `_repo` 접두사 폐기).
- `frontend-engineer` — React 18 / Vite. `frontend/src/**`. graduate project의 디자인을 그대로 보존.
- `db-specialist` — MySQL 8 + Spring Data JPA. `backend/src/main/resources/schema.sql` + `backend/.../<domain>/{entity,repository}/**`.

### 같은 세션 안에서 (표준 subagent 위임)

단일 계층 작업이거나 빠른 위임이면 `Agent` 툴로 위 역할을 그대로 호출. 여러 계층이 필요하면 한 메시지 안에서 `Agent` 호출을 병렬로 보낸다.

### 세션 간 협업이 필요할 때 (실험적 Agent Teams)

cross-cutting 기능(예: 새 엔드포인트 + DB 변경 + 프론트 페이지)은 Agent Teams로 처리.

**활성화**: `~/.claude/settings.json`의 `env.CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS = "1"` (Claude Code v2.1.32+). 적용은 `claude` 재실행 후.

**카논 spawn 프롬프트** (lead 세션에 그대로 붙여넣기):

```
Create an agent team named "jt-feature" to implement <기능 한 줄>.

Spawn 4 teammates using these project agent types:
- api-designer
- backend-engineer
- frontend-engineer
- db-specialist

Coordination protocol (assign via shared task list):
1. api-designer FIRST drafts the REST contract (endpoint + DTOs + status codes)
   and posts it to the shared task list as the gating deliverable.
2. Once the contract is posted:
   - db-specialist updates schema.sql + repositories (flag DB-wipe if schema breaks).
   - backend-engineer wires controller → service → repository against the DTOs.
   - frontend-engineer adds the page/component and the api/client.js call.
   These three run in parallel.
3. backend-engineer signals "endpoint live" → frontend-engineer switches from
   stub to real call and verifies in the browser at :5173.
4. Lead runs end-to-end smoke (curl per backend-engineer's notes + browser walkthrough)
   and then cleans up the team.

Constraints (inherited from project CLAUDE.md): no security/logging/validation/
retry/perf work unless the feature explicitly needs it.
```

**규칙**:
- 팀은 cross-cutting 기능에만 사용. 단일 파일 수정·색상 변경 같은 건 그냥 단일 세션에서.
- 한 번에 한 팀만. 새 기능 시작 전 이전 팀은 lead에게 "cleanup the team" 지시.
- 팀 정의 파일은 만들지 않는다 (런타임에 lead가 스폰). 우리가 미리 정의하는 건 4종 역할(`.claude/agents/*.md`)뿐.

## DB 규칙
- db를 삭제하기 전에 백업본을 만들어 놓을 것.

## 의사결정 기록 (`docs/DECISIONS.md`)

되돌리기 어렵거나 나중에 "왜 이렇게 했지?"가 나올 결정을 내리면 **그 자리에서**
`docs/DECISIONS.md` 맨 아래에 항목을 추가한다. 나중에 몰아서 쓰지 않는다.

기록할 것 — 스키마·아키텍처 변경, 범위에 넣거나 뺀 기능, 라이브러리·도구 선택,
기존 동작을 일부러 보존하거나 일부러 깨는 결정, 검토했지만 **기각한 대안**.

기록하지 않을 것 — 변수명, 파일 위치, 되돌리기 쉬운 것.

규칙:
- 번호는 이어서 붙이고 줄이지 않는다. 색인 표에도 한 줄 추가한다.
- 결정이 뒤집히면 **지우지 않는다.** 원래 항목을 `번복됨`으로 바꾸고 새 번호를 가리킨다.
- 기각한 대안과 그 이유를 반드시 쓴다. 같은 고민이 돌아왔을 때 이미 검토했다는 걸
  아는 것이 이 문서의 절반이다.
- 항목이 길어지면 `docs/superpowers/specs/` 에 문서를 만들고 여기서 링크한다.