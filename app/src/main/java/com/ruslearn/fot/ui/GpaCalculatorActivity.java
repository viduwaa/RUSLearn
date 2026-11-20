package com.ruslearn.fot.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.data.SessionManager;
import com.ruslearn.fot.data.SubjectCatalogLoader;
import com.ruslearn.fot.model.GradePoint;
import com.ruslearn.fot.model.Program;
import com.ruslearn.fot.model.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GPA calculator (FR07) with what-if prediction (FR08). The Saved card shows the
 * database's computed GPA; the Current card recalculates from the pickers — setting
 * future grades without saving is the prediction. Saving persists the current
 * semester's grades on a (user, programme, subject) key.
 */
public class GpaCalculatorActivity extends ToolbarActivity {

    private final List<Subject> semesterSubjects = new ArrayList<>();
    private final Map<String, String> currentGrades = new HashMap<>();

    private ArrayAdapter<String> programAdapter;
    private ArrayAdapter<String> semesterAdapter;
    private GradeRowAdapter gradeRowAdapter;

    private Spinner spProgram;
    private Spinner spSemester;

    private Program program;
    private String semesterKey;
    private boolean suppressingSpinnerEvents;

    private TextView tvSavedGpa;
    private TextView tvSavedBand;
    private TextView tvCurrentGpa;
    private TextView tvCurrentBand;
    private TextView tvCreditsProgress;
    private TextView tvSemesterGpa;
    private ProgressBar pbCredits;

    private SessionManager session;

    /** A grade was changed on one subject row. */
    private interface OnGradePicked {
        void onGradePicked(Subject subject, String grade);
    }

    // =========================================================================
    // 1. ACTIVITY LIFECYCLE: onCreate
    // =========================================================================
    /**
     * Entry point of the screen (called when this Activity is launched).
     * What happens here:
     * 1. Loads XML layout (activity_gpa.xml) and sets up toolbar + bottom navigation bar.
     * 2. Checks if the student is logged in using SessionManager; redirects if not.
     * 3. Finds all UI elements (Spinners, Buttons, TextViews, ProgressBar) using findViewById.
     * 4. Sets up listeners for Program & Semester dropdowns (Spinners).
     * 5. Connects the "Save Grades" button click listener to saveGrades().
     * 6. Loads the degree programmes from SubjectCatalogLoader (JSON catalog) into the dropdown.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpa);
        bindToolbar(R.string.gpa_title, false);
        new TopLevelNav(this, null).attach(R.id.nav_gpa);

        session = new SessionManager(this);
        if (!session.isSignedIn()) {
            Toast.makeText(this, R.string.toast_sign_in_first, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvSavedGpa = findViewById(R.id.tvSavedGpa);
        tvSavedBand = findViewById(R.id.tvSavedBand);
        tvCurrentGpa = findViewById(R.id.tvCurrentGpa);
        tvCurrentBand = findViewById(R.id.tvCurrentBand);
        tvCreditsProgress = findViewById(R.id.tvCreditsProgress);
        tvSemesterGpa = findViewById(R.id.tvSemesterGpa);
        pbCredits = findViewById(R.id.pbCredits);

        spProgram = findViewById(R.id.spProgram);
        spSemester = findViewById(R.id.spSemester);

        programAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProgram.setAdapter(programAdapter);

        semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSemester.setAdapter(semesterAdapter);

        spProgram.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String code = programAdapter.getItem(position);
                if (code != null) {
                    programPicked(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do — the dropdown always keeps a selection.
            }
        });
        spSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressingSpinnerEvents) {
                    return;
                }
                String key = semesterAdapter.getItem(position);
                if (key != null) {
                    semesterPicked(key);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do.
            }
        });

        Button btnSave = findViewById(R.id.btnSaveGrades);
        btnSave.setOnClickListener(v -> saveGrades());

        gradeRowAdapter = new GradeRowAdapter(this, semesterSubjects,
                this::onGradePicked);
        ListView lvSubjects = findViewById(R.id.lvSubjects);
        lvSubjects.setAdapter(gradeRowAdapter);

        for (Program catalogProgram : SubjectCatalogLoader.load(this).values()) {
            programAdapter.add(catalogProgram.getCode());
        }
        programAdapter.notifyDataSetChanged();
    }

    // =========================================================================
    // 2. USER SELECTION HANDLERS: Programme & Semester
    // =========================================================================

    /**
     *Called when the user picks a Degree Programme (e.g., BICT, BST, ET).
     * 1. Loads the program curriculum structure from SubjectCatalogLoader.
     * 2. Queries SQLite (DatabaseHelper) to fetch previously saved grades for this student and program.
     * 3. Populates the Semester dropdown with the semesters belonging to this program.
     * 4. Automatically selects the first semester to display its subjects.
     */
    private void programPicked(String programCode) {
        program = SubjectCatalogLoader.load(this).get(programCode);
        if (program == null) {
            return;
        }
        currentGrades.clear();
        DatabaseHelper db = new DatabaseHelper(this);
        currentGrades.putAll(db.getGrades(session.getEmail(), program.getCode()));
        db.close();

        suppressingSpinnerEvents = true;
        semesterAdapter.clear();
        semesterAdapter.addAll(program.getSemesters().keySet());
        semesterAdapter.notifyDataSetChanged();
        spSemester.setSelection(0, false);
        suppressingSpinnerEvents = false;

        semesterKey = null;
        if (!program.getSemesters().isEmpty()) {
            semesterPicked(program.getSemesters().keySet().iterator().next());
        }
    }

