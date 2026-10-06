package com.ruslearn.fot.model;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One past-paper record from the portal's /api/papers response. Field names in JSON
 * stay snake_case exactly as the portal sends them; accessors use camelCase.
 */
public class Paper {

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
     * Extracts the Google Drive file id from the view URL; the offline-download
     * feature (FR14) builds download links from it.
     */
    public String getDriveFileId() {
        if (viewUrl == null || viewUrl.isEmpty()) {
            return null;
        }
        Matcher matcher = DRIVE_FILE_ID.matcher(viewUrl);
        return matcher.find() ? matcher.group(1) : null;
    }
}
