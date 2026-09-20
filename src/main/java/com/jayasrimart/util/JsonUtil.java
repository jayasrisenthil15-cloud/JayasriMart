package com.jayasrimart.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility for JSON serialization and writing JSON responses to {@link HttpServletResponse}.
 */
public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .serializeNulls()
            .create();

    private JsonUtil() {
        // Prevent instantiation
    }

    public static Gson getGson() {
        return GSON;
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * Writes an object as JSON to the response output stream with appropriate headers and HTTP status code.
     *
     * @param response the HTTP servlet response
     * @param statusCode the HTTP status code
     * @param payload the object to serialize as JSON
     * @throws IOException if writing to the response fails
     */
    public static void writeJson(HttpServletResponse response, int statusCode, Object payload) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(GSON.toJson(payload));
            writer.flush();
        }
    }
}
