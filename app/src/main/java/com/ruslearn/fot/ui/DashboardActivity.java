package com.ruslearn.fot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.auth.GoogleAuth;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.data.SessionManager;
import com.ruslearn.fot.data.SubjectCatalogLoader;
import com.ruslearn.fot.model.GradePoint;
import com.ruslearn.fot.model.Paper;
import com.ruslearn.fot.model.Program;
import com.ruslearn.fot.model.Subject;

import java.util.List;
import java.util.Map;

/**
 * Personal dashboard (FR12): the session card, the academic snapshot (overall GPA,
 * band, credits progress, per-semester GPAs, current semester from tracking), the
 * shortcut grid and recent favourites. Everything reads local data (ADR-0002).
 */
public class DashboardActivity extends AppCompatActivity {

    private static final int RECENT_FAVOURITES_SHOWN = 3;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new SessionManager(this);
        if (!session.isSignedIn()) {
            Toast.makeText(this, R.string.toast_sign_in_first, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        renderSessionCard();

        bindShortcut(R.id.btnBrowsePapers, PapersActivity.class);
        bindShortcut(R.id.btnFavourites, FavouritesActivity.class);
        bindShortcut(R.id.btnOffline, OfflinePapersActivity.class);
        bindShortcut(R.id.btnRequest, PaperRequestActivity.class);
        bindShortcut(R.id.btnGpa, GpaCalculatorActivity.class);
        bindShortcut(R.id.btnTracking, SubjectTrackingActivity.class);

        Button signOut = findViewById(R.id.btnSignOut);
        signOut.setOnClickListener(v -> signOut());

        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session.isSignedIn()) {
            renderSessionCard();
            refreshData();
        }
    }

    private void renderSessionCard() {
        TextView tvName = findViewById(R.id.tvUserName);
        TextView tvEmail = findViewById(R.id.tvUserEmail);
        tvName.setText(session.getName());
        tvEmail.setText(session.getEmail());
    }

