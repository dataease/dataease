package io.dataease.dataset.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;

/** Validation for the independently maintained dataset semantic snapshot. */
final class DatasetSemanticValidation {
    private static final ObjectMapper JSON = new ObjectMapper();

    private DatasetSemanticValidation() {}

    static void text(String value, int max) {
        if (value != null && value.length() > max) invalid();
    }

    static void references(String value) {
        if (value == null || value.isEmpty()) return;
        text(value, 16000);
        try {
            var entries = JSON.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readTree(value);
            if (entries == null || !entries.isArray() || entries.size() > 20) invalid();
            for (var entry : entries) {
                var actual = entry.get("value");
                var meaning = entry.get("meaning");
                if (!entry.isObject() || actual == null || !actual.isTextual() || actual.asText().isBlank()) invalid();
                text(actual.asText(), 255);
                if (meaning != null && !meaning.isNull()) {
                    if (!meaning.isTextual()) invalid();
                    text(meaning.asText(), 255);
                }
            }
        } catch (java.io.IOException malformed) {
            invalid();
        }
    }

    private static void invalid() {
        DEException.throwException(Translator.get("i18n_datasource_metadata_required"));
    }
}
