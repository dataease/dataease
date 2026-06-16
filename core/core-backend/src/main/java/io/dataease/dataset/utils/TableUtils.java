package io.dataease.dataset.utils;

import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.DsTypeDTO;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.utils.Md5Utils;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    public static String getTableAndAlias(SQLObj sqlObj, DsTypeDTO datasourceType, boolean isCross) {
        String schema = "";
        String prefix = "";
        String suffix = "";
        if (StringUtils.isNotEmpty(sqlObj.getTableSchema())) {
            if (isCross) {
                prefix = "`";
                suffix = "`";
            } else {
                prefix = datasourceType.getPrefix();
                suffix = datasourceType.getSuffix();
            }
            schema = prefix + sqlObj.getTableSchema() + suffix + ".";
        }
        return schema + prefix + sqlObj.getTableName() + suffix + " " + sqlObj.getTableAlias();
    }

    public static String tableName2Sql(DatasourceSchemaDTO ds, String tableName) {
        return "SELECT * FROM " + ds.getSchemaAlias() + "." + String.format(format, tableName);
    }

    public static String quoteIdentifier(String name, String prefix, String suffix) {
        String resolvedPrefix = StringUtils.defaultString(prefix);
        String resolvedSuffix = StringUtils.defaultString(suffix);
        if (StringUtils.isEmpty(resolvedPrefix) && StringUtils.isEmpty(resolvedSuffix)) {
            resolvedPrefix = Quoting.BACK_TICK.string;
            resolvedSuffix = Quoting.BACK_TICK.string;
        }
        String escapedName = StringUtils.defaultString(name);
        if (StringUtils.isNotEmpty(resolvedSuffix)) {
            escapedName = escapedName.replace(resolvedSuffix, resolvedSuffix + resolvedSuffix);
        }
        return resolvedPrefix + escapedName + resolvedSuffix;
    }

    public static String quoteCompoundIdentifier(String name, String prefix, String suffix) {
        return Arrays.stream(StringUtils.defaultString(name).split("\\.", -1))
                .map(part -> quoteIdentifier(part, prefix, suffix))
                .collect(Collectors.joining("."));
    }
}
