# Favourites, offline papers, paper requests (M4)

Feature: FR06 (favourites screen), FR14 (offline list + open without network),
FR13 (paper request form). Builds on the M3 database tables (ADR-0002).

## Scope

- `ui/FavouritesActivity` — saved papers list (PaperAdapter reuse), tap → detail,
  long-press → confirm-remove (Practical-05 interaction pattern).
- `ui/OfflinePapersActivity` — downloaded copies with save date; tap → open the PDF
  through FileProvider (works in airplane mode); long-press → confirm-remove (file +
  row).
- `ui/PaperRequestActivity` — 500-character limited request form, POST the portal's
  feature-request endpoint (FR13), explicit success/error toasts.
- Manifest: FileProvider with res/xml/file_paths.xml for the offline folder.
- Dashboard gets the three new entries.

## Non-goals

- In-app PDF rendering: offline copies open with the device's PDF/viewer app.
- Any account-string on records (device-local only).
