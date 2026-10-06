# Issue tracker: Local Markdown

Issues and specs for this repo live as markdown files in `.scratch/`.

## Conventions

- One feature per directory: `.scratch/<feature-slug>/`
- The spec is `.scratch/<feature-slug>/spec.md`
- Implementation issues are one file per ticket at `.scratch/<feature-slug>/issues/<NN>-<slug>.md`, numbered from `01`, never a single combined tickets file
- Status is recorded as a `Status:` line near the top of each issue file (`open` / `in-progress` / `resolved`)
- Comments and conversation history append to the bottom of the file under a `## Comments` heading

## When a skill says "publish to the issue tracker"

Create a new file under `.scratch/<feature-slug>/` (creating the directory if needed).

## When a skill says "fetch the relevant ticket"

Read the file at the referenced path. The user will normally pass the path or the issue number directly.

## Switching to GitHub later

If the repo gets a remote and the group prefers GitHub Issues, replace this file with the
GitHub tracker variant and update the pointer in `AGENTS.md`.

## Master ticket list

`.scratch/tickets.md` holds one line per created ticket when you want an index beyond a
single feature directory (optional; not required by any skill).
