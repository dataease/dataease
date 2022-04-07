package io.dataease.commons.utils;

public class TableUtils {

    public static String tableName(String datasetId) {
        return "ds_" + datasetId.replace("-", "_");
    }

    public static String tmpName(String dorisName) {
        return "tmp_" + dorisName;
    }

    public static String deleteName(String dorisName) {
        return "delete_" + dorisName;
    }

    public static String addName(String dorisName) {
        return "add_" + dorisName;
    }

    public static String fieldName(String dorisName) {
        return "f_" + Md5Utils.md5(dorisName);
    }

    public static String fieldNameShort(String dorisName) {
        return "f_" + Md5Utils.md5(dorisName).substring(8, 24);
    }

    public static String columnName(String filedName) {
        return "C_" + Md5Utils.md5(filedName);
    }
}
