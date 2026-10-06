package com.ruslearn.fot.model;

/**
 * The papers search/filter values (FR02–FR04). {@code null} or an empty trimmed
 * string means "do not filter by this field", matching how the portal treats blanks.
 */
public class PaperFilter {

    private final String year;        // study year, e.g. "First Year"
    private final String semester;    // "Semester I" / "Semester II"
    private final String department;  // code, e.g. "ICT"
    private final String courseCode;  // partial subject-code match, e.g. "ICT 2308"
    private final String searchQuery; // free-text search on name/code/file

    public PaperFilter(String year, String semester, String department,
                       String courseCode, String searchQuery) {
        this.year = year;
        this.semester = semester;
        this.department = department;
        this.courseCode = courseCode;
        this.searchQuery = searchQuery;
    }

    public String getYear() { return year; }

    public String getSemester() { return semester; }

    public String getDepartment() { return department; }

    public String getCourseCode() { return courseCode; }

    public String getSearchQuery() { return searchQuery; }

    /** True when nothing is set — the request then returns every paper. */
    public boolean isEmpty() {
        return isBlank(year) && isBlank(semester) && isBlank(department)
                && isBlank(courseCode) && isBlank(searchQuery);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
