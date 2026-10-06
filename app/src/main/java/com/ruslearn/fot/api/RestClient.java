package com.ruslearn.fot.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    /** Raw HTTP result: status code, body decoded for JSON use, raw bytes for downloads. */
    public static final class RawResponse {
        public final int code;
        public final String body;
        public final byte[] bytes;

        RawResponse(int code, String body, byte[] bytes) {
            this.code = code;
            this.body = body == null ? "" : body;
            this.bytes = bytes == null ? new byte[0] : bytes;
        }
    }

    public static RawResponse get(String url) throws IOException {
        return send("GET", url, null, null);
    }

    /** GET with a bearer token (Drive API offline downloads, FR14). */
    public static RawResponse get(String url, String bearerToken) throws IOException {
        return send("GET", url, null, bearerToken);
    }

    public static RawResponse postJson(String url, String jsonBody) throws IOException {
        return send("POST", url, jsonBody, null);
    }

    private static RawResponse send(String method, String url, String jsonBody,
                                    String bearerToken) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");
            if (bearerToken != null) {
                connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }
            if (jsonBody != null) {
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setDoOutput(true);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }
            byte[] bytes = readAll(connection);
            return new RawResponse(connection.getResponseCode(),
                    new String(bytes, StandardCharsets.UTF_8), bytes);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /** Reads the error stream for HTTP error codes, otherwise the normal stream. */
    private static byte[] readAll(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        if (stream == null) {
            return new byte[0];
        }
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        try (InputStream in = new BufferedInputStream(stream)) {
            byte[] chunk = new byte[8_192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
        }
        return buffer.toByteArray();
    }

    private RestClient() {
        // Static helpers only.
    }
}
