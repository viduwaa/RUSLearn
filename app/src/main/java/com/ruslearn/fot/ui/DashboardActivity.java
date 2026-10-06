package com.ruslearn.fot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.auth.GoogleAuth;
import com.ruslearn.fot.data.SessionManager;

/**
 * Personal dashboard (FR12). Placeholder in M0/M1/M3 shape; the full data-rich
 * layout and wiring arrive in M6.
 */
public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button browsePapers = findViewById(R.id.btnBrowsePapers);
        browsePapers.setOnClickListener(v ->
                startActivity(new Intent(this, PapersActivity.class)));

        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(v -> signOut());
    }

    /** Temporary M2 logout: clears the local session and the Google account grant. */
    private void signOut() {
        SessionManager session = new SessionManager(this);
        session.signOut();
        new Thread(() -> GoogleAuth.client(this).signOut()).start();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
