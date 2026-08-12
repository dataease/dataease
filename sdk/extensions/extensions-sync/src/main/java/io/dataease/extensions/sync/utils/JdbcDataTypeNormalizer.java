package io.dataease.extensions.sync.utils;

import io.dataease.extensions.sync.datatype.StandardDataType;
import org.apache.commons.lang3.StringUtils;

import java.sql.Types;
import java.util.Locale;

/**
 * JDBC 字段类型归一化工具
 *
 * @author jianneng
 */
public final class JdbcDataTypeNormalizer {

    private JdbcDataTypeNormalizer() {
    }

    /**
     * 根据数据库原生类型和 JDBC 元数据确定源字段的标准类型
     * 原生类型用于保留 JSON、UUID、无符号整数等数据库语义
     * JDBC 类型用于修正 YEAR、BIT、FLOAT 等驱动实际读取语义
     *
     * @param sourceType  数据库原生类型
     * @param jdbcType    JDBC 类型码
     * @param precision   数值精度
     * @param scale       数值小数位数
     * @param displaySize JDBC 显示长度
     * @return DataEase 源标准类型
     */
    public static StandardDataType normalize(
            String sourceType,
            int jdbcType,
            int precision,
            int scale,
            int displaySize) {
        StandardDataType nativeType = resolveNativeType(sourceType);
        if (nativeType == StandardDataType.YEAR) {
            return normalizeYear(jdbcType);
        }
        if (nativeType == StandardDataType.BIT) {
            return normalizeBit(jdbcType, precision, displaySize);
        }
        if (nativeType == StandardDataType.FLOAT) {
            return normalizeFloat(jdbcType);
        }
        if (nativeType == StandardDataType.REAL) {
            return jdbcType == Types.DOUBLE
                    ? StandardDataType.DOUBLE
                    : StandardDataType.REAL;
        }
        if (nativeType == StandardDataType.TINYINT
                && (jdbcType == Types.BOOLEAN || jdbcType == Types.BIT)) {
            return normalizeBit(jdbcType, precision, displaySize);
        }
        if (nativeType == StandardDataType.DATE
                && jdbcType == Types.TIMESTAMP) {
            return StandardDataType.TIMESTAMP;
        }
        if (nativeType == StandardDataType.TIMESTAMP
                && isBinaryJdbcType(jdbcType)) {
            return StandardDataType.BYTEA;
        }
        if (nativeType != StandardDataType.UNKNOWN) {
            return nativeType;
        }
        return normalizeJdbcType(jdbcType, precision, displaySize);
    }

    /**
     * 仅根据数据库原生类型确定源字段的标准类型
     * 适用于无法取得 JDBC 类型码的系统表查询结果
     *
     * @param sourceType 数据库原生类型
     * @return DataEase 源标准类型
     */
    public static StandardDataType normalizeNativeType(String sourceType) {
        return resolveNativeType(sourceType);
    }

    private static StandardDataType resolveNativeType(String sourceType) {
        String normalizedType = normalizeTypeName(sourceType);
        if (StringUtils.isBlank(normalizedType)) {
            return StandardDataType.UNKNOWN;
        }
        return switch (normalizedType) {
            case "BOOL" -> StandardDataType.BOOLEAN;
            case "INT2" -> StandardDataType.SMALLINT;
            case "INT4", "SIGNED" -> StandardDataType.INTEGER;
            case "INT8", "BIGSERIAL" -> StandardDataType.BIGINT;
            case "SERIAL", "SERIAL4" -> StandardDataType.INTEGER;
            case "SERIAL8" -> StandardDataType.BIGINT;
            case "FLOAT4" -> StandardDataType.REAL;
            case "FLOAT8" -> StandardDataType.DOUBLE;
            case "CHARACTER_VARYING" -> StandardDataType.VARCHAR;
            case "BIT_VARYING" -> StandardDataType.BIT;
            case "UNIQUEIDENTIFIER" -> StandardDataType.UUID;
            case "GEOGRAPHY", "MULTIPOINT", "MULTILINESTRING", "MULTIPOLYGON",
                    "GEOMETRYCOLLECTION", "GEOMCOLLECTION" -> StandardDataType.GEOMETRY;
            case "TIMESTAMP_WITH_TIME_ZONE" -> StandardDataType.TIMESTAMPTZ;
            case "TIMESTAMP_WITHOUT_TIME_ZONE" -> StandardDataType.TIMESTAMP;
            default -> StandardDataType.customValueOf(normalizedType);
        };
    }

