# 02 - Offline papers list + open without network (FR14)

Type: task
Status: resolved

## Comments

- Device-passed END-TO-END: the authenticated Drive-API download returned 200/%PDF
  (after enabling the Drive API on the OAuth project), the row landed, and the
  stored PDF opened through FileProvider with AIRPLANE MODE ON (Drive PdfViewer,
  real BPT 1201 paper). Airplane mode restored after the test.

## What

`ui/OfflinePapersActivity` + `activity_offline_papers.xml`: rows "code — name — saved
dd MMM yyyy"; tap opens the local PDF through `FileProvider` (com.ruslearn.fot.fileprovider
+ res/xml/file_paths.xml covering both storage fallbacks) with grant-read flags; long-press
confirms a delete of file + row. Manifest gains the provider registration. No in-app PDF
rendering — the device's PDF viewer does it.

## Done

- Tap opens the PDF offline (airplane-mode check belongs to the user's device demo).
