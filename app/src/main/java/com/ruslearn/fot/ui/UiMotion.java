package com.ruslearn.fot.ui;

import android.view.View;

/**
 * Tiny motion vocabulary (spec §3.6): one staggered entrance per screen and the
 * favourite-star pop. No libraries, ViewPropertyAnimator only, everything ≤250ms
 * so motion never delays the user.
 */
public final class UiMotion {

    private static final long ENTRANCE_MS = 180;
    private static final long STAGGER_MS = 40;
    private static final float SLIDE_UP_DP = 8f;

    private UiMotion() {
        // Static helpers only.
    }

    /** Fades and slides the given views up, ~40ms apart. Runs once per screen. */
    public static void fadeUp(View... views) {
        float slide = views.length > 0
                ? SLIDE_UP_DP * views[0].getResources().getDisplayMetrics().density : 8f;
        long delay = 0;
        for (View view : views) {
            if (view == null) {
                continue;
            }
            view.setAlpha(0f);
            view.setTranslationY(slide);
            view.animate().alpha(1f).translationY(0f)
                    .setDuration(ENTRANCE_MS)
                    .setStartDelay(delay)
                    .start();
            delay += STAGGER_MS;
        }
    }

    /** Star-tap pop: 1 → 1.2 → 1 in ~160ms. */
    public static void pop(View view) {
        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(80).withEndAction(() ->
                view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start();
    }
}
