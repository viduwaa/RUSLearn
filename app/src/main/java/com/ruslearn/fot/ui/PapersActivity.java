package com.ruslearn.fot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.api.PaperApi;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.data.FilterPrefs;
import com.ruslearn.fot.model.FilterOptions;
import com.ruslearn.fot.model.Paper;
import com.ruslearn.fot.model.PaperFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Papers browser (FR02–FR04): search box, four filter dropdowns and the paper
 * list. Choices remembered automatically (FilterPrefs, like the portal); filters
 * live-fire on every selection change. The papers endpoint has no exam-year
 * parameter, so the Past-Paper Year filter is applied locally, mirroring the
 * portal web app's behaviour.
 */
public class PapersActivity extends ToolbarActivity {

    private static final String ALL = "All";

    private final List<Paper> currentPapers = new ArrayList<>();
    private TopLevelNav topLevelNav;

    private ArrayAdapter<String> studyYearAdapter;
    private ArrayAdapter<String> semesterAdapter;
    private ArrayAdapter<String> departmentAdapter;
    private ArrayAdapter<String> examYearAdapter;
    private PaperAdapter paperAdapter;

    private Spinner spStudyYear;
    private Spinner spSemester;
    private Spinner spDepartment;
    private Spinner spExamYear;
    private EditText etSearch;
    private ProgressBar loading;
    private TextView tvEmpty;
    private TextView tvResultCount;

