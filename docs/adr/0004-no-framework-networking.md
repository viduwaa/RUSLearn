# ADR-0004: HttpURLConnection + org.json on background threads

- Status: accepted
- Date: 2026-10-05
- Deciders: group (grilled session)

## Context

FR02–FR05 and FR13 need REST calls of a simple request/response shape against the
portal API. The practical notes teach no networking library, the locked stack says
"REST API + JSON", and code evaluation rewards plain, explainable Java.

## Decision

All portal communication uses `java.net.HttpURLConnection` + `org.json`, wrapped by two
small classes (`api/RestClient.java`, `api/PaperApi.java`). Network calls always run on
background threads; activities post the result to the UI thread with
`runOnUiThread`. No Retrofit/OkHttp/Volley, no RxJava/coroutines.

## Consequences

- Zero extra dependencies to explain in the viva; the network code is ~250 lines total.
- 429 rate limits and server errors surface as clear `IOException` messages shown by toasts.
- Malformed JSON is rethrown as `IOException` inside `PaperApi`, so UI code catches only `IOException`.
- Manual reconnect/retry logic is out of scope; loading is user-initiated (search/filter buttons), which the rate limiter tolerates.
