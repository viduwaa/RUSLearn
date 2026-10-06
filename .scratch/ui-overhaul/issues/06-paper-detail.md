# 06 - Paper detail: hero, meta grid, icon action row, offline chip

Type: task
Status: open

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

- [ ] Detail screen reads as hero → meta → actions, with icon-led buttons and a visible offline state.