    private void bindShortcut(int buttonId, Class<?> target) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v ->
                startActivity(new Intent(this, target)));
    }

    /** Loads the programme the user has really used and paints the snapshot card. */
    private void refreshData() {
        TextView tvDashGpa = findViewById(R.id.tvDashGpa);
        TextView tvDashBand = findViewById(R.id.tvDashBand);
        TextView tvDashCredits = findViewById(R.id.tvDashCredits);
        TextView tvDashSemesterGpas = findViewById(R.id.tvDashSemesterGpas);
        TextView tvDashCurrent = findViewById(R.id.tvDashCurrent);
        TextView tvDashNoData = findViewById(R.id.tvDashNoData);
        ProgressBar pbDash = findViewById(R.id.pbDash);

        Program userProgram = myProgramme();
        tvDashNoData.setVisibility(userProgram == null ? View.VISIBLE : View.GONE);

        if (userProgram == null) {
            tvDashGpa.setText(getString(R.string.gpa_dash));
            tvDashBand.setText(R.string.gpa_no_value);
            tvDashCredits.setText(getString(R.string.gpa_credits_progress, 0,
                    GradePoint.TOTAL_CREDITS));
            pbDash.setProgress(0);
            tvDashSemesterGpas.setVisibility(View.GONE);
            tvDashCurrent.setVisibility(View.GONE);
            renderRecentFavourites();
            return;
        }

        DatabaseHelper db = new DatabaseHelper(this);
        Map<String, String> grades =
                db.getGrades(session.getEmail(), userProgram.getCode());
        Map<String, String> statuses =
                db.getSubjectStatuses(session.getEmail(), userProgram.getCode());
        db.close();

        GradePoint.Totals totals =
                GradePoint.overallOf(userProgram.getSemesters().values(), grades);
        if (Double.isNaN(totals.gpa)) {
            tvDashGpa.setText(getString(R.string.gpa_dash));
            tvDashBand.setText(getString(R.string.gpa_no_value));
        } else {
            tvDashGpa.setText(getString(R.string.gpa_value_format, totals.gpa));
            tvDashBand.setText(GradePoint.bandFor(totals.gpa));
        }
        tvDashCredits.setText(getString(R.string.gpa_credits_progress,
                totals.creditsCompleted, GradePoint.TOTAL_CREDITS));
        pbDash.setProgress(totals.creditsCompleted);

        tvDashSemesterGpas.setText(semesterGpaLine(userProgram, grades));

        String currentSemester = myCurrentSemester(userProgram, statuses);
        if (currentSemester == null) {
            tvDashCurrent.setVisibility(View.GONE);
        } else {
            tvDashCurrent.setVisibility(View.VISIBLE);
            tvDashCurrent.setText(getString(R.string.dashboard_current_format,
                    currentSemester));
        }

        renderRecentFavourites();
    }

    /** "Y1S1 3.40 · Y1S2 3.50" — only graded semesters appear, in order. */
    private String semesterGpaLine(Program program, Map<String, String> grades) {
        StringBuilder line = new StringBuilder();
        for (Map.Entry<String, List<Subject>> entry : program.getSemesters().entrySet()) {
            double gpa = GradePoint.gpaOf(entry.getValue(), grades);
            if (!Double.isNaN(gpa)) {
                if (line.length() > 0) {
                    line.append("  ·  ");
                }
                line.append(entry.getKey()).append(' ')
                        .append(getString(R.string.gpa_value_format, gpa));
            }
        }
        return line.toString();
    }

    /** First semester holding a "Current" subject (FR12's semester cue). */
    private String myCurrentSemester(Program program, Map<String, String> statuses) {
        for (Map.Entry<String, List<Subject>> entry : program.getSemesters().entrySet()) {
            for (Subject subject : entry.getValue()) {
                if ("Current".equals(statuses.get(subject.getCode()))) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /** The first catalogue programme having any saved grades or tracking statuses. */
    private Program myProgramme() {
        for (Program program : SubjectCatalogLoader.load(this).values()) {
            DatabaseHelper db = new DatabaseHelper(this);
            boolean hasGrades = !db.getGrades(session.getEmail(), program.getCode()).isEmpty();
            boolean hasStatuses =
                    !db.getSubjectStatuses(session.getEmail(), program.getCode()).isEmpty();
            db.close();
            if (hasGrades || hasStatuses) {
                return program;
            }
        }
        return null;
    }

    /** Adds up to three favourite rows linking straight to a paper's detail. */
    private void renderRecentFavourites() {
        LinearLayout llRecent = findViewById(R.id.llRecent);
        TextView tvNoSaved = findViewById(R.id.tvNoSaved);

        DatabaseHelper db = new DatabaseHelper(this);
        List<Paper> favourites = db.getFavourites();
        db.close();

        llRecent.removeAllViews();
        tvNoSaved.setVisibility(favourites.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        int shown = Math.min(RECENT_FAVOURITES_SHOWN, favourites.size());
        for (int i = 0; i < shown; i++) {
            Paper paper = favourites.get(i);
            View row = inflater.inflate(R.layout.item_paper, llRecent, false);
            TextView tvCode = row.findViewById(R.id.tvPaperCode);
            TextView tvName = row.findViewById(R.id.tvPaperName);
            TextView tvMeta = row.findViewById(R.id.tvPaperMeta);
            tvCode.setText(paper.getCourseCode());
            tvName.setText(paper.getCourseName());
            tvMeta.setText(getString(R.string.paper_meta_format,
                    paper.getDepartmentCode(), paper.getAcademicYear(),
                    paper.getSemester(), paper.getExamYear()));
            row.findViewById(R.id.ivFavouriteStar)
                    .setBackgroundResource(R.drawable.ic_star_filled);
            row.setOnClickListener(v -> {
                Intent intent = new Intent(this, PaperDetailActivity.class);
                intent.putExtra(PaperDetailActivity.EXTRA_PAPER, paper);
                startActivity(intent);
            });
            llRecent.addView(row);
        }
    }

    /** Clears the session locally and revokes the Google grant (ADR-0001). */
    private void signOut() {
        session.signOut();
        new Thread(() -> GoogleAuth.client(this).signOut()).start();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
