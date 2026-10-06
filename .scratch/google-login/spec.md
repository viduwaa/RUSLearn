# Google login (M2)

Feature: FR01 — students sign in with their university Google account. Purely
client-side per ADR-0001; no backend changes.

## Scope

- `ui/LoginActivity.java` (new launcher screen): classic `GoogleSignIn` API, ID-token
  payload decoded by `auth/JwtPayloadDecoder`, domain-gated to `tec.rjt.ac.lk`.
- `data/SessionManager.java` — SharedPreferences-backed current-user record.
- `data/DatabaseHelper.java` — created with the `users` table + `saveUser` upsert;
  first login = registration, later logins = login.
- `auth/GoogleAuth.java` — shared Google client build + domain check helper.
- Temporary logout button on the dashboard (removed in M6 redesign).
- Prerequisite: an OAuth **Web application** client ID (strings.xml `web_client_id`) with
  the debug/release SHA-1 registered in the user's Google Cloud project.

## Accepted

- Signature verification is intentionally skipped (ADR-0001): the token never leaves the
  app process, so there is no trusting of any transport.
- `GoogleSignIn` classic API (deprecated but stable) for course-era simplicity.

## Non-goals

- Passwords, email verification flows, profile photos, roles/tokens, server sessions.
