# 02 - GPA calculator with prediction + subject tracking screens

Type: task
Status: resolved

## Comments

- Build-verified; device pass (grade pickers, live vs saved cards, status saves)
  pending the phone unlock — part of the combined verification pass.
- FR08 carries in the two-card design: "Saved CGPA" from the database versus
  "Current entries" live — future grades make the current card a projection.

## What

`ui/GpaCalculatorActivity` (programme + semester spinners, per-subject grade rows,
saved/current cards, credits bar, save button) and `ui/SubjectTrackingActivity`
(same row shape with status spinners auto-saved, summary of completed credits and
current count).

## Done

- `gradlew assembleDebug` passes; per-device pass checklist in PROJECT_STRUCTURE.md.
