# 02 - Offline papers list + open without network (FR14)

Type: task
Status: resolved

## Comments

- Device-passed: empty state renders; the open/delete cycle needs an actually
  downloaded paper — left as the user's first-run demo after adding web_client_id
  (FR14's authenticated layer), or a deliberately public Drive file (layer 1).

## What

`ui/OfflinePapersActivity` + `activity_offline_papers.xml`: rows "code — name — saved
dd MMM yyyy"; tap opens the local PDF through `FileProvider` (com.ruslearn.fot.fileprovider
+ res/xml/file_paths.xml covering both storage fallbacks) with grant-read flags; long-press
confirms a delete of file + row. Manifest gains the provider registration. No in-app PDF
rendering — the device's PDF viewer does it.

## Done

- Tap opens the PDF offline (airplane-mode check belongs to the user's device demo).
