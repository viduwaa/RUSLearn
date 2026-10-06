# 02 - Toolbar + up-navigation on every sub-screen

Type: task
Status: resolved

## What

- Create `layout/view_toolbar.xml` — a `MaterialToolbar` styled `Toolbar.RUSLearn`
  (Plex Serif title, `colorPaper` background) — and include it at the top of
  Papers, Detail, Favourites, Offline, Request, GPA and Tracking layouts
  (adjust each root's top constraint/padding).
- In each of those activities: `setSupportActionBar(toolbar)` +
  `setDisplayHomeAsUpEnabled(true)`; handle `android.R.id.home` with `finish()`
  (manifest `parentActivityName` not required — dashboard is the only hub, but
  adding it costs one line and helps the system back stack).
- Delete the now-invisible `setTitle(...)` confusion: the toolbar carries the title.

## Why

Audit S4: with `NoActionBar` and no toolbar, `setTitle()` renders nothing — six
screens currently open with no title and no back affordance beyond the gesture.

## Split

The **bottom navigation bar** (Home · Papers · GPA · Tracking) is ticket
**02b** below — same navigation theme, separate review so the toolbar pass stays small.

## Done

- [ ] Every sub-screen shows its title and an up arrow that returns to the dashboard.

## Comments

- 2026-10-06: Landed via `ToolbarActivity` base class (bindToolbar + up=finish()) and
  `view_toolbar.xml` include on Papers, Detail, Favourites, Offline, Request, GPA and
  Tracking. Papers layout also gained the mono result-count line (`papers_count_format`)
  since it rides the same file. parentActivityName not added — finish() covers it.
