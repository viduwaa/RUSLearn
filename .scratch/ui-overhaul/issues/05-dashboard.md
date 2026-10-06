# 05 - Dashboard: greeting, snapshot hero, tile grid, favourites strip

Type: task
Status: open

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

- [ ] Dashboard reads: greeting → hero → tiles → recent, with one consistent visual language.
