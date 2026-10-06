# Project structure — where things go, where they come from

Reading order for anyone (human or agent) touching this repo:
`AGENTS.md` → `GLOSSARY.md` → the relevant `docs/adr/*` → this file.

## Top level

| Path | Role |
|------|------|
| `app/` | The Android Studio module (all app code lives here) |
| `docs/FOT_Academic_Companion.pdf` | The course brief: FR01–FR14, NFR01–NFR09, locked tech stack |
| `docs/practical_notes/` | The lecturer's sheets — the code-style reference (Activities, `findViewById`, `SQLiteOpenHelper`, Toasts, `ListView`+adapters) |
| `GLOSSARY.md` | Domain vocabulary; use these words in code and issues |
| `docs/adr/` | Architecture decisions — read the ones for your area first |
| `docs/agents/` | How coding agents consume the tracker + domain docs |
| `.scratch/<feature>/` | Issue tracker: `spec.md` + `issues/NN-<slug>.md` per feature |
| `AGENTS.md` | Agent conventions; the master file list for contributors (human, AI) |

## The app module (`app/src/main`)

```
java/com/ruslearn/fot/
├── ApiConfig.java? no — inside api/            ← API constants (see below)
├── api/         ApiConfig, RestClient, PaperApi ← the ONLY code that touches the network
├── auth/        GoogleAuth, JwtPayloadDecoder   ← FR01 Google sign-in (client-side, ADR-0001)
├── data/        DatabaseHelper, SessionManager, FilterPrefs, SubjectCatalogLoader,
│                DrivePaperFetcher               ← local storage + offline downloads (ADR-0002/0005)
├── model/       Paper, PaperFilter, FilterOptions, Subject, Program, GradePoint
└── ui/          Login, Dashboard, Papers, PaperDetail, Favourites, OfflinePapers,
                 PaperRequest, GpaCalculator, SubjectTracking ← one Activity per screen (ADR-0003)
```

- **`ui/` naming**: one screen = one `…Activity` + one `res/layout/activity_*.xml`.
  List rows use `item_*.xml` layouts (see `PaperAdapter`, `GradeRowAdapter`).
- **`data/DatabaseHelper`** owns ALL SQLite tables: `users`, `favourites`,
  `offline_papers`, `grade_records`, `subject_records` (+ per-user keys).
- `api/PaperApi` converts portal JSON to `model/` classes; where a filter (Exam Year)
  has no server parameter, the filter is applied locally like the portal web app does.

## Where data COMES FROM

| Data | Source | Where defined |
|------|--------|---------------|
| Past papers, requests, filter dropdown options | Portal REST API (live, read-only): `https://past-paper-portal.pages.dev/api/*` | `api/ApiConfig.java` |
| Subject/semester catalog (FR09/FR10 + GPA entry) | Bundled asset `app/src/main/res/raw/subjects.json`, converted from the portal's `lib/subjectList.ts` (2022 Handbook) | `data/SubjectCatalogLoader` |
| Favourites, grades, tracking statuses, offline copies | Local SQLite only, per signed-in user | `data/DatabaseHelper` |
| Grade→points, bands, 120-credit totals | Static in `model/GradePoint` — copied from the portal's GPA calculator (NFR09) | — |
| Google sign-in | Classic `GoogleSignIn` + the OAuth **Web client ID** in `res/values/strings.xml` (`web_client_id`) | `auth/GoogleAuth` |

## Conventions & house rules (see also AGENTS.md)

- Java 11 source/target, minSdk 24, target/compile 36; Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`).
- No Kotlin, no Compose, no fragments; `HttpURLConnection` + `org.json` only (ADR-0004).
- User-facing strings belong in `res/values/strings.xml`; colors in `values/colors.xml` (portal-anchored tokens).
- Every screen handles empty/error states visibly (progress bar, empty text, toasts) — NFR03.
- Network always happens on background threads, results posted via `runOnUiThread`.
- Milestone work flows: ticket in `.scratch/<feature>/` first, then implement, then verify on device, then commit.

## Verifying the app (FR01–FR14 walkthrough matrix)

| FR | Check | Expected |
|----|-------|----------|
| FR01 login | sign with a `tec.rjt.ac.lk` Google account → lands on dashboard | profile saved, dashboard shows name |
| FR01 gate | try a personal Google account | refuse toast, Google sign-out |
| FR02 browse | open papers screen | list loads (matches the portal's table) |
| FR03 search | type a subject code (e.g. `ICT 2308`) → Search | narrowed list |
| FR04 filter | pick study year/semester/department/past-paper year | live-refiltered list | 
| FR05 view | paper detail → Open paper | opens in browser/Drive viewer |
| FR06 favourites | star from detail → Favourites screen shows it; long-press removes | persisted across restarts |
| FR07 GPA calc | enter grades incl. a Non-GPA Pass → Save | same numbers as the web calculator (±0.01) |
| FR08 prediction | enter future grades → current card | projected value vs saved card |
| FR09 tracking | mark subjects Completed/Current/Upcoming | badges survive restart |
| FR10 semester info | tracking summary shows current semester + per-semester subjects | matches catalog |
| FR11 progress | tracking + dashboard credits bar | completed / 120 |
| FR12 dashboard | sign in, look at the dashboard | GPA, band, credits, current semester, favourites, shortcuts |
| FR13 requests | send a paper request | success toast; visible in the portal's admin list |
| FR14 offline | download a paper → airplane mode → open from Offline papers | PDF opens without network |
