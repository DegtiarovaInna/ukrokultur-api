package de.ukrokultur.ukrokultur_api.common.slug;


import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

public final class SlugGenerator {

    private SlugGenerator() {}

    public static String slugify(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        s = s.toLowerCase(Locale.ROOT);

        s = s.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "")
                .replaceAll("-{2,}", "-");

        return s.isBlank() ? null : s;
    }


    public static String generateUnique(String base, Predicate<String> exists) {
        String b = slugify(base);
        if (b == null) b = "item";

        String candidate = b;
        int i = 2;
        while (exists.test(candidate)) {
            candidate = b + "-" + i;
            i++;
        }
        return candidate;
    }
}
