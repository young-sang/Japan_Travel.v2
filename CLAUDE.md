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

## Agent Team

이 프로젝트는 4종 역할의 서브에이전트가 정의되어 있다 (`.claude/agents/`):

- `api-designer` — REST 계약(`/api/...` 경로, DTO, 상태코드) 설계자. 새 기능의 **첫 번째** 산출물 담당.
- `backend-engineer` — Spring Boot 3.3 / Java 17. `backend/src/main/java/com/japantravel/{controller,service,client,collector,config,security}/**`.
- `frontend-engineer` — React 18 / Vite. `frontend/src/**`. graduate project의 디자인을 그대로 보존.
- `db-specialist` — SQLite. `backend/src/main/resources/schema.sql` + `backend/.../repository/**`.

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