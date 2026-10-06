# 01 - Favourites screen (FR06)

Type: task
Status: resolved

## Comments

- Device-passed: saved row renders; long-press dialog showsRemove/Cancel and the
  removal cleared the row ("No favourite papers saved yet" empty state);
  re-favourite from detail re-populated it.

## What

`ui/FavouritesActivity` + `activity_favourites.xml`: ListView of saved papers
(PaperAdapter reuse — star always filled), tap → PaperDetailActivity (open/download
still available), long-press → AlertDialog → remove → toast (Practical-05 pattern).
Empty state text points to the papers screen.

## Done

- The BPT 1201 favourite saved in M3 appears, opens the detail and removes cleanly.
