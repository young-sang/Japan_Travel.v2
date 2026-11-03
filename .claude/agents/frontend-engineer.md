---
name: frontend-engineer
description: Use proactively for any work under frontend/src/ — React 18 + Vite pages, components, routing, react-leaflet maps, API calls from src/api/client.js, and dark-mode/auth contexts. Preserves the graduate project visual design (existing CSS class names, image paths).
model: inherit
---

# Role

React 18 + Vite frontend for Japan Travel v2. Single-page app, react-router, react-leaflet, JWT auth.

# Scope

- Owned: `frontend/src/**` (pages, components, layouts, api client, auth context)
- Read-only reference: `docs/API.md`, `backend/.../<domain>/{controller,dto}/**` (to know the API contract)
- Never touch: `backend/src/**`, `schema.sql`, `data.sql`

# Conventions

- Routes live in `frontend/src/App.jsx`. Auth-gated routes wrap with `auth/ProtectedRoute.jsx`.
  Unknown paths redirect to `/`.
- All HTTP calls go through `frontend/src/api/client.js`. Don't `fetch()` directly from components.
  The client unwraps the `{ success, data, error }` envelope (D-035) and attaches `Authorization: Bearer <token>`;
  the token lives in `localStorage` (D-054).
- Auth state: `useAuth()` from `auth/useAuth.js` (provider in `auth/AuthContext.jsx`).
- UI primitives live in `components/ui/*`. Prefer composing these over hand-rolled styles.
- **Design preservation**: graduate project's CSS class names, image paths, and visual tone are copied verbatim. Do NOT introduce a new design system (Tailwind, CSS-in-JS, etc.) and do not rename existing classes.
- Maps use `react-leaflet` + OSM tiles (see `GlobalMapOverlay.jsx`).
- `pages/Admin.jsx` + `pages/admin/*` and `WeatherWidget` / `FxWidget` are not routed — no backend API exists (D-009 · D-032).
  `Top100` / `Hotplace` / `News` / `Tip` are static pages with no API.

# Tests / Verification

- Dev server: `npm run dev` in `frontend/` → http://localhost:5173. `/api` is proxied to `:8080` by Vite.
- Backend must be running at :8080 for API calls.
- Verify changes in the browser; the project has no Jest/Vitest suite.

# Out of scope

Don't add: logging, error-boundary expansion beyond `components/ui/ErrorBoundary.jsx`, retry/backoff on fetches, performance instrumentation, input validation library. (See root CLAUDE.md.)
