# Papers browse/search/filter + detail actions (M3)

Feature: FR02 (browse), FR03 (search), FR04 (filter), FR05 (view), favourites
foundation (FR06), offline-download foundation (FR14).

## Scope

- `DatabaseHelper` v2 — `favourites` + `offline_papers` tables, both keyed by the
  paper's Drive file id and carrying a `paper_json` snapshot (Paper model round-trips).
- `data/FilterPrefs` — the portal's habit: filter choices are remembered automatically.
- `ui/PapersActivity` — search field, four filters (Study Year, Semester, Department,
  Past-Paper Year — the last derived from a first full papers load), ListView +
  `PaperAdapter`, loading ProgressBar, portal-identical empty text, 429/offline toasts.
- `ui/PaperDetailActivity` — open in browser (FR05), favourite toggle (FR06),
  download-for-offline layered attempt (FR14, ADR-0006 planned alongside).
- `data/DrivePaperFetcher` — public Drive link first, then Drive API with the user's
  OAuth token (scope granted at sign-in), then a graceful "open online" failure.
- `GoogleAuth.options` gains the drive.readonly scope request (consent once at login).
- Dashboard: probe button replaced by a real "Browse papers" entry.

## Non-goals

- Favourites screen, offline list screen and paper requests remain M4.
- PDF in-app rendering (papers open in the device browser / Drive viewer).
