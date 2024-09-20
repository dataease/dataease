package io.dataease.dataset.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Junjun
 */
public class FieldUtils {
    public static int transType2DeType(final String type) {
        List<String> text = Arrays.asList("CHAR", "VARCHAR", "TEXT", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT", "ENUM", "ANY", "STRING", "BOOL", "BOOLEAN");
        List<String> time = Arrays.asList("DATE", "TIME", "YEAR", "DATETIME", "TIMESTAMP", "DATEV2", "DATETIMEV2", "DATETIME2", "DATETIMEOFFSET", "SMALLDATETIME", "DATETIME64", "_TIMESTAMPTZ", "TIMESTAMPTZ");
        List<String> num = Arrays.asList("INT", "SMALLINT", "MEDIUMINT", "INTEGER", "BIGINT", "LONG", "INT2", "INT4", "INT8", "int2", "int4", "int8", "INT16", "INT32", "INT64", "UINT8", "UINT16", "UINT32", "UINT64");
        List<String> doubleList = Arrays.asList("NUMBER", "FLOAT", "DOUBLE", "DECIMAL", "REAL", "MONEY", "NUMERIC", "float4", "float8", "FLOAT4", "FLOAT8", "DECFLOAT", "FLOAT32", "FLOAT64");
        List<String> boolType = Arrays.asList("BIT", "TINYINT");
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
        if (num.stream().anyMatch(l -> type.contains(l))) {
            return 2;// 整型
        }
        if (time.stream().anyMatch(l -> type.contains(l))) {
            return 1;// 时间
        }
        return 0;// 文本
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
