# 07 - GPA calculator + subject tracking: stat blocks, stamps, dropdown rows

Type: task
Status: open

## What

- **GPA**: labelled Programme/Semester exposed dropdowns; Saved vs Current CGPA as
  two stat blocks inside one hero card, each with its **band stamp** and, when both
  exist, a small delta arrow (▲/▼ in stamp colour); animated credits progress.
- **Subject rows** (shared by both screens): two comfortable lines — line 1: mono
  code + subject name; line 2: mono credits + an exposed-dropdown grade/status
  (replaces the 120dp spinner squeezed onto one line, audit per-screen note).
  The dropdown gets the **clean look**: compact outlined pill — faculty-tinted fill,
  10dp radius, faculty text, chevron — matching the Papers filter dropdowns
  (`.ddown` in `preview.html` §05/§07), 36dp min height, grade text in mono.
- **Tracking**: progress hero with Completed/Current/Upcoming summary stamps;
  status dropdowns per row with the three statuses.
- Sticky Save button becomes `.Filled` with a save icon; grades-save toast stays
  (house rule) but the button briefly shows the saved state.

## Constraint

`GradePoint` math, DatabaseHelper writes and FR07/FR08 parity (±0.01 vs the web
calculator) are untouched.

## Done

- [ ] GPA + Tracking screens use the same hero/stat/stamp language; rows are readable at 360dp width.
