package com.kdlt.platform.common;
import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private SlugUtils() {}

    // « Santé & Bien-être » devient « sante-bien-etre ».
    public static String slugify(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}