# GPA calculator, prediction and subject tracking (M5)

Feature: FR07 (GPA/CGPA), FR08 (prediction), FR09 (subject tracking),
FR10 (semester info), FR11 (credit/progress tracking). Grading model replicates the
portal's calculator exactly (GradePoint static map, Non-GPA Pass/Fail semantics,
±0.01 tolerance, 120-credit degree, classification bands) per NFR09.

## Scope

- `model/GradePoint` — the grade→points table, band labels, and the two core
  functions: per-semester GPA and overall totals (Σcredits·points / Σcredits over
  graded subjects only; Non GPA Pass counts credits, Fail/None/None nothing).
- DatabaseHelper v3 — `grade_records` (user, programme, subject_code → grade) and
  `subject_records` (→ status: completed/current/upcoming). Rows are per user.
- `ui/GpaCalculatorActivity` — programme + semester dropdowns; subject rows with
  grade spinners (Non GPA rows use Pass/Fail/None); live semester + overall GPA
  cards ("current entries" vs "saved") acting as the FR08 prediction comparison;
  Save persists the current entries.
- `ui/SubjectTrackingActivity` — programme dropdown, per-subject status spinners
  (saved instantly), completed-credits summary vs 120 with a progress bar.

## Accepted

- Number formatting: 2 decimals via String.format, same as the portal (±0.01 agreed).
- No fragments/RecyclerView; ListView + adapter rows, RadioButton-free.
- Catalog comes from SubjectCatalogLoader (M1 asset); credits/credit totals and subject
  names are never duplicated into the database.

## Non-goals

- Charts, notifications, multi-device sync, export.
