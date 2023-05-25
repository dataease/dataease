package io.dataease.provider.engine.mysql;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.commons.utils.TableUtils;
import io.dataease.provider.DDLProviderImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("mysqlEngineDDL")
public class MysqlDDLProvider extends DDLProviderImpl {

    private static final String creatTableSql =
            "CREATE TABLE IF NOT EXISTS `TABLE_NAME`" +
            "Column_Fields" + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;" ;


    @Override
    public String createView(String name, String viewSQL) {
        return "CREATE or replace view " + name + " AS (" + viewSQL + ")";
    }

    @Override
    public String insertSql(String name, List<String[]> dataList, int page, int pageNumber) {
        String insertSql = "INSERT INTO TABLE_NAME VALUES ".replace("TABLE_NAME", name);
        StringBuffer values = new StringBuffer();

        Integer realSize = page * pageNumber < dataList.size() ? page * pageNumber : dataList.size();
        for (String[] strings : dataList.subList((page - 1) * pageNumber, realSize)) {
            String[] strings1 = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                if (StringUtils.isEmpty(strings[i])) {
                    strings1[i] = null;
                    continue;
                }
                strings1[i] = strings[i].replace("\\", "\\\\").replace("'", "\\'");
            }
            values.append("('").append(UUID.randomUUID())
                    .append("','").append(String.join("','", Arrays.asList(strings1)))
                    .append("'),");
        }
        return (insertSql + values.substring(0, values.length() - 1)).replaceAll(",'null'",  ",null");
    }



    @Override
    public String dropTable(String name) {
        return "DROP TABLE IF EXISTS " + name;
    }

    @Override
    public String dropView(String name) {
        return "DROP VIEW IF EXISTS " + name;
    }

    @Override
    public String replaceTable(String name){
        String replaceTableSql = "rename table FROM_TABLE to FROM_TABLE_tmp, TO_TABLE to FROM_TABLE, FROM_TABLE_tmp to TO_TABLE"
                .replace("FROM_TABLE", name).replace("TO_TABLE", TableUtils.tmpName(name));
        String dropTableSql = "DROP TABLE IF EXISTS " + TableUtils.tmpName(name);
        return  replaceTableSql + ";" + dropTableSql;
    }

    @Override
    public String createTableSql(String tableName, List<DatasetTableField> datasetTableFields, Datasource engine) {
        String dorisTableColumnSql = createDorisTableColumnSql(datasetTableFields);
        return creatTableSql.replace("TABLE_NAME", tableName).replace("Column_Fields", dorisTableColumnSql);
    }

    private String createDorisTableColumnSql(final List<DatasetTableField> datasetTableFields) {
        StringBuilder Column_Fields = new StringBuilder("dataease_uuid  varchar(50), `");
        for (DatasetTableField datasetTableField : datasetTableFields) {
            Column_Fields.append(datasetTableField.getDataeaseName()).append("` ");
            Integer size = datasetTableField.getSize() * 4;
            switch (datasetTableField.getDeExtractType()) {
                case 0:
                    Column_Fields.append("longtext").append(",`");
                    break;
                case 1:
                    size  = size < 50? 50 : size;
                    Column_Fields.append("longtext").append(",`");
                    break;
                case 2:
                    Column_Fields.append("bigint(20)").append(",`");
                    break;
                case 3:
                    Column_Fields.append("longtext").append(",`");
                    break;
                case 4:
                    Column_Fields.append("TINYINT(length)".replace("length", String.valueOf(datasetTableField.getSize()))).append(",`");
                    break;
                default:
                    Column_Fields.append("longtext").append(",`");
                    break;
            }
        }

        Column_Fields = new StringBuilder(Column_Fields.substring(0, Column_Fields.length() - 1)).append("PRIMARY KEY(dataease_uuid)");
        Column_Fields = new StringBuilder("(" + Column_Fields + ")\n");
        return Column_Fields.toString();
    }
}
