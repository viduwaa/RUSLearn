# 01 - Dashboard rebuild (FR12) + structure docs

Type: task
Status: resolved

## Comments

- Device-passed: session card (name/email + Sign out → back to login, prefs cleared,
  no crash), snapshot card live-updating from saved grades/statuses (GPA 3.25, band,
  6/120 credits bar, "Y1S1 3.25" line, "Current semester: Y1S1"), the full shortcut
  grid (every cell launches its screen), and the recent-favourites strip.

## What

DashboardActivity + activity_dashboard.xml rebuilt: session card with Sign out,
academic snapshot (GPA/band/credits/per-semester lines/current semester), a 2-column
shortcut grid, recent favourites (max 3). PROJECT_STRUCTURE.md (structure, data
sources, FR test matrix) and README.md (setup incl. the OAuth client steps) written.

## Done

- `gradlew assembleDebug` passes; docs in place.
