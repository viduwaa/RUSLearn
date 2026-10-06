# 02 - Offline papers list + open without network (FR14)

Type: task
Status: in-progress

## Comments

- Code complete; build passes. Device pass (empty state + FileProvider open) waits
  for unlock + a successfully downloaded paper (needs web_client_id or a public paper).

## What

`ui/OfflinePapersActivity` + `activity_offline_papers.xml`: rows "code — name — saved
dd MMM yyyy"; tap opens the local PDF through `FileProvider` (com.ruslearn.fot.fileprovider
+ res/xml/file_paths.xml covering both storage fallbacks) with grant-read flags; long-press
confirms a delete of file + row. Manifest gains the provider registration. No in-app PDF
rendering — the device's PDF viewer does it.

## Done

- Tap opens the PDF offline (airplane-mode check belongs to the user's device demo).
