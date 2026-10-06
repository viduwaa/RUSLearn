# AGENTS.md

Instructions for AI coding agents working in this repo.

## Project

RUSLearn — FOT Academic Companion: an Android app (Java + XML) for Rajarata University
Faculty of Technology students. It wraps the existing FOT Past Papers Portal
(https://past-paper-portal.pages.dev — repo at `C:\Projects\past-paper-portal`) REST API for
past papers and paper requests, keeps all student-specific data (favourites, grades,
subject tracking) in local SQLite, and signs users in with Google, restricted to
university-domain accounts (`tec.rjt.ac.lk`).

Built per the course brief in `docs/FOT_Academic_Companion.pdf` (ICT 3219 module), following
the patterns taught in `docs/practical_notes/` (plain Activities, `findViewById`,
`SQLiteOpenHelper`). The group is graded on code quality, so keep every file simple and readable.

## Docs map

- `docs/FOT_Academic_Companion.pdf` — project brief: FR01–FR14, NFR01–NFR09, locked tech stack.
- `docs/practical_notes/` — lecturer's practical sheets; the code-style reference.
- `GLOSSARY.md` — domain vocabulary; use these exact terms in code, issues and docs.
- `docs/adr/` — architecture decisions; read the ones touching your area before coding.
- `docs/agents/` — how agents consume the issue tracker and domain docs.
- `.scratch/` — issue tracker (local markdown, one feature per directory).

## Agent skills

### Issue tracker

Issues live as local markdown under `.scratch/<feature>/` in this repo. See `docs/agents/issue-tracker.md`.

### Domain docs

Single-context: a root `GLOSSARY.md` plus `docs/adr/` at the repo root. See `docs/agents/domain.md`.

## House rules

- Java only (no Kotlin), XML layouts only (no Jetpack Compose). minSdk 24; Empty Views Activity template.
- Plain Activities + `findViewById`; `SQLiteOpenHelper` for local storage; Toast-based validation feedback.
- Don't invent backend endpoints — the portal API (`/api/papers`, `/api/feature-request`) is the only server surface; auth and user data never leave the device.
