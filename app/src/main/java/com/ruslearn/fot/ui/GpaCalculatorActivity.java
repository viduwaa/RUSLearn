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

    /** Loads the programme, restores its saved grades and lists its semesters. */
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

    private void onGradePicked(Subject subject, String grade) {
        currentGrades.put(subject.getCode(), grade);
        recomputeCards();
    }

    /** Persists the current semester's entries; "None" entries drop old rows. */
    private void saveGrades() {
        if (program == null || semesterKey == null) {
            return;
        }
        DatabaseHelper db = new DatabaseHelper(this);
        for (Subject subject : semesterSubjects) {
            db.saveGrade(session.getEmail(), program.getCode(), subject.getCode(),
                    currentGrades.getOrDefault(subject.getCode(), GradePoint.UNASSIGNED));
        }
        db.close();
        Toast.makeText(this, R.string.toast_grades_saved, Toast.LENGTH_SHORT).show();
        recomputeCards();
    }

    /** Recomputes both GPA cards, their bands, the credits bar and the semester chip. */
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

    private void showCard(TextView valueView, TextView bandView, double gpa) {
        if (Double.isNaN(gpa)) {
            valueView.setText(getString(R.string.gpa_dash));
            bandView.setText(getString(R.string.gpa_no_value));
            return;
        }
        valueView.setText(getString(R.string.gpa_value_format, gpa));
        bandView.setText(GradePoint.bandFor(gpa));
    }

    /**
     * Subject rows for one semester. View recycling rebinds the spinner options; a
     * rebound spinner may fire one synthetic selection event — harmless, because the
     * calculator reads only the current grade map.
     */
    private final class GradeRowAdapter extends ArrayAdapter<Subject> {

        private final OnGradePicked listener;

        GradeRowAdapter(Context context, List<Subject> subjects, OnGradePicked listener) {
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
