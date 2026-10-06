# ADR-0005: Layered Drive download for offline papers

- Status: accepted
- Date: 2026-10-05
- Deciders: group (grilled session)

## Context

FR14 wants past papers available offline. Papers live on Google Drive, and most are
shared only with the `tec.rjt.ac.lk` workspace — a plain HTTP download of those files
returns an HTML interstitial, not the PDF.

## Decision

`data/DrivePaperFetcher` tries, in order:

1. **Public link** — `https://drive.google.com/uc?export=download&id=<FILE_ID>`; accepted
   only when the response body starts with `%PDF`.
2. **Drive API** — `https://www.googleapis.com/drive/v3/files/<id>?alt=media` with the
   signed-in student's OAuth access token (`drive.readonly` scope). The scope is requested
   once at Google sign-in (GoogleAuth.options), so later token use is silent.
3. **Graceful failure** — a clear toast pointing the student to "open online" with their
   university account. No state changes on failure.

Downloaded PDF bytes go to app-specific external storage (`papers/` folder, no runtime
permission needed) and a metadata row lands in the local `offline_papers` table.

## Consequences

- The happy path needs no portal/backend changes.
- If the university admin blocks drive.readonly consent, FR14 degrades to the
  documented failure message rather than crashing or lying about success.
- Publicly shared papers demonstrate layer 1 without any OAuth setup.
- Drive bbox behaviors (virus-scan interstitials for >100MB files) degrade correctly
  because the response is rejected for not being a PDF.
