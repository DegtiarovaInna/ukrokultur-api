package de.ukrokultur.ukrokultur_api.support;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TestJson {
    private TestJson() {}

    public static String resource(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        try (InputStream is = TestJson.class.getResourceAsStream(p)) {
            if (is == null) throw new IllegalArgumentException("Resource not found: " + p);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}