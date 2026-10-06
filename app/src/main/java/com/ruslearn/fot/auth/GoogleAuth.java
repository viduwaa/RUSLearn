package com.ruslearn.fot.auth;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.ruslearn.fot.R;

import org.json.JSONObject;

/**
 * Shared Google sign-in helpers (classic GoogleSignIn API per ADR-0001).
 * Only university accounts on the tec.rjt.ac.lk workspace may use the app.
 */
public final class GoogleAuth {

    public static final String UNIVERSITY_DOMAIN = "tec.rjt.ac.lk";

    /** Sign-in options: email scope plus the ID token needed to read the domain. */
    public static GoogleSignInOptions options(Context context) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.web_client_id))
                .requestEmail()
                .build();
    }

    /** The client used both to launch sign-in and to revoke access on logout. */
    public static GoogleSignInClient client(Context context) {
        return GoogleSignIn.getClient(context, options(context));
    }

    /**
     * A valid credential is a Google account whose email address or workspace domain
     * (jwt "hd" claim) is the university's. The empty string maps to "any domain":
     * personal Google accounts carry an account email domain, not a Workspace org.
     */
    public static boolean isUniversityAccount(JSONObject payload, String email) {
        if (email == null) {
            return false;
        }
        String lower = email.toLowerCase();
        if (lower.endsWith("@" + UNIVERSITY_DOMAIN)) {
            return true;
        }
        return payload != null
                && UNIVERSITY_DOMAIN.equalsIgnoreCase(payload.optString("hd", ""));
    }
}
