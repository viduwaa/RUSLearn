package com.ruslearn.fot.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.api.PaperApi;
import com.ruslearn.fot.model.Paper;

import java.io.IOException;
import java.util.List;

/**
 * Personal dashboard (FR12). Placeholder in M0; full layout and data wiring
 * arrive in M6. The debug button exercises the M1 API plumbing and disappears
 * in M3.
 */
public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button loadPapers = findViewById(R.id.btnLoadPapers);
        loadPapers.setOnClickListener(v -> loadPapersDebug());
    }

    /** Temporary M1 probe: fetches papers on a background thread and toasts the result. */
    private void loadPapersDebug() {
        new Thread(() -> {
            try {
                List<Paper> papers = PaperApi.listPapers(null);
                android.util.Log.i("PaperApiDebug", "loaded=" + papers.size()
                        + " first=" + (papers.isEmpty() ? "-" : papers.get(0).getCourseCode()));
                String message = papers.isEmpty()
                        ? getString(R.string.debug_zero_papers)
                        : papers.size() + " papers; first: "
                                + papers.get(0).getCourseCode();
                runOnUiThread(() -> Toast
                        .makeText(DashboardActivity.this, message, Toast.LENGTH_LONG)
                        .show());
            } catch (IOException e) {
                android.util.Log.i("PaperApiDebug", "failed", e);
                runOnUiThread(() -> Toast
                        .makeText(DashboardActivity.this, "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                        .show());
            }
        }).start();
    }
}
