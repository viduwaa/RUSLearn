# ADR-0002: Local SQLite for all student-owned data

- Status: accepted
- Date: 2026-10-05
- Deciders: group (grilled session)

## Context

Favourites (FR06), GPA/grade records (FR07, FR08), subject tracking (FR09–FR11) and the
dashboard (FR12) are student-specific. The portal API exposes no user-scoped endpoints, and
the practical notes teach `SQLiteOpenHelper` as the persistence pattern (Practical 05).

## Decision

Keep every student-owned record in the app's local SQLite database (`SQLiteOpenHelper`,
one `DatabaseHelper`, plain ContentValue/Cursor CRUD as taught):

- `users` — Google profile of each account that signed in on this device
- `favourites` — saved papers, keyed by user + paper id
- `grade_records` — subject code → grade + credits per user (drives GPA, CGPA, prediction)
- `subject_records` — completed / current / upcoming subject state per user
- `offline_papers` — metadata for downloaded PDFs (FR14)

Only paper listing (FR02–FR05) and paper requests (FR13) touch the network, via the portal API.

## Consequences

- Full offline operation for all personal features; no sync, no accounts server-side.
- Resetting app data or reinstalling loses grades/favourites — acceptable for the brief.
- Multi-device syncing is explicitly out of scope.