    private static String normalizeTypeName(String sourceType) {
        String type = StringUtils.trimToEmpty(sourceType)
                .replaceAll("\\([^)]*\\)", "");
        return type.trim()
                .replaceAll("\\s+", "_")
                .toUpperCase(Locale.ROOT);
    }

    private static StandardDataType normalizeYear(int jdbcType) {
        return switch (jdbcType) {
            case Types.DATE -> StandardDataType.DATE;
            case Types.TINYINT, Types.SMALLINT -> StandardDataType.SMALLINT;
            case Types.INTEGER -> StandardDataType.INTEGER;
            case Types.BIGINT -> StandardDataType.BIGINT;
            default -> StandardDataType.UNKNOWN;
        };
    }

    private static StandardDataType normalizeBit(
            int jdbcType, int precision, int displaySize) {
        int bitLength = precision > 0 ? precision : displaySize;
        if (jdbcType == Types.BOOLEAN || bitLength == 1) {
            return StandardDataType.BOOLEAN;
        }
        if (jdbcType == Types.BIT && bitLength > 1) {
            return StandardDataType.BYTEA;
        }
        if (jdbcType == Types.BINARY
                || jdbcType == Types.VARBINARY
                || jdbcType == Types.LONGVARBINARY) {
            return StandardDataType.BYTEA;
        }
        return StandardDataType.UNKNOWN;
    }

    private static boolean isBinaryJdbcType(int jdbcType) {
        return jdbcType == Types.BINARY
                || jdbcType == Types.VARBINARY
                || jdbcType == Types.LONGVARBINARY
                || jdbcType == Types.BLOB;
    }

    private static StandardDataType normalizeFloat(int jdbcType) {
        if (jdbcType == Types.REAL) {
            return StandardDataType.REAL;
        }
        if (jdbcType == Types.FLOAT || jdbcType == Types.DOUBLE) {
            return StandardDataType.DOUBLE;
        }
        return StandardDataType.UNKNOWN;
    }

    private static StandardDataType normalizeJdbcType(
            int jdbcType, int precision, int displaySize) {
        return switch (jdbcType) {
            case Types.TINYINT -> StandardDataType.TINYINT;
            case Types.SMALLINT -> StandardDataType.SMALLINT;
            case Types.INTEGER -> StandardDataType.INTEGER;
            case Types.BIGINT -> StandardDataType.BIGINT;
            case Types.NUMERIC -> StandardDataType.NUMERIC;
            case Types.DECIMAL -> StandardDataType.DECIMAL;
            case Types.REAL -> StandardDataType.REAL;
            case Types.FLOAT -> StandardDataType.DOUBLE;
            case Types.DOUBLE -> StandardDataType.DOUBLE;
            case Types.BOOLEAN -> StandardDataType.BOOLEAN;
            case Types.BIT -> normalizeBit(jdbcType, precision, displaySize);
            case Types.CHAR -> StandardDataType.CHAR;
            case Types.VARCHAR -> StandardDataType.VARCHAR;
            case Types.LONGVARCHAR, Types.LONGNVARCHAR -> StandardDataType.TEXT;
            case Types.NCHAR -> StandardDataType.NCHAR;
            case Types.NVARCHAR -> StandardDataType.NVARCHAR;
            case Types.CLOB -> StandardDataType.CLOB;
            case Types.NCLOB -> StandardDataType.NCLOB;
            case Types.BINARY -> StandardDataType.BINARY;
            case Types.VARBINARY -> StandardDataType.VARBINARY;
            case Types.LONGVARBINARY, Types.BLOB -> StandardDataType.BLOB;
            case Types.DATE -> StandardDataType.DATE;
            case Types.TIME -> StandardDataType.TIME;
            case Types.TIMESTAMP -> StandardDataType.TIMESTAMP;
            case Types.TIMESTAMP_WITH_TIMEZONE -> StandardDataType.TIMESTAMPTZ;
            case Types.SQLXML -> StandardDataType.XML;
            case Types.ARRAY -> StandardDataType.ARRAY;
            case Types.ROWID -> StandardDataType.ROWID;
            default -> StandardDataType.UNKNOWN;
        };
    }
}
