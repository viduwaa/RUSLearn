# GLOSSARY.md

Domain vocabulary for RUSLearn — FOT Academic Companion. Use these exact terms in code, issues, specs and docs.

## People & identity

- **Student** — the app user; a Faculty of Technology member whose university Google account is on the `tec.rjt.ac.lk` domain (a **university account**).
- **Google login** — FR01 sign-in via the classic `GoogleSignIn` API; the ID token's `email` and `hd` claims are the valid credentials. No custom backend ever sees a password or token.
- **Profile** — the row in the local `users` table created on first Google login ("registration") and reused on later logins.

## Studies

- **Department** — a Faculty of Technology programme. Codes: `ICT` (Information & Communication Technology), `ITT` (treated as equivalent to ICT), `CMT`, `CML` (Common — present for every department), `BPT`, `EET`, `FDT`, `MTT`. Filter dropdowns render the live portal values verbatim, which include a few stray codes (e.g. `CMT1009`) — mirror them as-is.
- **Subject** — a course unit: `code` (e.g. `ICT 2308`), `name`, `credits`, `type` (one of `Core`, `Optional`, `Non GPA`). The full Subject Catalog (all departments × all semesters, per the 2022 Handbook) ships inside the app.
- **Non GPA subject** — a subject graded Pass/Fail. It is excluded from GPA calculations, but a Pass adds its credits to the completed-credit total.
- **Semester** — `1st Semester` / `2nd Semester` (also legacy `Semester I` / `Semester II` rows exist); the bundled catalog keys semesters `Y1S1`…`Y4S2`.
- **Study Year** — `1st Year` … `4th Year` (the live portal's wording); the `academic_year` column of a paper.
- **Exam Year** — the calendar year a particular past exam was held; the `exam_year` column of a paper (`"2018"`…). Study Year ≠ Exam Year.
- **Semester GPA** — weighted by credits over graded subjects of one semester. **Overall GPA** (CGPA) — weighted over all graded subjects of the programme There will be ±0.01 rounding like the portal.

## Papers

- **Past paper** — one exam PDF record from the portal's `past_papers` table: `file_name`, `view_url` (Drive view link), `preview_url`, `department_code`, `department_name`, `academic_year`, `semester`, `exam_year`, `course_code`, `course_name`.
- **Portal** — the existing web app (https://past-paper-portal.pages.dev, repo `C:\Projects\past-paper-portal`). The app's only server surface.
- **Portal API** — `GET /api/papers?options=true` → filter dropdowns; `GET /api/papers?year&semester&department&courseCode&searchQuery` → filtered paper list; `POST /api/feature-request` → paper request (≤500 chars). Papers are rate-limited per IP; handle 429 with a toast.
- **Drive file ID** — the id embedded in a paper's `view_url` (`/file/d/<FILE_ID>/view`); used to build download links (`https://drive.google.com/uc?export=download&id=<FILE_ID>`).
- **Favourite** — a paper the student saved; stored only in local SQLite.
- **Paper request** — a student-submitted request for a missing paper (FR13) via the portal's feature-request endpoint.
- **Offline paper** — a paper PDF whose bytes were fetched to app-private storage; openable with no network (FR14). Download attempts are layered: public Drive link → Drive API with the signed-in user's OAuth token → graceful "open online" toast.
- **Domain-restricted paper** — a Drive paper shared only with the `tec.rjt.ac.lk` workspace domain. The app opens drive links in the device's browser, where the student's university Google session grants access; in-app programmatic downloads need the Drive-API fallback (ADR-0001 context).

## Grades

- **Grade** — `A+`, `A`, `A-`, `B+`, `B`, `B-`, `C+`, `C`, `C-`, `D+`, `D`, `E` for GPA subjects; `Pass`, `Fail`, `None` for Non GPA subjects. Grade points: A+/A=4.0, A-=3.7, B+=3.3, B=3.0, B-=2.7, C+=2.3, C=2.0, C-=1.7, D+=1.3, D=1.0, E=0.0.
- **GPA band** — ≥3.70 First Class; 3.30–3.69 Second Class (Upper); 3.00–3.29 Second Class (Lower); 2.00–2.99 General; below 2.00 Not Eligible.
- **Degree credits** — the programme totals 120 credits; progress = completed credits / 120.
- **GPA prediction** — what-if grades for current/future subjects showing the effect on GPA/CGPA before real results arrive (FR08).

## App

- **RUSLearn** — this Android app; course module ICT 3219.
- **Screen** — one plain Activity with one XML layout; navigation is explicit `Intent`s (ADR-0003).
- **App DB** — the app's local `SQLiteOpenHelper` database holding users, favourites, grades, subject records and offline-paper metadata. Never synced to any server.

## References

- Spec: `docs/FOT_Academic_Companion.pdf` (FR01–FR14, NFR01–NFR09).
- Portal source of truth: `C:\Projects\past-paper-portal` (`lib/db.ts`, `lib/subjectList.ts`, `app/api/`).
