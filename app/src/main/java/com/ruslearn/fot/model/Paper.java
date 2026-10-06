package com.ruslearn.fot.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One past-paper record from the portal's /api/papers response. Field names in JSON
 * stay snake_case exactly as the portal sends them; accessors use camelCase.
 * Implements Serializable so it travels between screens in an Intent.
 */
public class Paper implements Serializable {

    private static final Pattern DRIVE_FILE_ID = Pattern.compile("/file/d/([^/]+)");

    private final String fileName;
    private final String viewUrl;
    private final String previewUrl;
    private final String departmentCode;
    private final String departmentName;
    private final String academicYear;
    private final String semester;
    private final String examYear;
    private final String courseCode;
    private final String courseName;

    public Paper(String fileName, String viewUrl, String previewUrl, String departmentCode,
                 String departmentName, String academicYear, String semester, String examYear,
                 String courseCode, String courseName) {
        this.fileName = fileName;
        this.viewUrl = viewUrl;
        this.previewUrl = previewUrl;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.academicYear = academicYear;
        this.semester = semester;
        this.examYear = examYear;
        this.courseCode = courseCode;
        this.courseName = courseName;
    }

    public static Paper fromJson(JSONObject json) {
        return new Paper(
                json.optString("file_name", ""),
                json.optString("view_url", ""),
                json.optString("preview_url", ""),
                json.optString("department_code", ""),
                json.optString("department_name", ""),
                json.optString("academic_year", ""),
                json.optString("semester", ""),
                json.optString("exam_year", ""),
                json.optString("course_code", ""),
                json.optString("course_name", ""));
    }

    public String getFileName() { return fileName; }

    public String getViewUrl() { return viewUrl; }

    public String getPreviewUrl() { return previewUrl; }

    public String getDepartmentCode() { return departmentCode; }

    public String getDepartmentName() { return departmentName; }

    public String getAcademicYear() { return academicYear; }

    public String getSemester() { return semester; }

    public String getExamYear() { return examYear; }

    public String getCourseCode() { return courseCode; }

    public String getCourseName() { return courseName; }

    /**
     * Extracts the Google Drive file id from the view URL; the offline download
     * feature (FR14) builds download links from it.
     */
    public String getDriveFileId() {
        if (viewUrl == null || viewUrl.isEmpty()) {
            return null;
        }
        Matcher matcher = DRIVE_FILE_ID.matcher(viewUrl);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** Serialises back to the portal's JSON shape (used to snapshot this paper locally). */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("file_name", fileName);
            json.put("view_url", viewUrl);
            json.put("preview_url", previewUrl);
            json.put("department_code", departmentCode);
            json.put("department_name", departmentName);
            json.put("academic_year", academicYear);
            json.put("semester", semester);
            json.put("exam_year", examYear);
            json.put("course_code", courseCode);
            json.put("course_name", courseName);
        } catch (JSONException e) {
            // put() of plain strings cannot fail; keep the honesty anyway.
            throw new IllegalStateException("Paper JSON serialization failed", e);
        }
        return json;
    }
}
