package io.dataease.provider.engine.mysql;

import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.utils.TableUtils;
import io.dataease.provider.DDLProviderImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("mysqlEngineDDL")
public class MysqlDDLProvider extends DDLProviderImpl {

    private static final String creatTableSql =
            "CREATE TABLE IF NOT EXISTS `TABLE_NAME`" +
            "Column_Fields;" ;


    @Override
    public String createView(String name, String viewSQL) {
        return "CREATE or replace view " + name + " AS (" + viewSQL + ")";
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
                    if (size < 65533) {
                        Column_Fields.append("varchar(length)".replace("length", String.valueOf(datasetTableField.getSize()))).append(",`");
                    }else {
                        Column_Fields.append("longtext").append(",`");
                    }
                    break;
                case 1:
                    size  = size < 50? 50 : size;
                    if (size < 65533) {
                        Column_Fields.append("varchar(length)".replace("length", String.valueOf(datasetTableField.getSize()))).append(",`");
                    }else {
                        Column_Fields.append("longtext").append(",`");
                    }
                    break;
                case 2:
                    Column_Fields.append("varchar(100)").append(",`");
                    break;
                case 3:
                    Column_Fields.append("varchar(100)").append(",`");
                    break;
                case 4:
                    Column_Fields.append("TINYINT(length)".replace("length", String.valueOf(datasetTableField.getSize()))).append(",`");
                    break;
                default:
                    if (size < 65533) {
                        Column_Fields.append("varchar(length)".replace("length", String.valueOf(datasetTableField.getSize()))).append(",`");
                    }else {
                        Column_Fields.append("longtext").append(",`");
                    }
                    break;
            }
        }

        Column_Fields = new StringBuilder(Column_Fields.substring(0, Column_Fields.length() - 1)).append("PRIMARY KEY(dataease_uuid)");
        Column_Fields = new StringBuilder("(" + Column_Fields + ")\n");
        return Column_Fields.toString();
    }
}
