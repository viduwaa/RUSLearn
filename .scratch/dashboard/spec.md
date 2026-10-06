# Personal dashboard + docs polish (M6)

Feature: FR12 — the dashboard summarises academic state and launches everything.
Also: final docs (PROJECT_STRUCTURE.md, README) and the FR01–FR14 test matrix.

## Scope

- `ui/DashboardActivity` + `activity_dashboard.xml` rebuilt: session card
  (name/email + Sign out), overall-GPA card (value/band/credits bar + per-semester
  GPA lines + current-semester line derived from tracking statuses), a 2-column
  shortcut grid (papers / favourites / offline / requests / GPA / tracking) and a
  "Recent favourites" strip (max 3).
- Programme auto-detection: the first programme with saved grades/statuses wins;
  nothing saved shows placeholder copy.
- `PROJECT_STRUCTURE.md` (also where catalogue/asset data comes from) and `README.md`
  (setup, OAuth client + SHA-1 steps, run instructions, per-FR test matrix).
- Design pass внимания: consistent token usage, 48dp targets, sentence case,
  4/12/16/24 rhythm — colors already tokenized in values/colors.xml.

## Non-goals

- No new persistence; no profile editing; charts.
