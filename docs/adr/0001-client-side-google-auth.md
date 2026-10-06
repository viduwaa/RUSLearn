# ADR-0001: Client-side Google Sign-In, no custom backend

- Status: accepted
- Date: 2026-10-05
- Deciders: group (grilled session)

## Context

FR01 requires students to register and log in with valid credentials. The existing portal
(`past-paper-portal.pages.dev`) has no user accounts at all, and the spec forbids inventing
backend surfaces beyond the portal API. NFR04 wants appropriate security without a server
round-trip story.

## Decision

Use the classic `GoogleSignIn` API (`com.google.android.gms:play-services-auth`) purely
client-side:

1. Request an ID token for a configured OAuth Web client.
2. Parse the ID token's payload in the app (email, name, `hd`) and verify `hd`/email domain is `tec.rjt.ac.lk`.
3. Upsert the profile into a local `users` SQLite table — first login acts as registration, later logins as login.
4. Store the signed-in profile in SharedPreferences; no session token leaves the device.

## Consequences

- Zero backend changes; the papers + feature-request APIs remain the only server surfaces.
- No password handling anywhere (NFR04 satisfied by construction — university Google account owns the credentials).
- ID token originates from Google Play Services, not from any transport, so local trust is justified; there is no custom endpoint to impersonate.
- If a lecturer later demands server-verified time, a tiny verify endpoint can be added to the portal without touching the app data model.
