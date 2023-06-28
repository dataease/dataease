package io.dataease.dataset.utils;

import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.utils.Md5Utils;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.commons.lang3.StringUtils;

public class TableUtils {

    public static String format = Quoting.BACK_TICK.string + "%s" + Quoting.BACK_TICK.string;
    public static String tableName(String name) {
        return name;
    }

    public static String tmpName(String name) {
        return "tmp_" + name;
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
        return schema + "`" + sqlObj.getTableName() + "` " + sqlObj.getTableAlias();
    }

    public static String tableName2Sql(DatasourceSchemaDTO ds, String tableName) {
        return "SELECT * FROM " + ds.getSchemaAlias() + "." + String.format(format, tableName);
    }
}
