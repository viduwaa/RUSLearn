# 01 - Add RestClient and ApiConfig

Type: task
Status: resolved

## What

Create `api/ApiConfig.java` (portal base URL + endpoint paths — the ONLY place URLs live)
and `api/RestClient.java`, a small `java.net.HttpURLConnection` wrapper with
`get(String url)` and `post(String url, String jsonBody)` returning a `RawResponse`
(status code + body string). Connect timeout 10s, read timeout 15s, always `setDoInput`.
Document the ADR-0004 constraint in the class comment: callers must invoke from a
background thread.

## Why

Every network call in the app flows through here; keeping it tiny keeps the graded code simple.

## Done

- `gradlew assembleDebug` passes.
- RestClient requests carry `Accept: application/json`; POST carries `Content-Type: application/json`.
