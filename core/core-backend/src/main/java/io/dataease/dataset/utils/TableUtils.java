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
        String prefix = isCross ? Quoting.BACK_TICK.string : datasourceType.getPrefix();
        String suffix = isCross ? Quoting.BACK_TICK.string : datasourceType.getSuffix();
        String schema = StringUtils.isNotEmpty(sqlObj.getTableSchema())
                ? quoteIdentifier(sqlObj.getTableSchema(), prefix, suffix) + "."
                : "";
        return schema + quoteIdentifier(sqlObj.getTableName(), prefix, suffix) + " " + sqlObj.getTableAlias();
    }

    public static String tableName2Sql(DatasourceSchemaDTO ds, String tableName) {
        return "SELECT * FROM "
                + quoteIdentifier(ds.getSchemaAlias(), Quoting.BACK_TICK.string, Quoting.BACK_TICK.string)
                + "."
                + quoteIdentifier(tableName, Quoting.BACK_TICK.string, Quoting.BACK_TICK.string);
    }

    public static String quoteIdentifier(String name, String prefix, String suffix) {
        String quotePrefix = StringUtils.defaultIfEmpty(prefix, Quoting.BACK_TICK.string);
        String quoteSuffix = StringUtils.defaultIfEmpty(suffix, quotePrefix);
        String identifier = StringUtils.defaultString(name);
        if (StringUtils.isEmpty(quotePrefix) || StringUtils.isEmpty(quoteSuffix)) {
            return identifier;
        }
        return quotePrefix + StringUtils.replace(identifier, quoteSuffix, quoteSuffix + quoteSuffix) + quoteSuffix;
    }

    public static String quoteCompoundIdentifier(String name, String prefix, String suffix) {
        return Arrays.stream(StringUtils.splitPreserveAllTokens(StringUtils.defaultString(name), "."))
                .map(part -> quoteIdentifier(part, prefix, suffix))
                .collect(Collectors.joining("."));
    }
}
