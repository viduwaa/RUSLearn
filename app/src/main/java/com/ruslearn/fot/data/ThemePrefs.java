package com.ruslearn.fot.data;

import android.content.Context;

/**
 * The user's theme choice (Paper light / Ink dark / system default) — FR-invisible
 * but user-facing; persisted in the same prefs file as the session.
 */
public final class ThemePrefs {

    private static final String KEY = "theme_mode";
    public static final String PAPER = "paper";
    public static final String INK = "ink";
    public static final String SYSTEM = "system";

    private final Context context;

    public ThemePrefs(Context context) {
        this.context = context;
    }

    public void save(String mode) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE)
                .edit().putString(KEY, mode).apply();
    }

    /** One of {@link #PAPER}, {@link #INK}, {@link #SYSTEM}; system is the default. */
    public String load() {
        return context.getSharedPreferences("theme", Context.MODE_PRIVATE)
                .getString(KEY, SYSTEM);
    }
}
