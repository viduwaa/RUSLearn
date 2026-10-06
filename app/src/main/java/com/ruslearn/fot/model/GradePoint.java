package com.ruslearn.fot.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The FOT grading model, copied from the portal's GPA calculator so app and web give
 * the same numbers (NFR09).
 *
 * Grade points: A+/A=4.0, A-=3.7, B+=3.3, B=3.0, B-=2.7, C+=2.3, C=2.0, C-=1.7,
 * D+=1.3, D=1.0, E=0.0. Non-GPA subjects use Pass/Fail instead and are excluded from
 * GPA; a Pass still adds its credits to the completed count.
 */
public final class GradePoint {

    public static final String UNASSIGNED = "None";

    /** Grade lists for calculators. */
    public static final Map<String, Double> POINTS = createPoints();
    public static final List<String> GPA_GRADES = List.of(
            "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "E",
            UNASSIGNED);
    public static final List<String> NON_GPA_GRADES = List.of("Pass", "Fail", UNASSIGNED);

    /** Degree total credits (the fixed FOT programme length). */
    public static final int TOTAL_CREDITS = 120;

    private static Map<String, Double> createPoints() {
        Map<String, Double> points = new HashMap<>();
        points.put("A+", 4.0);
        points.put("A", 4.0);
        points.put("A-", 3.7);
        points.put("B+", 3.3);
        points.put("B", 3.0);
        points.put("B-", 2.7);
        points.put("C+", 2.3);
        points.put("C", 2.0);
        points.put("C-", 1.7);
        points.put("D+", 1.3);
        points.put("D", 1.0);
        points.put("E", 0.0);
        return points;
    }

    /** True when the grade has a GPA value (not None/Pass/Fail). */
    public static boolean hasPointValue(String grade) {
        return grade != null && POINTS.containsKey(grade);
    }

    /** 4.0 = First Class ... <2.0 = Not Eligible, portal labels verbatim. */
    public static String bandFor(double gpa) {
        if (gpa >= 3.7) return "First Class";
        if (gpa >= 3.3) return "Second Class (Upper)";
        if (gpa >= 3.0) return "Second Class (Lower)";
        if (gpa >= 2.0) return "General";
        return "Not Eligible";
    }

    /** Semester GPA: Σ(credits × points) / Σ(credits) over graded subjects only. */
    public static double gpaOf(List<Subject> subjects, Map<String, String> gradeByCode) {
        double pointsSum = 0;
        double creditsSum = 0;
        for (Subject subject : subjects) {
            if (subject.isNonGpa()) {
                continue;
            }
            String grade = gradeByCode.get(subject.getCode());
            if (hasPointValue(grade)) {
                pointsSum += subject.getCredits() * POINTS.get(grade);
                creditsSum += subject.getCredits();
            }
        }
        return creditsSum > 0 ? pointsSum / creditsSum : Double.NaN;
    }

    /** Overall GPA over every semester plus the completed-credit count. */
    public static Totals overallOf(
            Iterable<List<Subject>> allSubjects, Map<String, String> gradeByCode) {
        double pointsSum = 0;
        double gpaCredits = 0;
        int creditsCompleted = 0;

        for (List<Subject> subjects : allSubjects) {
            for (Subject subject : subjects) {
                String grade = gradeByCode.get(subject.getCode());
                if (subject.isNonGpa()) {
                    if ("Pass".equals(grade)) {
                        creditsCompleted += subject.getCredits();
                    }
                } else if (hasPointValue(grade)) {
                    pointsSum += subject.getCredits() * POINTS.get(grade);
                    gpaCredits += subject.getCredits();
                    creditsCompleted += subject.getCredits();
                }
            }
        }
        return new Totals(gpaCredits > 0 ? pointsSum / gpaCredits : Double.NaN,
                creditsCompleted);
    }

    /** GPA plus the completed credits it rested on. */
    public static final class Totals {
        public final double gpa;
        public final int creditsCompleted;

        Totals(double gpa, int creditsCompleted) {
            this.gpa = gpa;
            this.creditsCompleted = creditsCompleted;
        }
    }

    private GradePoint() {
        // Static helpers only.
    }
}
