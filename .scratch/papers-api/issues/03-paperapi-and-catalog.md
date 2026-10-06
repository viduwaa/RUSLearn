# 03 - PaperApi + bundled subject catalog + live debug test

Type: task
Status: resolved

## What

- `api/PaperApi.java`: `listPapers(PaperFilter)` builds the encoded query string with
  URL-encoding, `getFilterOptions()`, `sendFeatureRequest(String text)` (≤500 enforced at
  the UI, not here). Maps HTTP 429 and 5xx to clear IOException messages.
- `model/Subject.java` (code, name, credits, raw type "C"|"O"|"Non GPA").
- `data/SubjectCatalogLoader.java` reading `res/raw/subjects.json`
  (generated from the portal's `lib/subjectList.ts` with node --experimental-strip-types).
- Temporary Dashboard debug button: background `Thread` → PaperApi.listPapers(null) →
  Toast with count + first course code; error paths toast the message. Removed in M3.

## Done

- `gradlew assembleDebug` passes.
- With emulator/app running: toast reports a paper count matching the portal's "Showing X of Y";
  zero-result filters and airplane mode yield readable error toasts (NFR03).
