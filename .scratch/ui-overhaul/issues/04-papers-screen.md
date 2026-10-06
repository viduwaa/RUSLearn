# 04 - Papers screen: labelled filters, chips, result count, retry

Type: task
Status: open

## What

- Search becomes a `TextInputLayout` (search icon, IME action Search; the button
  stays for the practicals' explicit-Action habit).
- The four `Spinner`s become **labelled exposed dropdowns** (`Dropdown.RUSLearn`):
  Study year, Semester, Department, Past-paper year — 2×2, each with a floating label.
- Add a **result-count line** in Plex Mono ("128 papers") above the list and an
  **active-filter chip row** with a "Clear all" chip (clears `FilterPrefs` + all
  dropdowns, one `refresh()`).
- Error path gets the `view_empty_state` with a **Retry** button wired to
  `loadInitial()`/`refresh()`; empty path gets the friendly CTA variant.
- Loading keeps the centred spinner but the list dims slightly while refreshing.

## Constraint

FilterPrefs behaviour, exam-year local filtering and portal parity are untouched —
this ticket re-skins and adds state UI only.

## Done

- [ ] Filters are labelled and clearable; count is visible; a network failure offers Retry.
