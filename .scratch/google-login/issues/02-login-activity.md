# 02 - LoginActivity with domain gate

Type: task
Status: in-progress

## Comments

- Code complete; the final human check (domain account in / personal account refused)
  waits on the user's Google Cloud OAuth web client ID in strings.xml and their
  university account. Tapping Sign in with the placeholder client id opens Google's
  account picker without crashing (verified on device).

## What

`ui/LoginActivity.java` + `activity_login.xml` (ConstraintLayout, SignInButton widget),
launcher moved to it in the manifest. Logic: if already signed in → Dashboard
immediately; else classic Google sign-in intent → `onActivityResult` → decode ID token →
`GoogleAuth.isUniversityAccount` (email suffix or `hd` claim equal to `tec.rjt.ac.lk`) →
`DatabaseHelper.saveUser` + `SessionManager.signIn` → Dashboard. Non-university account
→ toast + local Google sign-out. ApiException 12500 (missing/invalid web client id)
surfaces a configuration-specific message.

## Done

- Emulator/device: domain account passes the gate and lands on the dashboard with its
  name; a personal Google account is refused with the university-account toast.
