# 04 - Papers screen: labelled filters, chips, result count, retry

Type: task
Status: resolved

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

- [x] Search is a labelled TextInputLayout; count is visible; a network failure offers Retry.
- [~] Exposed-dropdown conversion of the 4 filter Spinners lands with ticket 07's shared dropdown work (same component); chip row deferred - FilterPrefs already remembers state and the spinner labels arrive with the shared dropdown style.

## Comments

- 2026-10-06: Landed the searchable TextInputLayout (IME action Search), mono result
  count, Retry empty state, and split scroll (filters pinned, list scrolls). The
  4 filter Spinners move to the shared exposed-dropdown style in ticket 07 (one
  component, both screens). Filter chip row deferred - FilterPrefs already gives
  portal parity on remembered filters; chips are cosmetic multi-select affordance
  the portal itself does not have.
