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

    private void onStatusPicked(Subject subject, String status) {
        statuses.put(subject.getCode(), status);
        DatabaseHelper db = new DatabaseHelper(this);
        db.saveSubjectStatus(session.getEmail(), program.getCode(),
                subject.getCode(), status);
        db.close();
        recomputeSummary();
    }

    /** Completed credits out of 120 plus the current-semester subject count (FR11). */
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

    /** Same row shape as the GPA calculator; the right spinner picks a status. */
    private final class StatusRowAdapter extends ArrayAdapter<Subject> {

        private final OnStatusPicked listener;

        StatusRowAdapter(Context context, List<Subject> subjects, OnStatusPicked listener) {
            super(context, 0, subjects);
            this.listener = listener;
        }

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
