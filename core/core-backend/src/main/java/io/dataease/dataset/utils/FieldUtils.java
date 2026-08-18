package io.dataease.dataset.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Junjun
 */
public class FieldUtils {
    public static int transType2DeType(final String type) {
        List<String> text = Arrays.asList("CHAR", "VARCHAR", "TEXT", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT", "LONGVARCHAR", "LONGNVARCHAR", "LONG VARCHAR", "LONG VARGRAPHIC", "ENUM", "ANY", "STRING", "POINT", "INT4RANGE", "INTERVAL", "BINARY_FLOAT", "BINARY_DOUBLE");
        List<String> time = Arrays.asList("DATE", "TIME", "YEAR", "DATETIME", "TIMESTAMP", "DATEV2", "DATETIMEV2", "DATETIME2", "DATETIMEOFFSET", "SMALLDATETIME", "DATETIME64", "_TIMESTAMPTZ", "TIMESTAMPTZ");
        List<String> num = Arrays.asList("INT", "SMALLINT", "MEDIUMINT", "INTEGER", "BIGINT", "LONG", "INT2", "INT4", "INT8", "int2", "int4", "int8", "INT16", "INT32", "INT64", "UINT8", "UINT16", "UINT32", "UINT64");
        List<String> doubleList = Arrays.asList("NUMBER", "FLOAT", "DOUBLE", "DECIMAL", "REAL", "MONEY", "NUMERIC", "float4", "float8", "FLOAT4", "FLOAT8", "DECFLOAT", "FLOAT32", "FLOAT64");
        List<String> boolType = Arrays.asList("BIT", "TINYINT", "BOOL", "BOOLEAN");
        if (boolType.contains(type)) {
            return 4;// 布尔
        }
        if (doubleList.contains(type)) {
            return 3;// 浮点
        }
        if (num.contains(type)) {
            return 2;// 整型
        }
        if (time.contains(type)) {
            return 1;// 时间
        }
        if (text.contains(type)) {
            return 0;// 文本
        }

        if (boolType.stream().anyMatch(l -> type.contains(l))) {
            return 4;// 布尔
        }
        if (doubleList.stream().anyMatch(l -> type.contains(l))) {
            return 3;// 浮点
        }
        if (text.stream().anyMatch(l -> type.contains(l))) {
            return 0;// 文本（如 INTERVAL DAY TO SECOND 包含 "INTERVAL"，避免被 "INT" 误判为整型）
        }
        if (num.stream().anyMatch(l -> type.contains(l))) {
            return 2;// 整型
        }
        if (time.stream().anyMatch(l -> type.contains(l))) {
            return 1;// 时间
        }
        return 0;// 文本
    }

    /**
     * 按数据源类型映射字段类型，解决同名类型在不同数据库语义不同的问题。
     * 例如 Oracle 的 LONG 是字符类型（最长 2GB），而 MySQL/MongoDB 的 LONG 是整型。
     */
    public static int transType2DeType(final String type, final String dsType) {
        if ("oracle".equalsIgnoreCase(dsType) && "LONG".equalsIgnoreCase(type)) {
            return 0;// 文本
        }
        // PostgreSQL/Kingbase 数组类型（_ 前缀，如 _int4、_text）按文本处理
        if (("pg".equalsIgnoreCase(dsType) || "kingbase".equalsIgnoreCase(dsType)) && type.startsWith("_")) {
            return 0;// 文本
        }
        // Doris/StarRocks 的 TINYINT 是真正的 1 字节整型，而非 MySQL 的布尔 TINYINT(1)
        if (("doris".equalsIgnoreCase(dsType) || "StarRocks".equalsIgnoreCase(dsType)) && "TINYINT".equalsIgnoreCase(type)) {
            return 2;// 整型
        }
        return transType2DeType(type);
    }

    public static String transDeType2DQ(int deType) {
        switch (deType) {
            case 0:
            case 1:
            case 5:
                return "d";
            case 2:
            case 3:
            case 4:
                return "q";
            default:
                return "d";
        }
    }
}
