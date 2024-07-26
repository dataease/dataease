package io.dataease.dataset.utils;

/**
 * @Author Junjun
 */
public class FieldUtils {
    public static int transType2DeType(String type) {
        type = type.replaceAll("\\((.*?)\\)","");
        switch (type) {
            case "CHAR":
            case "VARCHAR":
            case "TEXT":
            case "TINYTEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "ENUM":
            case "ANY":
            case "STRING":
            case "BOOL":
            case "BOOLEAN":
                return 0;// 文本
            case "DATE":
            case "TIME":
            case "YEAR":
            case "DATETIME":
            case "TIMESTAMP":
            case "DATEV2":
            case "DATETIMEV2":
            case "DATETIME2":
            case "DATETIMEOFFSET":
            case "SMALLDATETIME":
            case "DATETIME64":
                return 1;// 时间
            case "INT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INTEGER":
            case "BIGINT":
            case "LONG": //增加了LONG类型
            case "INT2":
            case "INT4":
            case "INT8":
            case "int2":
            case "int4":
            case "int8":
            case "INT16":
            case "INT32":
            case "INT64":
            case "UINT8":
            case "UINT16":
            case "UINT32":
            case "UINT64":
                return 2;// 整型
            case "NUMBER":
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
            case "REAL":
            case "MONEY":
            case "NUMERIC":
            case "float4":
            case "float8":
            case "FLOAT4":
            case "FLOAT8":
            case "DECFLOAT":
            case "FLOAT32":
            case "FLOAT64":
                return 3;// 浮点
            case "BIT":
            case "TINYINT":
                return 4;// 布尔
            default:
                return 0;
        }
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
