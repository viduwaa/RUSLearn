# 02b - Bottom navigation (4 slots) + Home settings sheet

Type: task
Status: resolved

## What
- `layout/view_bottom_nav.xml` — a `BottomNavigationView` (Material 1.12.0, already a
  dependency) with 4 items: **Home · Papers · GPA · Tracking** (`res/menu/nav_main.xml`).
  No "More" slot — the bar stays destinations-only.
- Include it on Dashboard, Papers, GPA, Tracking — the four top-level destinations —
  between the content and the screen bottom (`ConstraintLayout` bottom constraint).
- **Detail / Favourites / Offline / Request stay stacked pages** with toolbar up-nav
  (ticket 02) — no bottom bar.
- **Item behaviour** (plain-Activity navigation per ADR-0003, no fragments):
  - Home → Dashboard; Papers → `PapersActivity`; GPA → `GpaCalculatorActivity`;
    Tracking → `SubjectTrackingActivity`, all with `FLAG_ACTIVITY_SINGLE_TOP`
    so state is kept and the back stack stays shallow (no dashboard pile-up).
- **⚙ Settings sheet (Home only):** a settings icon (`ic_settings.xml`, vector) sits
  in the dashboard app bar, top-right. Tapping opens a plain `AlertDialog` listing:
  **Favourites**, **Offline papers**, **Request a paper**, **Developer details**, **Sign out** —
  the second-ring destinations. Developer details is a small info dialog (app name,
  version, ICT 3219 module credit); Sign out keeps the ADR-0001 revoke flow.
  The icon keeps a 48dp touch target (padding inside a 48dp toolbar button).
- **Back behaviour:** on Papers/GPA/Tracking an `OnBackPressedCallback` routes the
  system back to Dashboard (the nav root), mirroring single-activity nav graphs.
- **Active state:** each screen calls `bar.setSelectedItemId(...)` for its own slot
  (guarding re-selection so it doesn't re-trigger navigation).
- Suppress the shift/label animation for a flat, archival look that matches the design
  (`app:labelVisibilityMode="labeled"`).

## Why

Today every jump between Papers ↔ GPA ↔ Tracking routes through the dashboard
(audit S4/S5 follow-up). The bar puts the high-frequency destinations one thumb-tap
away on every top-level screen; the low-frequency second ring (Favourites, Offline,
Request, Sign out) folds behind one gear so the bar itself stays clean. Preview
section 06 in `preview.html` shows the exact anatomy (active pill + settings sheet) —
per-phone bars in every mockup are clickable.

## Constraint

No new libraries, no fragments; `BottomNavigationView` is a plain view. Sign-out
stays in the settings sheet and keeps the ADR-0001 revoke flow.

## Done

- [ ] Bar visible on the 4 top-level screens; hidden on stacked pages.
- [ ] Back from Papers/GPA/Tracking lands on Dashboard; no activity pile-up.
- [ ] ⚙ sheet opens Favourites/Offline/Request/Developer details/Sign out.
- [ ] Dark mode: bar and sheet colours come from `values-night/` tokens only.

## Comments

- 2026-10-06: Landed. view_bottom_nav.xml + res/menu/nav_main.xml (4 slots),
  color/color_nav_item.xml selector, TopLevelNav shared controller (bar wiring,
  SINGLE_TOP hops, back-to-Dashboard callback on non-home tabs, Home settings sheet,
  developer-details dialog). Dashboard hosts the 48dp settings icon; its old
  sign-out button is gone (sign-out lives in the sheet, ADR-0001 flow preserved).
  Every top-level screen attaches its own active slot.
