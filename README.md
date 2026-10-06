# RUSLearn — FOT Academic Companion

Android companion app for **RUSL Faculty of Technology** students: past papers (browse,
search, filter, open, favourites, offline copies), GPA/CGPA calculator with prediction,
subject & semester tracking and paper requests — built as the ICT 3219 (Mobile
Application Development) group project.

- **Stack (locked by the brief)**: Java, XML layouts, Android `appcompat`/`material`,
  REST API (the open-source [FOT Past Papers Portal](https://past-paper-portal.pages.dev)
  is the backend surface), PostgreSQL lives on the portal side; student data stays in
  **local SQLite** on the device.
- **Code style**: plain Activities + `findViewById`, `SQLiteOpenHelper`,
  `ListView` + adapters, Toasts — exactly what the practical sheets teach
  (`docs/practical_notes/`). One Activity per screen (see `docs/adr/`).
- Sign-in: Google account gated to the university workspace `tec.rjt.ac.lk` (client-side
  check, ADR-0001); nothing student-owned ever leaves the device.

## Build & run

1. Open the repo root in Android Studio (project = this folder; module = `app`).
2. Let Gradle sync (Kotlin DSL + version catalog, AGP 8.10.1, minSdk 24, targetSdk 36).
3. Run the `app` configuration on an emulator or a device.

Or from a terminal: `.\gradlew.bat assembleDebug` — the APK appears under
`app/build/outputs/apk/debug/`.

## One-time Google sign-in setup (needed before logging in in-app)

FR01 uses Google sign-in; the ID token is only issued when the app knows an OAuth client:

1. In [Google Cloud console](https://console.cloud.google.com/) pick the Google project you own.
2. Create an OAuth client of type **Android**: package `com.ruslearn.fot` plus your
   debug SHA-1 — get it with `.\gradlew.bat signingReport` (use the `debug` variant).
3. Create an OAuth client of type **Web application** in the same project; copy its
   **Client ID**.
4. Paste the **Web** client ID into `app/src/main/res/values/strings.xml` as
   `<string name="web_client_id">…</string>`.
5. On the "OAuth consent screen", add yourself and your teammates as test users; Google
   verification isn't needed for internal testing (a yellow unverified-app notice with
   an Advanced→Continue affordance will appear once — that's expected). Test users are
   only honoured while the consent status stays "Testing".
6. Add the `drive.readonly` scope to the consent screen's scopes list (used by the
   FR14 offline download; ADR-0005).
7. **APIs & Services → Library → enable "Google Drive API"** on the same project —
   without it the authenticated download fails with 403 "Google Drive API has not been
   used in project ..." (found live during testing).

Only `tec.rjt.ac.lk` accounts pass the gate; other Google accounts are refused with a
toast (try it — it's part of the FR01 test).

## Release the app (production build)

1. **Create the release key once** (never changes — every update must be signed with it):
   ```
   keytool -genkeypair -v -keystore ruslearn-release.jks -alias ruslearn -keyalg RSA -keysize 2048 -validity 10000
   ```
   Put `ruslearn-release.jks` in the repo root (or anywhere) and copy
   `keystore.properties.example` to `keystore.properties` with the real password +
   alias. Both stay uncommitted (`*.jks`, `keystore.properties` are git-ignored) —
   **back up the .jks + password somewhere safe: losing them means no more updates.**
2. **Register the release fingerprint with Google sign-in** (FR01 dies in release builds
   without it): run `.\gradlew.bat signingReport` → copy the SHA-1 listed under the
   `release` variant → Google Cloud console → the Android OAuth client → add that
   SHA-1 (the debug one you already added stays).
3. **Bump versions** in `app/build.gradle.kts` (`versionCode` +1 per release,
   `versionName` e.g. "1.1") for every new build.
4. Optional but recommended before distribution: a real launcher icon
   (Android Studio → right-click `res` → New → Image Asset → launcher icons
   adaptive + legacy) to replace the placeholder teal square.
5. **Build**:
   - Direct APK (share/sideload): `.\gradlew.bat assembleRelease` →
     `app/build/outputs/apk/release/app-release.apk` (signed, installable).
   - Play Store bundle (only if you ever publish there): `.\gradlew.bat bundleRelease` →
     `app/build/outputs/bundle/release/app-release.aab`.
6. **Verify the signed build on a device**: `adb install -r app\build\outputs\apk\release\app-release.apk`,
   then run the FR test matrix in `PROJECT_STRUCTURE.md` — the login gate and Drive
   download must both work there (debug and release have different SHA-1s and separate
   app data).
7. Data hygiene for release: the app is fully offline for student data and never
   uploads anything, so no privacy policy is needed for plain distribution; if it
   ever goes to the Play Store, add one plus store listing assets.

## Repository layout

See `PROJECT_STRUCTURE.md` — the one-pager of where things live, where they come from,
and the house rules.

## Issue tracker

Feature specs + numbered tickets live under `.scratch/<feature>/` (local markdown —
see `docs/agents/issue-tracker.md`). Domain vocabulary: `GLOSSARY.md`;
architecture decisions: `docs/adr/`.

## Feature checklist (FR01–FR14)

See the test matrix in `PROJECT_STRUCTURE.md` → "Verifying the app". Each milestone in
git history maps to a subset of the FRs; migration order: login → papers →
favourites/offline/requests → GPA/tracking → dashboard.
