package io.dataease.utils;

import org.apache.commons.lang3.StringUtils;

import static io.dataease.constant.XpackSettingConstants.DEFAULT_SPREADSHEET_PLUGIN_QUERY_LIMIT;
import static io.dataease.constant.XpackSettingConstants.DEFAULT_SPREADSHEET_QUERY_LIMIT;

public final class SpreadsheetQueryLimitUtils {

    private SpreadsheetQueryLimitUtils() {
    }

    public static long normalize(Long requestedLimit, String configuredLimit) {
        long normalizedRequestedLimit = requestedLimit != null && requestedLimit > 0
                ? requestedLimit
                : DEFAULT_SPREADSHEET_PLUGIN_QUERY_LIMIT;
        return Math.min(normalizedRequestedLimit, resolveConfiguredLimit(configuredLimit));
    }

    public static long resolveConfiguredLimit(String configuredLimit) {
        Long parsedConfiguredLimit = parsePositiveLong(configuredLimit);
        return parsedConfiguredLimit == null ? DEFAULT_SPREADSHEET_QUERY_LIMIT : parsedConfiguredLimit;
    }

    private static Long parsePositiveLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            long parsedValue = Long.parseLong(value.trim());
            return parsedValue > 0 ? parsedValue : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
