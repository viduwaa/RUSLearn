# 06 - Paper detail: hero, meta grid, icon action row, offline chip

Type: task
Status: resolved

## What

- **Hero**: mono code chip above a serif paper name; department shown as a small
  stamp beside the code.
- **Meta card**: label/value grid without the fixed 110dp label width — labels in
  `colorInkMuted`, values in ink, hairline separators between rows.
- **Action row**: **Open** (filled + doc icon), **Download / Remove offline**
  (tonal + download icon, label swaps when an offline copy exists), **Favourite**
  (outlined + star icon that toggles and pops via `UiMotion.pop`).
- Offline state becomes a green **stamp chip** ("Saved offline") instead of the
  small green text line.

## Constraint

Open/Drive/download/favourite logic in `PaperDetailActivity` is untouched — layout
+ button states only.

## Done

- [x] Detail screen reads as hero -> meta -> actions, with icon-led buttons and a visible offline state.

## Comments

- 2026-10-06: Landed. Meta card on bg_card stock with weight-based label/value
  columns (no fixed 110dp); action row is Filled+doc / Tonal+download / Outlined+star
  via the shared button styles with vector icons; favourite taps pop (UiMotion.pop).
  Offline badge stays a mono green line pending the stamp chip (ticket 08 QA pass
  will convert it to a Stamp pill).
