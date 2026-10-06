package com.ruslearn.fot.auth;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.ruslearn.fot.R;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Shared Google sign-in helpers (classic GoogleSignIn API per ADR-0001).
 * Only university accounts on the tec.rjt.ac.lk workspace may use the app.
 */
public final class GoogleAuth {

    public static final String UNIVERSITY_DOMAIN = "tec.rjt.ac.lk";

    /** The Drive scope offline downloads need (requested once at sign-in). */
    public static final String DRIVE_READONLY_SCOPE =
            "https://www.googleapis.com/auth/drive.readonly";

    /** Sign-in options: email scope, the ID token for the domain check, and the
     * Drive read scope so offline downloads (FR14) can run silently later. */
    public static GoogleSignInOptions options(Context context) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.web_client_id))
                .requestEmail()
                .requestScopes(new Scope(DRIVE_READONLY_SCOPE))
                .build();
    }

    /** The client used both to launch sign-in and to revoke access on logout. */
    public static GoogleSignInClient client(Context context) {
        return GoogleSignIn.getClient(context, options(context));
    }

    /**
     * A valid credential is a Google account whose email address or workspace domain
     * (jwt "hd" claim) is the university's.
     */
    public static boolean isUniversityAccount(JSONObject payload, String email) {
        if (email == null) {
            return false;
        }
        if (email.toLowerCase().endsWith("@" + UNIVERSITY_DOMAIN)) {
            return true;
        }
        return payload != null
                && UNIVERSITY_DOMAIN.equalsIgnoreCase(payload.optString("hd", ""));
    }

    /**
     * OAuth access token for Drive downloads (FR14); null when unavailable — callers
     * degrade to the public-link attempt or the "open online" guidance (ADR-0005).
     */
    @SuppressWarnings("deprecation")
    public static String driveAccessToken(Context context) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                return null;
            }
            return GoogleAuthUtil.getToken(context, account.getAccount(),
                    "oauth2:" + DRIVE_READONLY_SCOPE);
        } catch (GoogleAuthException | IOException e) {
            Log.i("PaperDownload", "Drive token unavailable", e);
            return null;
        }
    }

    private GoogleAuth() {
        // Static helpers only.
    }
}
