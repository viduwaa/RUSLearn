# 02 - GPA calculator with prediction + subject tracking screens

Type: task
Status: resolved

## Comments

- Device-passed: grades A+/B/Pass on Y1S1 → current 3.25 "Second Class (Lower)",
  6/120 credits (Non-GPA Pass counted); Save → saved card matches; per-user rows
  read back from SQLite after relaunch. Tracking: statuses persisted (ICT 1111
  Completed, CMT 1205 Current), summary + "Currently taking" counters correct, and
  the dashboard derives "Current semester: Y1S1" + the GPA snapshot from them.
- One fix en route: the tracking summary now initializes on programme load
  (it previously stayed empty until the first pick).

## What

`ui/GpaCalculatorActivity` (programme + semester spinners, per-subject grade rows,
saved/current cards, credits bar, save button) and `ui/SubjectTrackingActivity`
(same row shape with status spinners auto-saved, summary of completed credits and
current count).

## Done

- `gradlew assembleDebug` passes; per-device pass checklist in PROJECT_STRUCTURE.md.
