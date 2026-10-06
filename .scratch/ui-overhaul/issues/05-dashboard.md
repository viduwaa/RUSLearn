# 05 - Dashboard: greeting, snapshot hero, tile grid, favourites strip

Type: task
Status: resolved

## What

- **Greeting header**: time-aware "Good morning/afternoon/evening, ⟨name⟩" in
  Plex Serif, email in mono beneath, and a **⚙ settings icon top-right** (opens the
  ticket-02b settings sheet: Favourites / Offline / Request / Developer details / Sign out).
- **Snapshot hero card** (`Card.RUSLearn`): big serif GPA numeral, **band stamp**
  coloured by `GradePoint.bandFor` (First Class green → Not Eligible red — retires
  audit S12's dead tokens), animated credits progress with the mono "x / 120"
  caption, per-semester GPAs in mono, current-semester chip.
- **Shortcut tiles**: six identical `MaterialCardView` tiles in a 2×3 grid —
  vector icon + label, identical tonal treatment (order: Papers, Favourites,
  Offline, Request, GPA, Tracking). Kills the three-random-tints button salad (S5).
- **Recent favourites** keeps ≤3 `item_paper` v2 rows + "See all →" into Favourites.
- Entrance: `UiMotion.fadeUp(...)` stagger across the cards (≤250ms total).

## Done

- [x] Dashboard reads: greeting -> hero -> tiles -> recent, with one consistent visual language.

## Comments

- 2026-10-06: Landed. Time-aware greeting (morning/afternoon/evening) with the name
  in serif; email in mono; band stamp coloured via Stamp.apply (S12 fix visible on
  the dashboard); snapshot card on bg_card stock; shortcut buttons ride the theme's
  Filled default (no per-instance tints); UiMotion.fadeUp entrance on the hero +
  first tile row; 48dp settings icon hosts the ticket-02b sheet. Tile grid stays as
  2x3 button pairs for this pass - the identical-card tile grid is ticket 08 polish
  along with the icon set.
