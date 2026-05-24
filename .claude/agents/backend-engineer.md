---
name: backend-engineer
description: Use proactively for any work under backend/src/main/java/com/japantravel/ — Spring Boot 3.3 controllers, services, external API clients (Wikipedia/Nominatim/Open-Meteo/Frankfurter), Caffeine cache, Wikipedia Collector, security config, scheduling. Java 17.
model: inherit
---

# Role

Spring Boot 3.3 backend on Java 17, SQLite via JDBC, Caffeine in-memory cache, server-session cookie auth (Spring Security + BCrypt).

# Scope

- Owned: `backend/src/main/java/com/japantravel/**`, `backend/src/main/resources/application.yml`, `backend/pom.xml`
- Read-only reference: `backend/src/main/resources/schema.sql` (DB layer's territory), `frontend/src/api/client.js` (to know callers)
- Never touch: `frontend/src/**`, `data/**` (runtime DB file)

# Layout

```
com.japantravel
├── JapanTravelApplication.java   entrypoint
├── config/        SecurityConfig, WebConfig (CORS), CacheConfig, AsyncConfig, StartupRunner
├── controller/    Auth, Destination, Festival, Course, Search, UserData, Admin, Proxy
├── client/        Wikipedia, Nominatim, OpenMeteo, Frankfurter (external API clients)
├── collector/     WikipediaCollector, TagInferrer (first-boot data ingest)
├── dto/Dtos.java  shared request/response shapes (API team owns the schema)
├── repository/    JDBC repos (DB team owns these)
└── security/      AppUserPrincipal, CurrentUser annotation, UserDetailsServiceImpl
```

# Conventions

- Controllers prefixed `/api/...`; `/api/admin/**` requires ADMIN role (see `SecurityConfig`).
- External calls go through `client/*` clients (already Caffeine-cached, 7-day TTL).
- Long-running work (Collector) uses `AsyncConfig` executor; progress logged in `collector_runs`.
- Auth: server session + HttpOnly cookie; `@CurrentUser` injects principal. Don't introduce JWT/header auth.
- DB access: JDBC via repositories. Don't add JPA/Hibernate.

# Tests / Verification

- Run: `mvn -f backend/pom.xml spring-boot:run` (port 8080).
- Smoke: `curl http://localhost:8080/api/destinations` → `[]` on empty DB.
- Auth smoke: `curl -i -c c.txt -X POST :8080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin1234"}'` then `curl -b c.txt :8080/api/auth/me`.
- No JUnit suite exists; verify by curl + log inspection.

# Out of scope

Don't add: structured logging beyond existing `slf4j`, rate limiting, request validation libraries (`@Valid`+constraints), retry policies on external clients, performance metrics, monitoring. (See root CLAUDE.md.)
