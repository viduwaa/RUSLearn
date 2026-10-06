package com.ruslearn.fot.model;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * One department programme as delivered by the bundled subject catalog
 * (converted from the portal's lib/subjectList.ts). {@code semesters} keeps the
 * catalog's key order: "Y1S1", "Y1S2", ... "Y4S2".
 */
public class Program {

    private final String code;        // e.g. "ICT"
    private final String name;        // e.g. "Information & Communication Technology"
    private final LinkedHashMap<String, List<Subject>> semesters;

    public Program(String code, String name, LinkedHashMap<String, List<Subject>> semesters) {
        this.code = code;
        this.name = name;
        this.semesters = semesters;
    }

    public String getCode() { return code; }

    public String getName() { return name; }

    public LinkedHashMap<String, List<Subject>> getSemesters() { return semesters; }
}
