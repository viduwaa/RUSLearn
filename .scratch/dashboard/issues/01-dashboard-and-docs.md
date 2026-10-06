# 01 - Dashboard rebuild (FR12) + structure docs

Type: task
Status: resolved

## Comments

- Build-verified; the same device pass as M4/M5 covers the dashboard (session card,
  snapshot card, shortcuts, recent favourites).

## What

DashboardActivity + activity_dashboard.xml rebuilt: session card with Sign out,
academic snapshot (GPA/band/credits/per-semester lines/current semester), a 2-column
shortcut grid, recent favourites (max 3). PROJECT_STRUCTURE.md (structure, data
sources, FR test matrix) and README.md (setup incl. the OAuth client steps) written.

## Done

- `gradlew assembleDebug` passes; docs in place.
