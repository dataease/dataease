package io.dataease.commons.utils;

import io.dataease.utils.LogUtil;

import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;

public final class TimeZoneUtils {

    /**
     * Java 时区 ID 中会被 JVM 自动探测或用户配置命中、但 PostgreSQL 不接受的别名。
     * 统一映射到等价的 IANA 时区名称。
     */
    private static final Map<String, String> JAVA_ALIAS_TO_IANA = Map.ofEntries(
            Map.entry("PRC", "Asia/Shanghai"),
            Map.entry("ROC", "Asia/Taipei"),
            Map.entry("ROK", "Asia/Seoul"),
            Map.entry("Japan", "Asia/Tokyo"),
            Map.entry("Singapore", "Asia/Singapore"),
            Map.entry("Hongkong", "Asia/Hong_Kong"),
            Map.entry("Iceland", "Atlantic/Reykjavik"),
            Map.entry("Iran", "Asia/Tehran"),
            Map.entry("Israel", "Asia/Jerusalem"),
            Map.entry("Jamaica", "America/Jamaica"),
            Map.entry("Libya", "Africa/Tripoli"),
            Map.entry("Navajo", "America/Denver"),
            Map.entry("Poland", "Europe/Warsaw"),
            Map.entry("Portugal", "Europe/Lisbon"),
            Map.entry("Turkey", "Europe/Istanbul"),
            Map.entry("Egypt", "Africa/Cairo"),
            Map.entry("Cuba", "America/Havana"),
            Map.entry("Eire", "Europe/Dublin"),
            Map.entry("GB", "Europe/London"),
            Map.entry("GB-Eire", "Europe/London"),
            Map.entry("Zulu", "UTC"),
            Map.entry("UCT", "UTC"),
            Map.entry("Universal", "UTC"),
            Map.entry("W-SU", "Europe/Moscow"),
            Map.entry("Kwajalein", "Pacific/Kwajalein")
    );

    private TimeZoneUtils() {
    }

    /**
     * PostgreSQL JDBC 驱动连接时会原样发送 TimeZone.getDefault().getID()。
     * JVM 自动探测到的时区 ID 可能是 Java 专属别名（如 PRC、ROK），
     * 这些 ID 对 PostgreSQL 非法，需要在启动时规范化为 IANA 名称（如 Asia/Shanghai）。
     */
    public static void normalizeDefaultTimeZone() {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        if (defaultTimeZone == null) {
            return;
        }
        String currentId = defaultTimeZone.getID();
        if (isPostgresCompatible(currentId)) {
            return;
        }
        String replacement = resolveIanaId(currentId, defaultTimeZone);
        if (replacement == null || replacement.isEmpty() || replacement.equals(currentId)) {
            return;
        }
        TimeZone.setDefault(TimeZone.getTimeZone(replacement));
        LogUtil.info("Normalized default timezone from " + currentId + " to " + replacement);
    }

    private static String resolveIanaId(String currentId, TimeZone defaultTimeZone) {
        String mapped = JAVA_ALIAS_TO_IANA.get(currentId);
        if (mapped != null) {
            return mapped;
        }
        String shortId = ZoneId.SHORT_IDS.get(currentId);
        if (shortId != null) {
            return shortId;
        }
        return findSameRulesIanaId(defaultTimeZone);
    }

    /**
     * 对别名表覆盖不到的时区，寻找一个与当前时区规则一致的 IANA 时区名称。
     */
    private static String findSameRulesIanaId(TimeZone timeZone) {
        String fallback = null;
        for (String id : ZoneId.getAvailableZoneIds()) {
            if (!id.contains("/")) {
                continue;
            }
            TimeZone candidate = TimeZone.getTimeZone(id);
            if (candidate.getRawOffset() == timeZone.getRawOffset() && candidate.hasSameRules(timeZone)) {
                if ("Asia/Shanghai".equals(id)) {
                    return id;
                }
                if (fallback == null) {
                    fallback = id;
                }
            }
        }
        return fallback;
    }

    private static boolean isPostgresCompatible(String timeZoneId) {
        if (timeZoneId == null || timeZoneId.isEmpty()) {
            return false;
        }
        if ("UTC".equalsIgnoreCase(timeZoneId) || "GMT".equalsIgnoreCase(timeZoneId)) {
            return true;
        }
        if (timeZoneId.startsWith("GMT") && timeZoneId.length() > 3) {
            return true;
        }
        return timeZoneId.contains("/");
    }
}
