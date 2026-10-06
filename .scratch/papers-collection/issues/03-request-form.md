# 03 - Paper requests form (FR13)

Type: task
Status: resolved

## Comments

- Device-passed END-TO-END: the submitted "RUSLearn ICT1507 2024 exam paper please"
  appears as the newest entry in the portal's /api/feature-request admin listing;
  the "n / 500" counter live-updates as typed.

## What

`ui/PaperRequestActivity` + `activity_paper_request.xml`: multiline request text
(limited to 500 with a live "n / 500" counter), POST to the portal's
feature-request endpoint on a background thread, success toast + finish,
failure toast from the API message.

## Done

- A submitted request reaches the portal (seen in `/api/feature-request` admin listing).
