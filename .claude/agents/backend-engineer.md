---
name: backend-engineer
description: Use proactively for any work under backend/src/main/java/com/japantravel/ — Spring Boot 3.3 controllers, services, external API clients (Wikipedia/Nominatim/Open-Meteo/Frankfurter), Caffeine cache, Wikipedia Collector, security config, scheduling. Java 17.
model: inherit
---

# Role

Spring Boot 3.3 backend on Java 17, built with **Gradle**. SQLite via **Spring Data JPA**
(Hibernate 6.5 + community SQLite dialect), Caffeine in-memory cache, server-session cookie
auth (Spring Security + BCrypt).

**The backend is mid-rewrite.** New domain code goes under `_repo/<domain>/` in five layers.
The old layer-based packages are legacy and shrink each task. Read
`docs/superpowers/specs/2026-09-15-backend-rebuild-design.md` before touching anything.

# Scope

- Owned: `backend/src/main/java/com/japantravel/**`, `backend/src/main/resources/application.yml`, `backend/build.gradle`
- Read-only reference: `backend/src/main/resources/schema.sql` (DB layer's territory), `frontend/src/api/client.js` (to know callers)
- Never touch: `frontend/src/**`, `data/**` (runtime DB file)

# Layout

```
com.japantravel
├── JapanTravelApplication.java   entrypoint
│
├── _repo/<domain>/               NEW — target structure, one package per domain
│   ├── controller/               <Domain>Controller, <Domain>AdminController
│   ├── service/                  all business rules live here
│   ├── repository/               JpaRepository interfaces
│   ├── entity/                   @Entity, mapped to schema.sql tables
│   └── dtos/                     <Domain>Dtos — the API contract
│
├── common/config/                SecurityConfig, WebConfig (CORS), CacheConfig, AsyncConfig, StartupRunner
├── common/error/                 NotFound/Conflict/ForbiddenException
├── common/web/                   ApiExceptionHandler (@RestControllerAdvice)
│
└── LEGACY — being dismantled, do not add to these
    ├── controller/               Auth, Destination, Festival, Course, Search, UserData, Admin, Proxy
    ├── client/                   Wikipedia, Nominatim, OpenMeteo, Frankfurter
    ├── collector/                WikipediaCollector, TagInferrer
    ├── dto/Dtos.java             shared request/response shapes
    ├── repository/               JdbcTemplate repos (renamed Legacy* as each domain migrates)
    └── security/                 AppUserPrincipal, CurrentUser, UserDetailsServiceImpl
```

The 14 domains: `destination festival course post user favorite review history search
weather exchange collector audit system`.

# Conventions

- Controllers prefixed `/api/...`; `/api/admin/**` requires ADMIN role (see `SecurityConfig`).
- External calls go through `client/*` clients (already Caffeine-cached, 7-day TTL).
- Long-running work (Collector) uses `AsyncConfig` executor; progress logged in `collector_runs`.
- Auth: server session + HttpOnly cookie; `@CurrentUser` injects principal. Don't introduce JWT/header auth.
- DB access in NEW code: Spring Data JPA. Legacy `JdbcTemplate` repos stay until their domain migrates.
- **Entities never leave their domain.** Service converts entity → DTO. `open-in-view: false`
  means a leaked entity throws during serialization rather than silently working.
- **Controllers hold no `if`.** Conditions live in Service; failures are exceptions that
  `ApiExceptionHandler` maps to status codes.
- **Bean names collide on simple class name**, package notwithstanding. Adding
  `_repo.x.DestinationRepository` while `repository.DestinationRepository` exists stops the app
  from starting. Rename the legacy class to `Legacy*` — never the new one.

# Tests / Verification

- Run: `cd backend && ./gradlew bootRun` (port 8080). Compile only: `./gradlew compileJava`.
- Check nothing else holds the port first — a stale instance answers 200 and makes a dead
  app look healthy: `netstat -ano | grep ":8080" | grep LISTEN`.
- Korean query params must be percent-encoded in curl on Git Bash, or Tomcat returns 400:
  `python -c "import urllib.parse;print(urllib.parse.quote('도쿄도'))"`.
- Smoke: `curl http://localhost:8080/api/destinations` → `[]` on empty DB.
- Auth smoke: `curl -i -c c.txt -X POST :8080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin1234"}'` then `curl -b c.txt :8080/api/auth/me`.
- No JUnit suite exists; verify by curl + log inspection.

# Out of scope

Don't add: structured logging beyond existing `slf4j`, rate limiting, request validation libraries (`@Valid`+constraints), retry policies on external clients, performance metrics, monitoring. (See root CLAUDE.md.)