    /**
     * Called when the user picks a Semester from the dropdown (e.g., Year 1 Semester 1).
     * 1. Fetches the list of subjects for this semester from the loaded program.
     * 2. Clears the previous subject list and updates the ListView via gradeRowAdapter.notifyDataSetChanged().
     * 3. Triggers recomputeCards() to refresh GPA numbers and credit totals.
     */
    private void semesterPicked(String pickedKey) {
        if (program == null || pickedKey == null
                || program.getSemesters().get(pickedKey) == null) {
            return;
        }
        semesterKey = pickedKey;
        semesterSubjects.clear();
        semesterSubjects.addAll(program.getSemesters().get(semesterKey));
        gradeRowAdapter.notifyDataSetChanged();
        recomputeCards();
    }

    // =========================================================================
    // 3. GRADE CHANGE & "WHAT-IF" PREDICTION
    // =========================================================================

    /**
     *Called whenever a grade dropdown is changed for any subject row.
     * 1. Updates currentGrades map in memory (does NOT write to SQLite yet!).
     * 2. Calls recomputeCards() immediately.
     * 3. This powers the "What-If" prediction (FR08) — the student sees their predicted
     *    GPA in real time on the "Current" card without altering their saved records.
     */
    private void onGradePicked(Subject subject, String grade) {
        currentGrades.put(subject.getCode(), grade);
        recomputeCards();
    }

    // =========================================================================
    // 4. DATABASE PERSISTENCE: saveGrades
    // =========================================================================

    /**
     *Called when the student clicks the "Save Grades" button.
     * 1. Iterates through all subjects across all semesters in the selected program.
     * 2. Writes each grade into the local SQLite table 'grade_records' via DatabaseHelper.
     *    - Key: (user_email, program_code, subject_code).
     *    - If a grade is "None", DatabaseHelper deletes the row to save space.
     * 3. Shows a brief Toast message to confirm ("Grades saved").
     * 4. Updates the "Saved" GPA card so it matches the newly saved state.
     */
    private void saveGrades() {
        if (program == null) {
            return;
        }
        DatabaseHelper db = new DatabaseHelper(this);
        for (List<Subject> subjects : program.getSemesters().values()) {
            for (Subject subject : subjects) {
                db.saveGrade(session.getEmail(), program.getCode(), subject.getCode(),
                        currentGrades.getOrDefault(subject.getCode(), GradePoint.UNASSIGNED));
            }
        }
        db.close();
        Toast.makeText(this, R.string.toast_grades_saved, Toast.LENGTH_SHORT).show();
        recomputeCards();
    }

    // =========================================================================
    // 5. CALCULATIONS & DISPLAY: recomputeCards & showCard
    // =========================================================================

    /**
     *The core calculation engine for this screen.
     * 1. Reads officially saved grades from SQLite to calculate and show the "Saved GPA" card.
     * 2. Reads live grades from currentGrades to calculate and show the "Current (What-If)" card.
     * 3. Uses GradePoint.overallOf() to compute cumulative GPA:
     *    GPA = Sum(credits * points) / Sum(credits).
     * 4. Updates the degree completion ProgressBar (out of 120 credits).
     * 5. Uses GradePoint.gpaOf() to compute the single semester GPA chip for the currently selected semester.
     */
    private void recomputeCards() {
        if (program == null) {
            return;
        }
        DatabaseHelper db = new DatabaseHelper(this);
        Map<String, String> savedGrades =
                db.getGrades(session.getEmail(), program.getCode());
        db.close();

        GradePoint.Totals savedTotals =
                GradePoint.overallOf(program.getSemesters().values(), savedGrades);
        showCard(tvSavedGpa, tvSavedBand, savedTotals.gpa);

        GradePoint.Totals liveTotals =
                GradePoint.overallOf(program.getSemesters().values(), currentGrades);
        showCard(tvCurrentGpa, tvCurrentBand, liveTotals.gpa);

        pbCredits.setProgress(liveTotals.creditsCompleted);
        tvCreditsProgress.setText(getString(R.string.gpa_credits_progress,
                liveTotals.creditsCompleted, GradePoint.TOTAL_CREDITS));

        double semesterGpa = GradePoint.gpaOf(semesterSubjects, currentGrades);
        tvSemesterGpa.setText(Double.isNaN(semesterGpa)
                ? getString(R.string.gpa_dash)
                : getString(R.string.gpa_value_format, semesterGpa));
    }

