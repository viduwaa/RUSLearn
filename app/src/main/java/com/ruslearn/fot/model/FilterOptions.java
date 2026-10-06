package com.ruslearn.fot.model;

import java.util.List;

/**
 * Values for the papers-filter dropdowns (FR04), as served by
 * {@code GET /api/papers?options=true}.
 */
public class FilterOptions {

    private final List<String> years;       // "First Year" ... "Fourth Year"
    private final List<String> semesters;   // "Semester I", "Semester II"
    private final List<String> departments; // "ICT", "BPT", ...

    public FilterOptions(List<String> years, List<String> semesters, List<String> departments) {
        this.years = years;
        this.semesters = semesters;
        this.departments = departments;
    }

    public List<String> getYears() { return years; }

    public List<String> getSemesters() { return semesters; }

    public List<String> getDepartments() { return departments; }
}
