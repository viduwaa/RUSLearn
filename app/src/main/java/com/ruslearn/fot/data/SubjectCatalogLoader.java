package com.ruslearn.fot.data;

import android.content.Context;

import com.ruslearn.fot.R;
import com.ruslearn.fot.model.Program;
import com.ruslearn.fot.model.Subject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the semester-wise subject catalog shipped in res/raw/subjects.json
 * (generated from the portal's lib/subjectList.ts). The catalog is read-only,
 * so it is parsed once and kept in memory for process lifetime.
 */
public class SubjectCatalogLoader {

    private static Map<String, Program> programs;

    /** Programme code -> Program, in catalog order. Safe to call from any thread. */
    public static synchronized Map<String, Program> load(Context context) {
        if (programs != null) {
            return programs;
        }
        try {
            JSONObject root = new JSONObject(readRaw(context));
            programs = new LinkedHashMap<>();
            for (Iterator<String> it = root.keys(); it.hasNext(); ) {
                String code = it.next();
                JSONObject jsonProgram = root.getJSONObject(code);
                programs.put(code, new Program(
                        code,
                        jsonProgram.optString("name", code),
                        readSemesters(jsonProgram)));
            }
            return programs;
        } catch (JSONException | IOException e) {
            throw new IllegalStateException("Corrupted subject catalog asset", e);
        }
    }

    private static LinkedHashMap<String, List<Subject>> readSemesters(JSONObject jsonProgram)
            throws JSONException {
        LinkedHashMap<String, List<Subject>> semesters = new LinkedHashMap<>();
        JSONObject jsonSemesters = jsonProgram.getJSONObject("semesters");
        for (Iterator<String> it = jsonSemesters.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONArray jsonSubjects = jsonSemesters.getJSONArray(key);
            List<Subject> subjects = new ArrayList<>();
            for (int i = 0; i < jsonSubjects.length(); i++) {
                JSONObject jsonSubject = jsonSubjects.getJSONObject(i);
                subjects.add(new Subject(
                        jsonSubject.optString("code", ""),
                        jsonSubject.optString("name", ""),
                        jsonSubject.optInt("credits", 0),
                        jsonSubject.optString("type", "C")));
            }
            semesters.put(key, subjects);
        }
        return semesters;
    }

    private static String readRaw(Context context) throws IOException {
        InputStream in = context.getResources().openRawResource(R.raw.subjects);
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append('\n');
            }
        }
        return text.toString();
    }
}
