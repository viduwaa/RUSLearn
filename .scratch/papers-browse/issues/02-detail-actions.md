# 02 - Paper detail: open / favourite / offline download

Type: task
Status: resolved

## Comments

- Verified on device: detail renders live paper data; favourite toggle persisted
  (button flipped to "Remove favourite"); download on a domain-restricted paper
  degraded gracefully (button re-enabled, clear toast, no crash — after fixing a
  NetworkOnMainThreadException by moving the fetch onto a background thread).
- Live happy-path download still needs either a publicly shared paper or the
  signed-in OAuth scope (blocked on the user's web client ID); failure layer verified.

## What

- `ui/PaperDetailActivity.java` + `activity_paper_detail.xml`: open via ACTION_VIEW,
  favourite toggle (FR06),/offline download via `data/DrivePaperFetcher` (ADR-0005).
- `model/Paper` gained Serializable + toJson (local snapshotting).
