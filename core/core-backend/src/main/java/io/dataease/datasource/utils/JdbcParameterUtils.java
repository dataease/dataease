package io.dataease.datasource.utils;

import java.sql.Types;

public final class JdbcParameterUtils {
    private JdbcParameterUtils() {
    }

    public static Integer resolveType(String datasourceType, Integer deExtractType, Integer jdbcType) {
        if ("pg".equals(datasourceType) && Integer.valueOf(1).equals(deExtractType)
                && Integer.valueOf(Types.VARCHAR).equals(jdbcType)) {
            // 日期参数仍保留原始文本，由 PG 按比较字段推断 date/timestamp/timestamptz，避免引入 JVM 时区转换。
            return Types.OTHER;
        }
        return jdbcType;
    }
}
