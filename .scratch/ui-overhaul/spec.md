# UI overhaul — "Paper & Ink" design system (M7 candidate)

Feature: a full visual + interaction redesign of all 9 screens. Functionality is
frozen (FR01–FR14 behaviour, ADR-0001…0005 all respected); what changes is how it
looks, how screens are navigated, and how states (loading / empty / error) speak.

Deliverables of this feature: the audit below, the design system (tokens +
components), and one ticket per screen-cluster under `issues/`.

---

## Part 1 — Audit (what the UI looks like today, and why it feels off)

### Systemic findings (cross every screen)

| # | Finding | Evidence |
|---|---------|----------|
| S1 | **The Material3 theme is wired wrong, so every widget is hand-tinted.** `colorPrimary` is set to the ink-slate `#0F172A` (not the brand teal), so default M3 widgets render slate; each layout then patches buttons with `android:backgroundTint` per-instance. Result: no consistent button anywhere, and the M3 component library (Material 1.12.0 is already a dependency) is being used as a dumb `Button`. | `values/themes.xml`, every `activity_*.xml` |
| S2 | **"Cards" are flat white rectangles.** Surfaces are `LinearLayout`s with `background="@color/colorSurface"` — no corner radius, no stroke, no elevation, no shadow. On the `#F8FAFC` canvas they read as unprinted patches, not cards. | dashboard snapshot card, GPA `overallCard`, tracking `trackingCard`, detail `detailMetaCard`, login hint |
| S3 | **Typography is ad-hoc and default-font.** 11sp–34sp sizes are hardcoded per TextView with no `textAppearance` styles; no font family is set anywhere, so the app renders in the platform default. Nothing distinguishes a screen title from a row label except size. | all layouts |
| S4 | **No visible titles or up-navigation.** The theme is `NoActionBar` and no `MaterialToolbar` exists, so `setTitle(...)` calls are **invisible** — Favourites, Offline, Request, Detail, GPA and Tracking land on the user with zero header, zero back affordance beyond the system gesture. Worst single UX defect in the app. | all sub-screens; manifest has no `parentActivityName` |
| S5 | **Dashboard buttons are a hierarchy-free grid of 6 pills.** Three rows of two `Button`s with three different tints chosen arbitrarily ("Request a paper" — the least-used action — gets the accent colour; GPA/Tracking get dark slate). No icons, no grouping, visual weight ≠ importance. | `activity_dashboard.xml` |
| S6 | **List rows blend into the page.** Dividers are stripped (`divider=#00000000`) but rows get no card surface, no ripple, no spacing — `item_paper` is a bare 16dp-padded `LinearLayout`. Rows are untappable-looking. | `item_paper.xml`, `activity_favourites.xml`, `activity_offline_papers.xml` |
| S7 | **Dropdowns are raw, unlabelled `Spinner`s.** Six spinners across Papers/GPA/Tracking use `android.R.layout.simple_spinner_item`, no labels, no chevron styling. On Papers, four naked spinners appear under the search with no "Filter" heading, no active-filter indication, and no way to clear them. | `activity_papers.xml`, `activity_gpa.xml`, `activity_tracking.xml`, `item_subject_value.xml` |
| S8 | **Empty/error states are dead ends.** Empty = grey text at `alpha 0.8` (which drops it below AA contrast). Network errors on Papers show a toast + the empty text but **no retry button** — the only recovery is leaving the screen. | `activity_papers.xml` `tvEmpty`, `PapersActivity.loadInitial()` |
| S9 | **Dark mode is half-broken.** Theme is `DayNight`, but every colour lives only in `values/` (no `values-night/`) and layouts hardcode `@android:color/white` text. In dark mode the widgets flip but the window background, cards and most text do not — the app looks genuinely broken after sunset. | `values/colors.xml`, all layouts |
| S10 | **No iconography, no motion, no brand.** Three drawables exist (launcher + 2 stars). No icons in buttons/tiles/empty states, no transitions, no entrance animation, no logo — the login screen is centred text on a white field. | `res/drawable/`, all layouts |
| S11 | **Accessibility misses.** Sign-out button is 40dp tall (<48dp target); `tvEmpty` alpha 0.8 fails contrast; 11sp labels are at the floor of legibility; `item_paper` root uses `layout_height="match_parent"` inside a ListView row (harmless but wrong). | layouts |
| S12 | **Dead tokens.** `colorGpaGreen/Blue/Cyan/Yellow/Red` are defined and never referenced — the GPA *band* (First Class → Not Eligible) renders in the same muted grey everywhere. The one place colour could carry meaning (your degree class) is colourless. | `colors.xml`, `DashboardActivity` |

