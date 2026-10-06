package com.ruslearn.fot.api;

/**
 * Single source of portal server URLs. The portal (past-paper-portal.pages.dev) is the
 * app's only backend surface — hot papers list, filter options and the feature-request
 * endpoint (paper requests).
 */
public final class ApiConfig {

    public static final String BASE_URL = "https://past-paper-portal.pages.dev";
    public static final String PAPERS_PATH = "/api/papers";
    public static final String FEATURE_REQUEST_PATH = "/api/feature-request";

    private ApiConfig() {
        // Static constants only.
    }
}
