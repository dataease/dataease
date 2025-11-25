package io.dataease.extensions.sync.datatype;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 数据类型标准库枚举
 *
 * @author jianneng
 * @date 2025/11/18 19:09
 **/
public enum StandardDataType {
    // 数值类型
    TINYINT, SMALLINT, INTEGER, BIGINT, MEDIUMINT, INT,
    TINYINT_UNSIGNED, SMALLINT_UNSIGNED, INTEGER_UNSIGNED,
    BIGINT_UNSIGNED, MEDIUMINT_UNSIGNED, INT_UNSIGNED,
    FLOAT, DOUBLE, DECIMAL, NUMERIC, REAL, DOUBLE_PRECISION,
    DECIMAL_UNSIGNED, NUMERIC_UNSIGNED, DECFLOAT, NUMBER,
    BINARY_FLOAT, BINARY_DOUBLE, BYTE, SHORT, LONG_INT,
    UNSIGNED_LONG, HALF_FLOAT, SCALED_FLOAT,
    INT_IDENTITY, BIGINT_IDENTITY, SMALLINT_IDENTITY, TINYINT_IDENTITY, DECIMAL_IDENTITY, NUMERIC_IDENTITY,
    // 字符类型
    CHAR, VARCHAR, STRING, TEXT, CLOB, TINYTEXT, MEDIUMTEXT,
    LONGTEXT, TINYSTRING, MEDIUMSTRING, LONGSTRING, CHARACTER,
    VARCHAR2, NCHAR, LONG, NCLOB, NVARCHAR2, NVARCHAR, NTEXT,
    // 二进制类型
    BINARY, VARBINARY, BLOB, BYTEA, BIT, TINYBINARY, LONGBINARY,
    TINYBLOB, MEDIUMBLOB, LONGBLOB, LONGVARCHAR, RAW, LONG_RAW,
    // 日期时间类型
    DATE, TIME, DATETIME, TIMESTAMP, TIMESTAMPTZ, YEAR, DATETIME2,
    SMALLDATETIME, DATETIMEOFFSET,
    // 布尔类型
    BOOLEAN,
    // 枚举类型
    ENUM,
    // JSON/XML
    JSON, JSONB, XML,
    // 地理空间类型
    GEOMETRY, POINT, LINESTRING, POLYGON, GEO_POINT, GEO_SHAPE,
    // 网络地址类型
    INET, CIDR, MACADDR, IP,
    // UUID
    UUID,
    // 金额
    MONEY, SMALLMONEY,
    // 其他
    INTERVAL, SET, ARRAY, OBJECT, ROWID, UROWID, KEYWORD, NESTED,
    // 未知类型
    UNKNOWN;

    /**
     * 忽略大小写查找
     */
    public static StandardDataType customValueOf(String type) {
        return Arrays.stream(StandardDataType.values())
                .filter(item -> StringUtils.equalsIgnoreCase(item.name(), type.replace(" ", "_")))
                .findFirst()
                .orElse(StandardDataType.UNKNOWN);
    }
}
