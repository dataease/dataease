package io.dataease.dataset.utils;

import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.utils.Md5Utils;
import org.apache.commons.lang3.StringUtils;

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

    public static String columnName(String fieldName) {
        return "C_" + Md5Utils.md5(fieldName);
    }

    public static String getTableAndAlias(SQLObj sqlObj) {
        String schema = "";
        if (StringUtils.isNotEmpty(sqlObj.getTableSchema())) {
            schema = sqlObj.getTableSchema() + ".";
        }
        return schema + sqlObj.getTableName() + " " + sqlObj.getTableAlias();
    }
}
