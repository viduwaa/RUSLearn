# ADR-0003: One Activity per screen, no fragments

- Status: accepted
- Date: 2026-10-05
- Deciders: group (grilled session)

## Context

The lecturer evaluates code style against the practical notes, which teach only plain
Activities with `findViewById`, explicit `Intent`s and single-XML layouts. Jetpack
Compose is out of scope (locked stack: Java + XML), and fragments add lifecycle surface
the team hasn't been taught.

## Decision

One plain `AppCompatActivity` per screen, one `res/layout/*.xml` each, navigation via
explicit `Intent`s. A small `ui/` package groups the activities; list screens use the adapter patterns from
Practicals 04–05 (`ListView` + `ArrayAdapter`/custom adapter) rather than RecyclerView fragments.

## Consequences

- Every screen is independently explainable in a viva; no fragments-lifecycle bugs.
- Slight duplication of headers/toolbars is accepted; extract to shared styles/drawables in XML where useful.
- If the course later introduces fragments, this ADR is the thing to revisit.
