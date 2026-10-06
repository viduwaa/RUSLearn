# 03 - Shared components: card stock, paper rows, empty states, stamps, motion

Type: task
Status: resolved

## What

- `Card.RUSLearn` style is landed with ticket 01; here build the reusable
  **include-layouts**: `view_empty_state.xml` (icon-in-circle, title, support
  text, optional Retry button) and `view_stamp.xml` (small tinted pill whose
  colour is set in code from `GradePoint.bandFor`).
- **`item_paper.xml` v2**: card row (hairline stroke, 16dp radius) — mono code
  chip, Plex Sans paper name, mono meta line, star with ripple + pop animation;
  root `wrap_content` (fixes the `match_parent` row bug); ≥48dp row height.
- **`UiMotion`** helper (ui/ package, ~30 lines): `fadeUp(View…)` staggered
  entrance via `ViewPropertyAnimator`, `pop(View)` for the star, used by the
  screen tickets that follow.
- Wire the new rows into `PaperAdapter`, `FavouritesActivity`, `OfflinePapersActivity`
  and the dashboard's recent strip; empty states adopt `view_empty_state` with
  the right CTA ("Browse papers").

## Done

- [x] Paper rows are card-like with visible press feedback; empty lists offer a CTA.

## Comments

- 2026-10-06: Landed. item_paper v2 (bg_card, mono code + sans name + mono meta,
  star in a 48dp FrameLayout target, root wrap_content), view_empty_state include
  (icon circle + title + CTA button; used by Favourites/Offline with "Browse papers"
  and Papers with "Retry"), view_stamp + bg_stamp pill, Stamp.java (band to colour,
  alpha-composited tint over card), UiMotion.java (fadeUp stagger + star pop, both
  <=250ms). PaperAdapter stars pop on tap.
