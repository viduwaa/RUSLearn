package com.ruslearn.fot.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the signed-in student in SharedPreferences. The email/name pair is the
 * whole session — there is no server-side session (ADR-0001).
 */
public class SessionManager {

    private static final String PREFS_NAME = "ruslearn_session";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void signIn(String email, String name) {
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_NAME, name == null ? "" : name)
                .apply();
    }

    public boolean isSignedIn() {
        return !prefs.getString(KEY_EMAIL, "").isEmpty();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public void signOut() {
        prefs.edit().clear().apply();
    }
}
