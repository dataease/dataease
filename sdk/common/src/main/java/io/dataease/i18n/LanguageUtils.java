package io.dataease.i18n;

import io.dataease.constant.StaticResourceConstants;
import io.dataease.exception.DEException;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared language options and validation for explicit language changes. */
public final class LanguageUtils {
    private static final Pattern CUSTOM_FRONT_FILE = Pattern.compile("^custom_(.+)_front_(.+)\\.js$");
    private static final Map<String, String> BUILTINS = Map.of("zh-cn", "zh-CN", "en", "en", "tw", "tw");
    private static final Map<String, String> ALIASES = Map.of(
            "zh", "zh-CN", "zh-hans", "zh-CN", "zh-hant", "tw",
            "zh-tw", "tw", "zh-hk", "tw", "zh-mo", "tw", "en-us", "en", "en-gb", "en");

    private LanguageUtils() {
    }

    public static Map<String, String> customOptions() {
        Map<String, String> result = new LinkedHashMap<>();
        File[] files = new File(StaticResourceConstants.I18N_DIR).listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            Matcher matcher = CUSTOM_FRONT_FILE.matcher(file.getName());
            if (file.isFile() && matcher.matches()) {
                result.put(matcher.group(1).replace('_', '-'), matcher.group(2));
            }
        }
        return result;
    }

    public static String requireSupported(String language) {
        if (language == null || language.isBlank()) {
            DEException.throwException(Translator.get("i18n_language_required"));
        }
        String normalized = language.strip().replace('_', '-').toLowerCase(Locale.ROOT);
        String builtin = BUILTINS.get(normalized);
        if (builtin != null) {
            return builtin;
        }
        // Prefer an exact custom identifier to a builtin alias.
        for (String custom : customOptions().keySet()) {
            if (custom.toLowerCase(Locale.ROOT).equals(normalized)) {
                return custom;
            }
        }
        String alias = ALIASES.get(normalized);
        if (alias != null) {
            return alias;
        }
        DEException.throwException(Translator.get("i18n_language_unsupported"));
        return null;
    }
}
