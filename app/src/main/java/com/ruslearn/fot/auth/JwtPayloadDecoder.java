package com.ruslearn.fot.auth;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Decodes the payload of a Google ID token (JWT). Signature verification is
 * deliberately not performed: the token comes straight from Google Play Services into
 * the app process and is never sent to another server, so there is nothing the check
 * would protect (ADR-0001).
 */
public final class JwtPayloadDecoder {

    /** Payload claims as a JSON object; throws IllegalArgumentException on a malformed token. */
    public static JSONObject decode(String idToken) {
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Malformed ID token");
        }
        byte[] bytes = Base64.decode(parts[1],
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        try {
            return new JSONObject(new String(bytes, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Malformed ID token payload", e);
        }
    }

    private JwtPayloadDecoder() {
        // Static helpers only.
    }
}
