# Papers API plumbing (M1)

Feature: the app-side client for the portal API plus bundled subject catalog — the substrate for FR02–FR05 (browse/search/filter/view), FR13 (requests) and FR09–FR11 (tracking uses the catalog).

## Scope

- `api/ApiConfig.java` — the single source of portal URLs.
- `api/RestClient.java` — tiny HttpURLConnection wrapper: GET/POST, timeouts, response reading, 429/5xx surfaced as messages.
- `api/PaperApi.java` — papers list (`/api/papers` w/ filters), filter options (`options=true`), feature request POST. org.json parsing.
- `model/` — `Paper` (full portal record incl. `driveFileId()`), `PaperFilter`, `FilterOptions`, `Subject`.
- `data/SubjectCatalogLoader.java` — reads `res/raw/subjects.json` (generated from the portal's `lib/subjectList.ts`) into memory once.
- Temporary debug button on the Dashboard to prove the plumbing live ("Load papers" toast with count + first code). Removed in M3.

## Accepted

- ADR-0004 (to be written with this milestone): HttpURLConnection + org.json on background threads via Thread + runOnUiThread; no third-party networking libraries.
- GradePoint static map deferred to M5.

## Non-goals

- No caching beyond SharedPreferences filter-remembering (M3), no Retrofit/OkHttp, no polling.
