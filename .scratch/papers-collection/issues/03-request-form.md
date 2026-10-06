# 03 - Paper requests form (FR13)

Type: task
Status: in-progress

## Comments

- Code complete; build passes. Device pass (counter, submit, server receipt) waits
  for unlock; the POST will then be verifiable in the portal's admin listing.

## What

`ui/PaperRequestActivity` + `activity_paper_request.xml`: multiline request text
(limited to 500 with a live "n / 500" counter), POST to the portal's
feature-request endpoint on a background thread, success toast + finish,
failure toast from the API message.

## Done

- A submitted request reaches the portal (seen in `/api/feature-request` admin listing).
