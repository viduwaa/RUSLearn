# 02 - Paper/Filter/FilterOptions models with org.json parsing

Type: task
Status: resolved

## What

`model/Paper.java` with exactly the portal's fields (file_name, view_url, preview_url,
department_code, department_name, academic_year, semester, exam_year, course_code,
course_name) plus `driveFileId()` (regex on view_url) used later by FR14.
`model/PaperFilter.java` (year, semester, department, courseCode, searchQuery — nullable,
empty = absent). `model/FilterOptions.java` (years, semesters, departments — string lists).

## Why

Mirrors the portal record one-to-one so filter/search behaviour matches the website.

## Done

- Static `fromJson(JSONObject)` factories for Papers and FilterOptions.
- Field names in JSON responses stay snake_case from the API; model accessors use camelCase.
