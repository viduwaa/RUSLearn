# ADR-0006: "Paper & Ink" design language on Material3 (proposed)

- Status: proposed
- Date: 2026-10-06
- Deciders: group (pending)

## Context

Functionality across FR01–FR14 is complete, but the UI is default-material: the
Material3 theme is wired with the wrong `colorPrimary` (slate, not the faculty
teal), surfaces are flat white slabs, typography is hardcoded sizes in the platform
default font, sub-screens have no visible title or up-navigation (`NoActionBar`
with no toolbar), dark mode is half-broken (no `values-night/`), and the one
meaningful signal — the GPA band — renders colourless. The audit lives in
`.scratch/ui-overhaul/spec.md` (findings S1–S12).

## Decision

Adopt a single design language, **"Paper & Ink — the FOT archive"**, executed
entirely inside the locked stack (Java + XML, plain Activities, `findViewById`,
`ListView` + adapters — ADR-0003 stands):

- **Tokens over hexes**: warm paper/ink palette + faculty-teal accent in
  `values/colors.xml` mirrored by `values-night/`; band colours as "stamp" tokens.
- **IBM Plex** superfamily bundled in `res/font/`: Serif for display/numerals,
  Sans for UI, Mono for catalog codes/metadata — applied via shared
  `textAppearance` styles, never per-view sizes.
- **Material Components as the toolkit**: `MaterialToolbar` (title + up-nav on
  every sub-screen), `MaterialCardView` with hairline strokes, `MaterialButton`
  styles, `TextInputLayout` + exposed dropdowns for all six spinners, chips for
  active filters/stamps.
- **Designed states**: loading/empty/error/success are first-class; errors offer
  Retry, empties offer a CTA; toasts remain the validation feedback per house rules.
- **Bottom navigation on top-level screens**: a `BottomNavigationView` — Home ·
  Papers · GPA · Tracking (4 slots) — on the four top-level destinations (Dashboard,
  Papers, GPA, Tracking); a ⚙ settings icon in the Home app bar opens a plain
  `AlertDialog` sheet with Favourites / Offline papers / Request a paper /
  Developer details / Sign out. Detail/Favourites/Offline/Request remain stacked pages with
  toolbar up-nav. Plain-Activity intents keep the stack shallow (ADR-0003 stands —
  no fragments, no nav library).
- **Motion**: one orchestrated entrance per screen (≤250ms) via a tiny
  `ViewPropertyAnimator` helper; no libraries, no activity transitions.

## Consequences

- The group is graded on code quality: all UI vocabulary lives in `themes.xml` +
  two include-layouts + one ~30-line motion helper, so screen layouts shrink and
  stay viva-explainable.
- Material 1.12.0 is already a dependency; no new libraries, no Compose, no
  fragments — the practical-notes patterns still explain every file.
- Dark mode becomes correct by construction (tokens only); layouts must not
  hardcode colours or `@android:color/white`.
- Ticket order and scope: `.scratch/ui-overhaul/issues/01…08`.
