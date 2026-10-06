package com.ruslearn.fot.api;

import com.ruslearn.fot.model.FilterOptions;
import com.ruslearn.fot.model.Paper;
import com.ruslearn.fot.model.PaperFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Portal API client: past-paper listing with filters (FR02–FR04), filter dropdown
 * options and paper requests via the feature-request endpoint (FR13).
 *
 * Parsing problems are rethrown as {@link IOException} so UI code only has to catch
 * that for any network/parse failure.
 */
public class PaperApi {

    private static final String RATE_LIMIT_MESSAGE = "Too many requests. Please wait a moment.";
    private static final String SERVER_ERROR_MESSAGE = "Portal server error. Please try again.";

    /** GET /api/papers — filtered paper list; an empty filter returns all papers. */
    public static List<Paper> listPapers(PaperFilter filter) throws IOException {
        String url = ApiConfig.BASE_URL + ApiConfig.PAPERS_PATH + buildQuery(filter);
        RestClient.RawResponse response = RestClient.get(url);
        assertSuccess(response);

        try {
            JSONArray array = new JSONArray(response.body);
            List<Paper> papers = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                papers.add(Paper.fromJson(array.getJSONObject(i)));
            }
            return papers;
        } catch (JSONException e) {
            throw new IOException("Malformed papers response from the portal", e);
        }
    }

    /** GET /api/papers?options=true — values for the filter dropdowns. */
    public static FilterOptions getFilterOptions() throws IOException {
        String url = ApiConfig.BASE_URL + ApiConfig.PAPERS_PATH + "?options=true";
        RestClient.RawResponse response = RestClient.get(url);
        assertSuccess(response);

        try {
            JSONObject json = new JSONObject(response.body);
            return new FilterOptions(
                    readStringList(json, "years"),
                    readStringList(json, "semesters"),
                    readStringList(json, "departments"));
        } catch (JSONException e) {
            throw new IOException("Malformed options response from the portal", e);
        }
    }

    /** POST /api/feature-request — a student-submitted paper request (FR13). */
    public static void sendFeatureRequest(String text) throws IOException {
        try {
            String body = new JSONObject().put("request", text).toString();
            RestClient.RawResponse response = RestClient.postJson(
                    ApiConfig.BASE_URL + ApiConfig.FEATURE_REQUEST_PATH, body);
            if (response.code == 201) {
                return;
            }
            String error = new JSONObject(response.body).optString("error", null);
            throw new IOException(error != null
                    ? error
                    : "Could not send the request (HTTP " + response.code + ")");
        } catch (JSONException e) {
            throw new IOException("Malformed request response from the portal", e);
        }
    }

    private static void assertSuccess(RestClient.RawResponse response) throws IOException {
        if (response.code == 429) {
            throw new IOException(RATE_LIMIT_MESSAGE);
        }
        if (response.code >= 500) {
            throw new IOException(SERVER_ERROR_MESSAGE);
        }
        if (response.code != 200) {
            throw new IOException("Request failed (HTTP " + response.code + ")");
        }
    }

    /** Builds "?year=...&semester=..." skipping empty filter fields, URL-encoded. */
    private static String buildQuery(PaperFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return "";
        }
        StringBuilder query = new StringBuilder();
        appendParam(query, "year", filter.getYear());
        appendParam(query, "semester", filter.getSemester());
        appendParam(query, "department", filter.getDepartment());
        appendParam(query, "courseCode", filter.getCourseCode());
        appendParam(query, "searchQuery", filter.getSearchQuery());
        return query.toString();
    }

    private static void appendParam(StringBuilder query, String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        query.append(query.length() == 0 ? '?' : '&');
        query.append(key).append('=')
                .append(URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
    }

    private static List<String> readStringList(JSONObject json, String key) {
        JSONArray array = json.optJSONArray(key);
        List<String> values = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                values.add(array.optString(i, ""));
            }
        }
        return values;
    }
}