    /**
     * Formats and displays a GPA card on the screen.
     * - If GPA is NaN (no grades entered yet), shows "-" and "No grades".
     * - If GPA is valid, formats to 2 decimal places (e.g., "3.75").
     * - Maps GPA to class honors (e.g., First Class, Second Upper) using GradePoint.bandFor(gpa).
     * - Colors the badge using Stamp.apply(bandView, gpa).
     */
    private void showCard(TextView valueView, TextView bandView, double gpa) {
        if (Double.isNaN(gpa)) {
            valueView.setText(getString(R.string.gpa_dash));
            bandView.setText(getString(R.string.gpa_no_value));
            bandView.setTextColor(androidx.core.content.ContextCompat.getColor(
                    this, R.color.colorInkMuted));
            return;
        }
        valueView.setText(getString(R.string.gpa_value_format, gpa));
        bandView.setText(GradePoint.bandFor(gpa));
        Stamp.apply(bandView, gpa);
    }

    // =========================================================================
    // 6. LISTVIEW ADAPTER: GradeRowAdapter & ViewHolder
    // =========================================================================

    /**
     *Custom ArrayAdapter that converts each Subject into a row inside the ListView.
     * Uses the "ViewHolder Pattern" for performance (avoids repeated expensive findViewById calls).
     */
    private final class GradeRowAdapter extends ArrayAdapter<Subject> {

        private final OnGradePicked listener;

        GradeRowAdapter(Context context, List<Subject> subjects, OnGradePicked listener) {
            super(context, 0, subjects);
            this.listener = listener;
        }

        /**
         * Called by Android for every row visible on the screen.
         * 1. Inflates item_subject_value.xml layout if needed (or recycles convertView).
         * 2. Sets Subject Code, Subject Name, and Credits.
         * 3. Checks if the subject is Non-GPA:
         *    - If Non-GPA (e.g. English), dropdown shows [Pass, Fail, None].
         *    - If GPA subject, dropdown shows [A+, A, A-, B+ ... E, None].
         * 4. Pre-selects the previously entered grade for this subject.
         * 5. Listens for user grade selections and notifies listener.onGradePicked(...).
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_subject_value, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
                holder.value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        String grade = holder.gradeOptions.get(position);
                        if (grade != null && holder.subjectCode != null) {
                            Subject target = findSubject(holder.subjectCode);
                            if (target != null) {
                                listener.onGradePicked(target, grade);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Rows keep a selection.
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Subject subject = getItem(position);
            if (subject == null) {
                return convertView;
            }
            holder.subjectCode = subject.getCode();
            holder.code.setText(subject.getCode());
            holder.name.setText(subject.getName());
            holder.credits.setText(getContext().getString(R.string.subject_credits_format,
                    subject.getCredits()));

            List<String> options = subject.isNonGpa()
                    ? GradePoint.NON_GPA_GRADES
                    : GradePoint.GPA_GRADES;
            if (!options.equals(holder.gradeOptions)) {
                holder.gradeOptions.clear();
                holder.gradeOptions.addAll(options);
                ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(
                        getContext(), android.R.layout.simple_spinner_item,
                        holder.gradeOptions);
                valueAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                holder.value.setAdapter(valueAdapter);
            }
            String assigned = currentGrades.getOrDefault(subject.getCode(),
                    GradePoint.UNASSIGNED);
            int index = holder.gradeOptions.indexOf(assigned);
            holder.value.setSelection(index >= 0
                    ? index : holder.gradeOptions.indexOf(GradePoint.UNASSIGNED), false);
            return convertView;
        }

        private Subject findSubject(String code) {
            for (Subject subject : semesterSubjects) {
                if (subject.getCode().equals(code)) {
                    return subject;
                }
            }
            return null;
        }
    }

    /** Cached row views. */
    private static final class ViewHolder {
        final TextView code;
        final TextView name;
        final TextView credits;
        final Spinner value;
        final List<String> gradeOptions = new ArrayList<>();
        String subjectCode;

        ViewHolder(View row) {
            code = row.findViewById(R.id.tvSubjectCode);
            name = row.findViewById(R.id.tvSubjectName);
            credits = row.findViewById(R.id.tvSubjectCredits);
            value = row.findViewById(R.id.spSubjectValue);
        }
    }
}
