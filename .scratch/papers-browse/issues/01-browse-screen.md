# 01 - Filter prefs + papers list UI

Type: task
Status: resolved

## Comments

- Verified on device: list renders live papers row-by-row (code/name/meta/star);
  dropdowns populate from GET /api/papers?options=true.

## What

- `data/FilterPrefs` (incl. the separate exam-year key) and `ui/PapersActivity` +
  `activity_papers.xml` (search row, 2x2 filter grid, ProgressBar, empty TextView,
  ListView) + `ui/PaperAdapter`/`item_paper.xml` (`ViewHolder` pattern).
- Dashboard's M1 probe button replaced by the real "Browse past papers" entry.

## Done

- `gradlew assembleDebug` passes; live list verified on the connected device.
