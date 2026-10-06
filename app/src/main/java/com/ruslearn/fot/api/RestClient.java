package com.ruslearn.fot.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Minimal HTTP client over {@link HttpURLConnection} — the course stack uses no
 * third-party networking libraries (ADR-0004). Call only from background threads;
 * UI code must post results back with {@code runOnUiThread}.
 */
public class RestClient {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    /** Raw HTTP result: status code plus the decoded body (empty when absent). */
    public static final class RawResponse {
        public final int code;
        public final String body;

        RawResponse(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }

    public static RawResponse get(String url) throws IOException {
        return send("GET", url, null);
    }

    public static RawResponse postJson(String url, String jsonBody) throws IOException {
        return send("POST", url, jsonBody);
    }

    private static RawResponse send(String method, String url, String jsonBody) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");
            if (jsonBody != null) {
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setDoOutput(true);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }
            return new RawResponse(connection.getResponseCode(), readBody(connection));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /** Reads the error stream for HTTP error codes, otherwise the normal stream. */
    private static String readBody(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        if (stream == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(line);
            }
        }
        return text.toString();
    }

    private RestClient() {
        // Static helpers only.
    }
}
