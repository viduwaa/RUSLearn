package com.ruslearn.fot.model;

/**
 * One subject from the FOT curriculum handbook (2022). The {@code type} values are
 * exactly the portal's raw values: "C" (core), "O" (optional), "Non GPA"
 * (graded Pass/Fail and excluded from GPA calculations).
 */
public class Subject {

    private final String code;    // e.g. "ICT 2308"
    private final String name;    // e.g. "Database Systems"
    private final int credits;    // e.g. 3
    private final String type;    // "C", "O" or "Non GPA"

    public Subject(String code, String name, int credits, String type) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.type = type;
    }

    public String getCode() { return code; }

    public String getName() { return name; }

    public int getCredits() { return credits; }

    public String getType() { return type; }

    /** True when the subject does not carry GPA grade points. */
    public boolean isNonGpa() {
        return "Non GPA".equals(type);
    }
}