    private FilterPrefs filterPrefs;
    private boolean suppressingSelectionEvents = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_papers);
        bindToolbar(R.string.papers_title, false);

        topLevelNav = new TopLevelNav(this, null);
        topLevelNav.attach(R.id.nav_papers);

        filterPrefs = new FilterPrefs(this);

        spStudyYear = findViewById(R.id.spStudyYear);
        spSemester = findViewById(R.id.spSemester);
        spDepartment = findViewById(R.id.spDepartment);
        spExamYear = findViewById(R.id.spExamYear);

        studyYearAdapter = setupSpinner(spStudyYear);
        semesterAdapter = setupSpinner(spSemester);
        departmentAdapter = setupSpinner(spDepartment);
        examYearAdapter = setupSpinner(spExamYear);

        AdapterView.OnItemSelectedListener refreshOnSelect = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!suppressingSelectionEvents) {
                    refresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do — a dropdown always keeps a selection.
            }
        };
        spStudyYear.setOnItemSelectedListener(refreshOnSelect);
        spSemester.setOnItemSelectedListener(refreshOnSelect);
        spDepartment.setOnItemSelectedListener(refreshOnSelect);
        spExamYear.setOnItemSelectedListener(refreshOnSelect);

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> refresh());

        loading = findViewById(R.id.loading);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvResultCount = findViewById(R.id.tvResultCount);

        ListView lvPapers = findViewById(R.id.lvPapers);
        paperAdapter = new PaperAdapter(this, currentPapers);
        lvPapers.setAdapter(paperAdapter);
        lvPapers.setOnItemClickListener((parent, view, position, id) -> {
            Paper paper = currentPapers.get(position);
            Intent intent = new Intent(this, PaperDetailActivity.class);
            intent.putExtra(PaperDetailActivity.EXTRA_PAPER, paper);
            startActivity(intent);
        });

        etSearch = findViewById(R.id.etSearch);
        loadInitial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Favourite stars may have changed inside the detail screen.
        DatabaseHelper db = new DatabaseHelper(this);
        Set<String> favouriteIds = new HashSet<>();
        for (Paper paper : db.getFavourites()) {
            favouriteIds.add(paper.getDriveFileId());
        }
        db.close();
        paperAdapter.setFavouriteIds(favouriteIds);
    }

    /** Options + the full paper list; the first report also builds the exam-year list. */
    private void loadInitial() {
        loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                FilterOptions options = PaperApi.getFilterOptions();
                List<Paper> allPapers = PaperApi.listPapers(
                        new PaperFilter(null, null, null, null, null));
                List<String> paperExamYears = collectExamYears(allPapers);
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    populateDropdowns(options, paperExamYears);
                    currentPapers.clear();
                    currentPapers.addAll(allPapers);
                    paperAdapter.notifyDataSetChanged();
                    showEmpty(currentPapers.isEmpty(), R.string.papers_empty_no_results);
                    restoreRemembered();
                });
            } catch (IOException e) {
                android.util.Log.w("PaperApi", "initial papers load failed", e);
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    showToast(e.getMessage());
                    showEmpty(true, R.string.papers_error_network);
                });
            }
        }).start();
    }

    private void populateDropdowns(FilterOptions options, List<String> paperExamYears) {
        studyYearAdapter.clear();
        studyYearAdapter.add(ALL);
        for (String value : options.getYears()) {
            studyYearAdapter.add(value);
        }

        semesterAdapter.clear();
        semesterAdapter.add(ALL);
        for (String value : options.getSemesters()) {
            semesterAdapter.add(value);
        }

        departmentAdapter.clear();
        departmentAdapter.add(ALL);
        for (String value : options.getDepartments()) {
            departmentAdapter.add(value);
        }

        examYearAdapter.clear();
        examYearAdapter.add(ALL);
        examYearAdapter.addAll(paperExamYears);
    }

    /** Applies remembered filter values exactly once (portal parity). */
    private void restoreRemembered() {
        PaperFilter remembered = filterPrefs.load();

        suppressingSelectionEvents = true;
        etSearch.setText(remembered.getSearchQuery() == null ? "" : remembered.getSearchQuery());
        selectValue(spStudyYear, remembered.getYear());
        selectValue(spSemester, remembered.getSemester());
        selectValue(spDepartment, remembered.getDepartment());
        selectValue(spExamYear, filterPrefs.loadExamYear());
        suppressingSelectionEvents = false;

        if (!remembered.isEmpty() || !ALL.equals(filterPrefs.loadExamYear())) {
            refresh();
        }
    }

    @SuppressWarnings("unchecked")
    private void selectValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        String wanted = (value == null || value.isEmpty()) ? ALL : value;
        int position = adapter.getPosition(wanted);
        spinner.setSelection(position >= 0 ? position : 0);
    }

    private void refresh() {
        loading.setVisibility(View.VISIBLE);
        PaperFilter filter = currentFilter();
        filterPrefs.save(filter);
        filterPrefs.saveExamYear(examYearValue());

        new Thread(() -> {
            try {
                List<Paper> fetched = PaperApi.listPapers(filter);
                List<Paper> displayed = applyExamYearFilter(fetched);
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    currentPapers.clear();
                    currentPapers.addAll(displayed);
                    paperAdapter.notifyDataSetChanged();
                    showEmpty(currentPapers.isEmpty(), R.string.papers_empty_no_results);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    showToast(e.getMessage());
                    showEmpty(currentPapers.isEmpty(), R.string.papers_error_network);
                });
            }
        }).start();
    }

    /** The server has no exam-year parameter, mirroring the portal's local filtering. */
    private List<Paper> applyExamYearFilter(List<Paper> fetched) {
        String selected = examYearValue();
        if (ALL.equals(selected)) {
            return fetched;
        }
        List<Paper> result = new ArrayList<>();
        for (Paper paper : fetched) {
            if (selected.equals(paper.getExamYear())) {
                result.add(paper);
            }
        }
        return result;
    }

    private PaperFilter currentFilter() {
        String year = dropdownValue(studyYearAdapter, spStudyYear);
        String semester = dropdownValue(semesterAdapter, spSemester);
        String department = dropdownValue(departmentAdapter, spDepartment);
        String query = etSearch == null ? "" : etSearch.getText().toString().trim();
        return new PaperFilter(
                ALL.equals(year) ? null : year,
                ALL.equals(semester) ? null : semester,
                ALL.equals(department) ? null : department,
                null,
                query);
    }

    private String dropdownValue(ArrayAdapter<String> adapter, Spinner spinner) {
        int position = spinner.getSelectedItemPosition();
        if (position >= 0 && position < adapter.getCount()) {
            return adapter.getItem(position);
        }
        return ALL;
    }

    private String examYearValue() {
        return dropdownValue(examYearAdapter, spExamYear);
    }

    private void showEmpty(boolean visible, int messageResId) {
        tvEmpty.setText(messageResId);
        tvEmpty.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            tvResultCount.setText("");
        } else {
            tvResultCount.setText(getString(R.string.papers_count_format, currentPapers.size()));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /** Distinct exam years across ALL papers, newest first. */
    private static List<String> collectExamYears(List<Paper> papers) {
        Set<String> unique = new LinkedHashSet<>();
        for (Paper paper : papers) {
            if (paper.getExamYear() != null && !paper.getExamYear().isEmpty()) {
                unique.add(paper.getExamYear());
            }
        }
        List<String> years = new ArrayList<>(unique);
        years.sort((a, b) -> b.compareTo(a));
        return years;
    }

    private ArrayAdapter<String> setupSpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return adapter;
    }
}
