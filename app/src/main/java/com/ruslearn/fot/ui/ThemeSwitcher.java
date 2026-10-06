package com.ruslearn.fot.ui;

import androidx.appcompat.app.AppCompatDelegate;

import com.ruslearn.fot.data.ThemePrefs;

/**
 * Applies the saved theme before any activity renders (spec: light-first, dark
 * correct). Called in attachBaseContext-time from RUApplication-free wiring:
 * every activity calls {@link #apply(ThemePrefs)} first thing in onCreate.
 */
final class ThemeSwitcher {

    private ThemeSwitcher() {
        // Static helpers only.
    }

    /** Maps the persisted mode onto AppCompatDelegate's night-mode constants. */
    static void apply(ThemePrefs prefs) {
        switch (prefs.load()) {
            case ThemePrefs.PAPER:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case ThemePrefs.INK:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
