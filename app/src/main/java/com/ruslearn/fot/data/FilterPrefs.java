package com.ruslearn.fot.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.ruslearn.fot.model.PaperFilter;

/**
 * Remembers the papers-screen filter choices in SharedPreferences, mirroring the
 * portal's behaviour ("Your filter preferences are automatically saved").
 */
public class FilterPrefs {

    private static final String PREFS_NAME = "ruslearn_paper_filters";
    private static final String KEY_YEAR = "year";
    private static final String KEY_SEMESTER = "semester";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_COURSE_CODE = "courseCode";
    private static final String KEY_SEARCH = "searchQuery";
    private static final String KEY_EXAM_YEAR = "examYear";

    private final SharedPreferences prefs;

    public FilterPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Reads the remembered filter as a PaperFilter. */
    public PaperFilter load() {
        return new PaperFilter(
                prefs.getString(KEY_YEAR, ""),
                prefs.getString(KEY_SEMESTER, ""),
                prefs.getString(KEY_DEPARTMENT, ""),
                prefs.getString(KEY_COURSE_CODE, ""),
                prefs.getString(KEY_SEARCH, ""));
    }

    public void save(PaperFilter filter) {
        prefs.edit()
                .putString(KEY_YEAR, filter.getYear() == null ? "" : filter.getYear())
                .putString(KEY_SEMESTER, filter.getSemester() == null ? "" : filter.getSemester())
                .putString(KEY_DEPARTMENT, filter.getDepartment() == null ? "" : filter.getDepartment())
                .putString(KEY_COURSE_CODE, filter.getCourseCode() == null ? "" : filter.getCourseCode())
                .putString(KEY_SEARCH, filter.getSearchQuery() == null ? "" : filter.getSearchQuery())
                .apply();
    }

    /** The exam year filters locally (no API parameter), so it persists separately. */
    public void saveExamYear(String examYear) {
        prefs.edit().putString(KEY_EXAM_YEAR, examYear == null ? "" : examYear).apply();
    }

    public String loadExamYear() {
        return prefs.getString(KEY_EXAM_YEAR, "");
    }
}
