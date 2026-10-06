# 01 - Design tokens: fonts, colours, night mode, theme wiring

Type: task
Status: resolved

## What

- Bundle **IBM Plex Sans / Serif / Mono** TTFs (the weights listed in spec §3.1)
  in `res/font/` with the `OFL.txt` licence alongside.
- Rewrite `values/colors.xml` to the "Paper & Ink" tokens from the spec
  (`colorPaper/colorCard/colorInk/colorInkMuted/colorHairline/colorFaculty/...`),
  add `values-night/colors.xml`, retire the unused `colorGpa*` tokens.
- Rewrite `values/themes.xml` so the M3 slots are wired properly
  (`colorPrimary=colorFaculty`, `colorSurface=colorCard`, `android:colorBackground=colorPaper`,
  status-bar treatment) and add the shared `textAppearance`, `shapeAppearance`,
  button/card/chip/dropdown styles from spec §3.4–3.5.
- Existing layouts must still compile against the renamed tokens — update
  references as part of this ticket so `assembleDebug` never goes red.

## Done

- [x] `gradlew assembleDebug` passes with the new tokens in both modes.
- [x] App runs in dark mode with every surface/text correct (no hardcoded whites left).

## Comments

- 2026-10-06: Landed. 7 static TTFs (Sans 400/500/600, Serif 600/700, Mono 400/500)
  in `res/font/` with `plex_sans/serif/mono.xml` families; static weights chosen over
  the Sans VF so API 24/25 render true weights (VF default-instances to faux-bold).
  Licence moved to repo root `FONT_LICENSE.txt` — `res/font/` only accepts
  .xml/.ttf/.ttc/.otf. `values/colors.xml` rewritten to Paper & Ink tokens with
  legacy names aliased (compiles mid-migration); `values-night/` added; `themes.xml`
  wires all M3 slots (colorPrimary=colorFaculty, surfaces, outline, status bar,
  type defaults, component defaults). Dead `colorGpa*` tokens retired (S12);
  `colorGpaGreen` aliased to `colorStampGreen` for the one detail-screen reference.
