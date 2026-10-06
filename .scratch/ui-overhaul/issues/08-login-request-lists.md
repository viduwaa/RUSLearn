# 08 - Login, Request, Favourites/Offline polish + dark-mode & accessibility QA

Type: task
Status: open

## What

- **Login**: paper background; serif "RUSLearn" wordmark + "FOT Academic Companion ·
  Rajarata University" lockup; domain hint as a card with the mono `@tec.rjt.ac.lk`
  highlighted; restyled Google sign-in button; privacy line at the foot; staggered
  entrance (first impression of the app).
- **Request**: `TextInputLayout` with `counterEnabled`/500; filled send button that
  **disables and shows progress** while sending; a success view (check + copy +
  "Browse more papers" path) instead of toast-only confirmation. Error text stays
  inline + toast (house rule).
- **Favourites/Offline**: toolbars (ticket 02) + v2 rows (ticket 03) + CTA empty
  states; remove-dialogs keep Material default styling now that the theme is wired.
- **QA pass (whole app)**: 48dp targets everywhere (sign-out first), contrast
  ≥4.5:1 in light *and* night, every icon-only view has a `contentDescription`,
  mono/serif/sans applied consistently, toasts consistent, `UiMotion` entrances on
  every screen ≤250ms.

## Done

- [ ] FR01–FR14 walkthrough matrix re-run on device; dark mode spot-checked on every screen.
