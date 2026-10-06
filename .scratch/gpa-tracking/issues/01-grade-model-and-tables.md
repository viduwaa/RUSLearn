# 01 - GradePoint model + grade_records / subject_records tables

Type: task
Status: resolved

## Comments

- Portal's calculator semantics ported verbatim (points, Non-GPA Pass credits,
  bands, 120-credit target); DB v3 adds the two per-user tables.

## What

`model/GradePoint` (points map, `gpaOf` per semester, `overallOf` totals + bands),
DatabaseHelper v3 with `grade_records` and `subject_records` (+ per-user, per-programme
upserts and removal on "None").

## Done

- `gradlew assembleDebug` passes.