### Per-screen notes

- **Login** — no logo/brand moment; the domain-restriction hint is a bare white slab; the Google button is stock-grey. The one screen every student sees first is the least designed.
- **Dashboard** — greeting is a 12sp email under a name; the "snapshot" card crams GPA, band, credits, progress bar, per-semester GPAs and current semester into one 16dp-padded box with no visual hierarchy; shortcut rows use 8dp/20dp/24dp margins with no rhythm.
- **Papers** — search is a bare `EditText` + "Search" button; result count is nowhere (you can't tell 12 results from 400); filters give no feedback of what's active.
- **Detail** — meta card is a 4-row label/value table with 110dp fixed label width; three stacked full-width buttons (slate / teal / white) with no icons; offline state is a small green text line.
- **GPA calculator** — Saved vs Current CGPA sit side-by-side at 26sp with no band stamp, no colour, no delta cue; subject rows squeeze code + name + credits + a 120dp spinner into one line (cramped on 360dp screens).
- **Tracking** — same cramped rows; status is another raw spinner; the progress card repeats the dashboard's without its own identity.
- **Request** — textarea is a white slab (`background=@color/colorSurface`), counter is a lone centred TextView, submit gives only a toast; no success state.
- **Favourites / Offline** — a bare ListView and an empty text. No header, no row cards, no CTA out of emptiness.

---

## Part 2 — Design direction: "Paper & Ink — the FOT archive"

**Concept.** This app is an archive of exam papers. So the UI should feel like a
well-kept university archive: **warm paper, ink-dark type, one faculty-teal stamp
colour, monospaced catalog codes, and grade "stamps"** — typeset with editorial
confidence instead of generic material grey.

- **Tone:** archival-editorial; calm, trustworthy, a little bookish. Not playful, not corporate.
- **The one thing people remember:** the big serif GPA numeral sitting on a paper card with a coloured *degree-class stamp* — plus subject codes everywhere in typewriter mono.
- **Light-first, dark-correct:** "Paper" by day; after dark the same ink flips to a deep teal-black "Ink" mode via `values-night/`.

### Why this direction is safe here

- All execution stays inside the locked stack: Java + XML, plain Activities,
  `findViewById`, `ListView` + adapters (ADR-0003). Material Components 1.12.0 is
  already on the classpath — `MaterialToolbar`, `MaterialCardView`, `MaterialButton`,
  `TextInputLayout`/`ExposedDropdownMenu`, `Chip` are ordinary views; no Compose, no fragments.
- House rule "Toast-based validation feedback" is kept — toasts stay; we only add
  *visual* states around them (buttons that disable while sending, inline errors, retry).
- Every token lands in `values/` + `values-night/`, so layouts keep referencing
  resources, not hex values.

---

## Part 3 — Design system

### 3.1 Type — IBM Plex family (bundled TTFs in `res/font/`)

One superfamily, three voices, real character, OFL-licensed (commit `OFL.txt`):

| Role | Font | Used for |
|------|------|----------|
| Display / numerals | **IBM Plex Serif** (SemiBold/Bold) | App wordmark, screen titles, the big GPA numeral |
| UI / body | **IBM Plex Sans** (Regular/Medium) | Labels, body text, buttons |
| Catalog / metadata | **IBM Plex Mono** (Regular/Medium) | Subject & paper codes, meta lines, counters — the "typed record" feel |

Delivered as `textAppearance` styles (`TextAppearance.RUSLearn.Display`,
`.Title`, `.Body`, `.Label`, `.Code`) — layouts stop hardcoding sizes.

### 3.2 Colour tokens (`values/colors.xml` + `values-night/colors.xml`)

| Token | Light (Paper) | Dark (Ink) | Use |
|-------|--------------|------------|-----|
| `colorPaper` (window bg) | `#FAF7F0` warm cream | `#0E1A1D` | Window background |
| `colorCard` | `#FFFFFF` | `#152528` | Card surfaces |
| `colorInk` (on-paper text) | `#14282E` deep teal-black | `#F2EDE3` | Primary text |
| `colorInkMuted` | `#5D6B70` (≥4.5:1 on paper) | `#9DB4B8` | Secondary text |
| `colorHairline` | `#E4DFD3` | `#24383C` | 1dp card strokes, dividers |
| `colorFaculty` (primary) | `#0F766E` teal-700 | `#5EEAD4` teal-300 | Filled buttons, active states, links |
| `colorOnFaculty` | `#FFFFFF` | `#062F2C` | Text on faculty fill |
| `colorFacultyContainer` | `#CCE8E4` | `#0B4A44` | Tonal buttons, chip fills, selected states |
| `colorStampGreen` | `#15803D` | `#4ADE80` | First Class / Pass / Completed |
| `colorStampAmber` | `#B45309` | `#FBBF24` | Second Upper / Current |
| `colorStampBlue` | `#1D4ED8` | `#93C5FD` | Second Lower / info |
| `colorStampRed` | `#B91C1C` | `#F87171` | Errors / Not Eligible / Fail |
| `colorError` | `#B91C1C` | `#F87171` | Errors |

Replaces today's slate palette. Band mapping: ≥3.70 → green, 3.30–3.69 → teal
(`colorFaculty`), 3.00–3.29 → blue, 2.00–2.99 → amber, <2.00 → red. (Retires the
five unused `colorGpa*` tokens — S12.)

### 3.3 Shape, spacing, elevation

- **Shape:** cards 16dp radius; buttons 12dp; chips full-round; menus 12dp. `ShapeAppearance` styles, one place.
- **Spacing rhythm:** 4 / 8 / 12 / 16 / 24 / 32 only. Screens use 16dp page padding, 12dp between cards, 24dp between sections.
- **Elevation = ink, not shadow:** cards are flat with a 1dp `colorHairline` stroke; the only elevation is on pressed/overlay surfaces. Keeps the archive-print look and renders identically on cheap devices.

### 3.4 Theme wiring (fixes S1, S9)

`Theme.RUSLearn` (parent stays `Theme.Material3.DayNight.NoActionBar`) maps the M3
colour slots properly: `colorPrimary=colorFaculty`, `colorSurface=colorCard`,
`android:colorBackground=colorPaper`, `colorOnError/colorError`, plus
`materialCardViewStyle`, `materialButtonStyle`, `textAppearance*` defaults and
`android:statusBarColor=colorPaper` with light-status-bar icons in day mode.
`values-night/` mirrors the tokens so dark mode works without touching any layout.

### 3.5 Component inventory (styles + small include-layouts)

| Component | Built from | Fixes |
|-----------|-----------|-------|
| `Toolbar.RUSLearn` | `MaterialToolbar` include — title in Plex Serif, up-arrow enabled | S4 |
| **Bottom nav** | `BottomNavigationView` (`view_bottom_nav.xml` include) — **Home · Papers · GPA · Tracking** (4 slots, no More). A **⚙ settings icon** in the Home app bar opens a plain `AlertDialog` sheet listing Favourites / Offline papers / Request a paper / Developer details / Sign out. Bar present on the 4 top-level screens (Dashboard, Papers, GPA, Tracking); Detail/Favourites/Offline/Request stay stacked pages with up-nav. `OnBackPressedCallback` on Papers/GPA/Tracking routes back to Dashboard. Papers/GPA/Tracking become singleTask-ish: `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP` + `onNewIntent`-free `setIntent` refresh on resume. | S4, S5 |
| **GPA/tracking stat rows** | stat blocks (serif value + stamp + delta), two-line subject rows with exposed dropdowns | per-screen |
| `Card.RUSLearn` | `MaterialCardView` style — 16dp radius, 1dp hairline stroke, `colorCard` | S2 |
| Buttons: `.Filled` (faculty), `.Tonal` (faculty container), `.Outlined` (hairline + ink), `.Text` | `MaterialButton` styles, 48dp min height, optional `app:icon` | S1, S5 |
| `Dropdown.RUSLearn` | `TextInputLayout` + `ExposedDropdownMenuEditText` with floating labels | S7 |
| `Chip.RUSLearn.Filter` + "Clear all" action chip | active-filter row on Papers | S7 |
| `item_paper` v2 | card row: mono code chip, Plex Sans title, mono meta line, 24→28dp star with ripple + pop animation, 48dp+ row height | S6 |
| `view_empty_state` include | icon-in-circle + title + support text + optional Retry button | S8 |
| Band/status stamps | small tinted pill (`colorStamp*` @ 14% bg + full-colour text) | S12 |
| Progress: rounded track, faculty fill, `x / 120` caption in Plex Mono | restyled `ProgressBar` | S2 |

### 3.6 Motion (small, orchestrated, Java-only)

- Screen entrance: cards fade + 8dp slide-up, 150–220ms, ~30ms stagger (a tiny `UiMotion` helper using `ViewPropertyAnimator` — viva-explainable, no libraries).
- Star favourite: scale pop 1→1.2→1 (~160ms).
- Progress bars animate to their value instead of jumping.
- No activity transitions, no shared elements — keep the practical-notes vocabulary.

---

## Part 4 — Screen-by-screen plan

1. **Login** — paper background; serif "RUSLearn" wordmark + "FOT Academic Companion ·
   Rajarata University" lockup; the domain hint as a proper card with a mono
   `@tec.rjt.ac.lk` highlight; restyled Google button; privacy line at the foot.
   Staggered entrance.
2. **Dashboard** — "Good morning, ⟨name⟩" greeting (time-aware) with a subtle sign-out
   text button; snapshot hero card: big serif GPA, coloured band stamp, animated credits
   progress, per-semester GPA in mono, current-semester chip; six shortcut **tiles**
   (2×3 card grid: icon + label, identical treatment — Papers first); "Recent favourites"
   strip of `item_paper` v2 rows + "See all →" into Favourites; **bottom nav, Home active**.
3. **Papers** — toolbar **+ bottom nav (Papers slot active)**; search as a labelled text
   field (Search on IME action *and* button); result-count line ("128 papers") in mono;
   2×2 labelled exposed dropdowns; active-filter chip row with "Clear all"; list of card
   rows; skeleton/progress; error state with **Retry**.
4. **Paper detail** — hero: mono code chip + serif paper name; meta card as a clean label/value grid (no fixed 110dp); action row: **Open** (filled, doc icon), **Download** (tonal, download icon / "Remove offline" when present), **Favourite** (outlined, star toggles + pops); offline state as a stamp chip.
5. **Favourites / Offline papers** — toolbar + card rows + CTA empty states ("Browse papers" button straight into Papers).
6. **GPA calculator** — toolbar **+ bottom nav (GPA slot active)**; labelled Programme/Semester dropdowns; Saved vs Current CGPA as two stat blocks with band stamps and a delta arrow (▲/▼ + 2dp) against the saved value; subject rows get two comfortable lines (code+name / credits+grade dropdown) using exposed dropdowns; sticky Save button.
7. **Subject tracking** — toolbar **+ bottom nav (Tracking slot active)**; progress hero with Completed/Current/Upcoming summary stamps; per-subject exposed dropdowns (status) on two-line rows.
8. **Request a paper** — toolbar; intro copy; `TextInputLayout` with `counterEnabled` (500), filled send button that **disables + shows progress** while sending; success view (check + "Request sent") replacing the toast-only confirmation.

### UX principles going forward (the rules behind the redesign)

1. **Never land lost:** every screen has a title, an up arrow *or* the bottom nav; the 4 top-level destinations are one tap away everywhere.
2. **State is a first-class citizen:** loading, empty, error and success all get designed states; errors offer a next action (Retry / Browse).
3. **One accent, meaning elsewhere:** teal = interactive; stamps = meaning (band, status); ink = content.
4. **Type does the labelling:** mono for catalog data, serif for moments, sans for everything else — no more size-hunting hierarchy.
5. **48dp and honest contrast:** every target ≥48dp, every text ≥4.5:1 in *both* modes.
6. **Motion rewards, never delays:** entrances under 250ms; nothing animates between the user and their data.

---

## Part 5 — Execution order

| Ticket | Scope |
|--------|-------|
| `01-design-tokens.md` | Fonts, colours + night, theme wiring, textAppearances, shape, base styles |
| `02-toolbar-navigation.md` | Toolbar include + up-nav on all 7 sub-screens |
| `02b-bottom-navigation.md` | `BottomNavigationView` include: Home · Papers · GPA · Tracking (4 slots) + Home ⚙ settings sheet; on the 4 top-level screens; back-routing to Dashboard |
| `03-shared-components.md` | Card style, `item_paper` v2, empty-state include, stamps, motion helper |
| `04-papers-screen.md` | Search, labelled dropdowns, filter chips, result count, retry |
| `05-dashboard.md` | Greeting, snapshot hero, tile grid, favourites strip |
| `06-paper-detail.md` | Hero, meta grid, icon action row, offline chip |
| `07-gpa-tracking.md` | Stat blocks, band stamps, dropdown subject rows, sticky save |
| `08-login-request-lists.md` | Login, Request success state, Favourites/Offline, dark-mode + a11y QA pass |

## Non-goals

- No functional/API/DB changes; no new screens; no RecyclerView migration (ADR-0003 stands);
  no Compose, no fragments, no libraries beyond the current dependencies.
