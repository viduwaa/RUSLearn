package com.ruslearn.fot.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
 * Subject tracking (FR09–FR11). Statuses (Completed/Current/Upcoming) are saved the
 * moment they are picked; the summary counts completed credits out of the degree's
 * FR11 total. Tracking statuses and GPA grades are separate records — this screen is
 * the progress plan, the calculator is the results.
 */
public class SubjectTrackingActivity extends ToolbarActivity {

    private static final List<String> STATUS_OPTIONS =
            List.of("None", "Completed", "Current", "Upcoming");

    private final List<Subject> semesterSubjects = new ArrayList<>();
    private final Map<String, String> statuses = new HashMap<>();

    private ArrayAdapter<String> programAdapter;
    private ArrayAdapter<String> semesterAdapter;
    private StatusRowAdapter statusRowAdapter;

    private Spinner spProgram;
    private Spinner spSemester;

    private Program program;
    private String semesterKey;
    private boolean suppressingSpinnerEvents;

    private TextView tvCompletedCredits;
    private TextView tvCurrentlyTaking;
    private ProgressBar pbTracking;

    private SessionManager session;

    private interface OnStatusPicked {
        void onStatusPicked(Subject subject, String status);
    }

    // =========================================================================
    // 1. ACTIVITY LIFECYCLE: onCreate
    // =========================================================================
    /**
     *Entry point of the Subject Tracking screen.
     * What happens here:
     * 1. Sets layout (activity_tracking.xml) and attaches top toolbar + bottom navigation bar.
     * 2. Checks session to ensure the student is signed in (redirects if not).
     * 3. Finds UI components (Spinners, TextViews, ProgressBar, ListView) via findViewById.
     * 4. Initializes Adapters for Programme and Semester dropdowns (Spinners).
     * 5. Sets up ListView with StatusRowAdapter to display subject rows.
     * 6. Loads the degree programmes from SubjectCatalogLoader (JSON catalog).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        bindToolbar(R.string.tracking_title, false);
        new TopLevelNav(this, null).attach(R.id.nav_tracking);

        session = new SessionManager(this);
        if (!session.isSignedIn()) {
            Toast.makeText(this, R.string.toast_sign_in_first, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCompletedCredits = findViewById(R.id.tvCompletedCredits);
        tvCurrentlyTaking = findViewById(R.id.tvCurrentlyTaking);
        pbTracking = findViewById(R.id.pbTracking);

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

        statusRowAdapter = new StatusRowAdapter(this, semesterSubjects, this::onStatusPicked);
        ListView lvSubjects = findViewById(R.id.lvSubjects);
        lvSubjects.setAdapter(statusRowAdapter);

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
     * 1. Loads the program structure from the JSON catalog.
     * 2. Reads previously saved statuses (Completed/Current/Upcoming) from the SQLite
     *    table 'subject_records' for this user & programme.
     * 3. Calls recomputeSummary() to update progress bar and counters.
     * 4. Populates the Semester dropdown with this program's semesters.
     */
    private void programPicked(String programCode) {
        program = SubjectCatalogLoader.load(this).get(programCode);
        if (program == null) {
            return;
        }
        statuses.clear();
        DatabaseHelper db = new DatabaseHelper(this);
        statuses.putAll(db.getSubjectStatuses(session.getEmail(), program.getCode()));
        db.close();
        recomputeSummary();

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
     *Called when the user picks a Semester from the dropdown.
     * 1. Retrieves the subjects for that semester from the program.
     * 2. Updates the ListView via statusRowAdapter.notifyDataSetChanged().
     */
    private void semesterPicked(String pickedKey) {
        if (program == null || pickedKey == null
                || program.getSemesters().get(pickedKey) == null) {
            return;
        }
        semesterKey = pickedKey;
        semesterSubjects.clear();
        semesterSubjects.addAll(program.getSemesters().get(semesterKey));
        statusRowAdapter.notifyDataSetChanged();
    }

    // =========================================================================
    // 3. STATUS CHANGE & INSTANT AUTO-SAVE
    // =========================================================================

    /**
     *Called whenever the student changes a subject status (None/Completed/Current/Upcoming).
     * 1. Updates the in-memory 'statuses' map.
     * 2. Instantly auto-saves to SQLite via DatabaseHelper.saveSubjectStatus(...).
     *    - Key: (user_email, program_code, subject_code).
     *    - If status is "None", DatabaseHelper deletes the row.
     * 3. Calls recomputeSummary() to immediately refresh the progress bar and counters.
     */
    private void onStatusPicked(Subject subject, String status) {
        statuses.put(subject.getCode(), status);
        DatabaseHelper db = new DatabaseHelper(this);
        db.saveSubjectStatus(session.getEmail(), program.getCode(),
                subject.getCode(), status);
        db.close();
        recomputeSummary();
    }

    // =========================================================================
    // 4. PROGRESS SUMMARY: recomputeSummary
    // =========================================================================

    /**
     *Calculates degree completion and currently taking counts.
     * 1. Iterates through all subjects across all semesters in the programme.
     * 2. If status is "Completed": sums up their credits (e.g. 3 credits + 2 credits ...).
     * 3. If status is "Current": increments the count of subjects currently being studied.
     * 4. Updates pbTracking (ProgressBar) out of 120 total credits.
     * 5. Updates tvCompletedCredits ("45 / 120 credits completed") and tvCurrentlyTaking ("5 subjects").
     */
    private void recomputeSummary() {
        int completedCredits = 0;
        int currentCount = 0;
        for (List<Subject> subjects : program.getSemesters().values()) {
            for (Subject subject : subjects) {
                String status = statuses.get(subject.getCode());
                if ("Completed".equals(status)) {
                    completedCredits += subject.getCredits();
                }
                if ("Current".equals(status)) {
                    currentCount++;
                }
            }
        }
        pbTracking.setProgress(completedCredits);
        tvCompletedCredits.setText(getString(R.string.tracking_completed_format,
                completedCredits, GradePoint.TOTAL_CREDITS));
        tvCurrentlyTaking.setText(getString(R.string.tracking_current_format, currentCount));
    }

    // =========================================================================
    // 5. LISTVIEW ADAPTER: StatusRowAdapter & ViewHolder
    // =========================================================================

    /**
     *Custom ArrayAdapter that displays each Subject row for tracking.
     * Reuses the same XML layout as the GPA screen (item_subject_value.xml),
     * but the spinner dropdown shows statuses: [None, Completed, Current, Upcoming].
     */
    private final class StatusRowAdapter extends ArrayAdapter<Subject> {

        private final OnStatusPicked listener;

        StatusRowAdapter(Context context, List<Subject> subjects, OnStatusPicked listener) {
            super(context, 0, subjects);
            this.listener = listener;
        }

        /**
         *Called by Android to render each subject row in the ListView.
         * 1. Recycles views using ViewHolder pattern.
         * 2. Sets Subject Code, Name, and Credits.
         * 3. Populates status dropdown with STATUS_OPTIONS (None, Completed, Current, Upcoming).
         * 4. Pre-selects the saved status from SQLite for this subject.
         * 5. Listens for user selection and triggers onStatusPicked(...).
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_subject_value, parent, false);
                holder = new ViewHolder(convertView);
                ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, STATUS_OPTIONS);
                statusAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                holder.value.setAdapter(statusAdapter);
                convertView.setTag(holder);
                holder.value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        String status = STATUS_OPTIONS.get(position);
                        if (holder.subjectCode != null) {
                            Subject target = findSubject(holder.subjectCode);
                            if (target != null) {
                                listener.onStatusPicked(target, status);
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

            holder.value.setSelection(STATUS_OPTIONS.indexOf(
                    statuses.getOrDefault(subject.getCode(), "None")));
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

    /** Cached row views; the same layouts as the GPA calculator rows. */
    private static final class ViewHolder {
        final TextView code;
        final TextView name;
        final TextView credits;
        final Spinner value;
        String subjectCode;

        ViewHolder(View row) {
            code = row.findViewById(R.id.tvSubjectCode);
            name = row.findViewById(R.id.tvSubjectName);
            credits = row.findViewById(R.id.tvSubjectCredits);
            value = row.findViewById(R.id.spSubjectValue);
        }
    }
}
