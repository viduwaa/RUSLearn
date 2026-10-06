# 01 - Favourites screen (FR06)

Type: task
Status: in-progress

## Comments

- Code complete; build passes. Device pass (saved-papers list, remove flow) pending
  the user unlocking the attached phone; the seeded BPT 1201 favourite should appear.

## What

`ui/FavouritesActivity` + `activity_favourites.xml`: ListView of saved papers
(PaperAdapter reuse — star always filled), tap → PaperDetailActivity (open/download
still available), long-press → AlertDialog → remove → toast (Practical-05 pattern).
Empty state text points to the papers screen.

## Done

- The BPT 1201 favourite saved in M3 appears, opens the detail and removes cleanly.
