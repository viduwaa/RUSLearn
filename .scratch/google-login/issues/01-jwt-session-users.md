# 01 - JWT decode + session + users table

Type: task
Status: resolved

## What

- `auth/JwtPayloadDecoder.decode(idToken)` → org.json `JSONObject` of the JWT payload
  (android.util.Base64 URL-safe; java.util.Base64 is API 26+, minSdk is 24).
- `data/SessionManager` — sign in/out, read current email/name from SharedPreferences.
- `data/DatabaseHelper` extends SQLiteOpenHelper with `users` (email PK, name, last_login)
  and `saveUser` upsert per Practical-05 style.

## Done

- `gradlew assembleDebug` passes; switching accounts works through the logout loop.
