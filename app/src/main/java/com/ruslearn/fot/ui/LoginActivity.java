package com.ruslearn.fot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.ruslearn.fot.R;
import com.ruslearn.fot.auth.GoogleAuth;
import com.ruslearn.fot.auth.JwtPayloadDecoder;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.data.SessionManager;
import com.ruslearn.fot.data.ThemePrefs;

import org.json.JSONObject;

/**
 * Google sign-in screen (FR01). Only university accounts on the tec.rjt.ac.lk
 * workspace may continue (checked via the email domain or the ID token's "hd"
 * claim — ADR-0001). First sign-in writes the profile into the local users table
 * ("registration"); later sign-ins refresh it.
 */
public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::handleSignInResult);

    private SessionManager session;
    private GoogleSignInClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeSwitcher.apply(new ThemePrefs(this));
        session = new SessionManager(this);

        // Already signed in on this device: skip straight to the dashboard.
        if (session.isSignedIn()) {
            goToDashboard();
            return;
        }
        setContentView(R.layout.activity_login);
        googleClient = GoogleAuth.client(this);

        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v ->
                signInLauncher.launch(googleClient.getSignInIntent()));

        // The app's first impression (ticket 08): staggered entrance, ≤250ms.
        UiMotion.fadeUp(findViewById(R.id.loginTitle), findViewById(R.id.loginHint),
                findViewById(R.id.btnGoogleSignIn));
    }

    private void handleSignInResult(ActivityResult result) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
        try {
            finishSignIn(task.getResult(ApiException.class));
        } catch (ApiException e) {
            if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_FAILED) {
                showToast(R.string.login_config_error);
            } else if (e.getStatusCode() != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                showToast(R.string.login_failed_generic);
            }
        }
    }

    private void finishSignIn(GoogleSignInAccount account) {
        String email = account == null ? null : account.getEmail();
        String idToken = account == null ? null : account.getIdToken();
        if (email == null || idToken == null) {
            showToast(R.string.login_failed_generic);
            return;
        }
        try {
            JSONObject payload = JwtPayloadDecoder.decode(idToken);
            if (!GoogleAuth.isUniversityAccount(payload, email)) {
                showToast(R.string.login_domain_error);
                revokeGoogleAccess();
                return;
            }
            new DatabaseHelper(this).saveUser(email, account.getDisplayName());
            session.signIn(email, account.getDisplayName());
            goToDashboard();
        } catch (IllegalArgumentException e) {
            showToast(R.string.login_failed_generic);
        }
    }

    /** Forgets the Google session for this app so another account can be picked next time. */
    private void revokeGoogleAccess() {
        new Thread(() -> googleClient.signOut()).start();
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void showToast(int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }
}
