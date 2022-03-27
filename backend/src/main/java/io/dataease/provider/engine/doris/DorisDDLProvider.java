package io.dataease.provider.engine.doris;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.utils.TableUtils;
import io.dataease.dto.datasource.DorisConfiguration;
import io.dataease.dto.datasource.JdbcConfiguration;
import io.dataease.dto.datasource.MysqlConfiguration;
import io.dataease.provider.DDLProviderImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("dorisEngineDDL")
public class DorisDDLProvider extends DDLProviderImpl {
    private static final String creatTableSql = "CREATE TABLE IF NOT EXISTS `TABLE_NAME`" +
            "Column_Fields" +
            "UNIQUE KEY(dataease_uuid)\n" +
            "DISTRIBUTED BY HASH(dataease_uuid) BUCKETS BUCKETS_NUM\n" +
            "PROPERTIES(\"replication_num\" = \"ReplicationNum\");";

    @Override
    public String createView(String name, String viewSQL) {
        return "CREATE VIEW IF NOT EXISTS " + name + " AS (" + viewSQL + ")";
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
       return  "ALTER TABLE DORIS_TABLE  REPLACE WITH TABLE DORIS_TMP_TABLE PROPERTIES('swap' = 'false')"
               .replace("DORIS_TABLE", name).replace("DORIS_TMP_TABLE", TableUtils.tmpName(name));
    }

    @Override
    public String createTableSql(String tableName, List<DatasetTableField> datasetTableFields, Datasource engine) {
        DorisConfiguration dorisConfiguration = new Gson().fromJson(engine.getConfiguration(), DorisConfiguration.class);
        String dorisTableColumnSql = createDorisTableColumnSql(datasetTableFields);
        return creatTableSql.replace("TABLE_NAME", tableName).replace("Column_Fields", dorisTableColumnSql)
                .replace("BUCKETS_NUM", dorisConfiguration.getBucketNum().toString())
                .replace("ReplicationNum", dorisConfiguration.getReplicationNum().toString());
    }

    private String createDorisTableColumnSql(final List<DatasetTableField> datasetTableFields) {
        StringBuilder Column_Fields = new StringBuilder("dataease_uuid  varchar(50), `");
        for (DatasetTableField datasetTableField : datasetTableFields) {
            Column_Fields.append(datasetTableField.getDataeaseName()).append("` ");
            Integer size = datasetTableField.getSize() * 3;
            if (datasetTableField.getSize() == 0 || datasetTableField.getSize() > 65533 || datasetTableField.getSize() * 3 > 65533) {
                size = 65533;
            }
            switch (datasetTableField.getDeExtractType()) {
                case 0:
                    Column_Fields.append("varchar(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
                case 1:
                    size  = size < 50? 50 : size;
                    Column_Fields.append("varchar(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
                case 2:
                    Column_Fields.append("bigint").append(",`");
                    break;
                case 3:
                    Column_Fields.append("DOUBLE").append(",`");
                    break;
                case 4:
                    Column_Fields.append("TINYINT(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
                default:
                    Column_Fields.append("varchar(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
            }
        }

        Column_Fields = new StringBuilder(Column_Fields.substring(0, Column_Fields.length() - 2));
        Column_Fields = new StringBuilder("(" + Column_Fields + ")\n");
        return Column_Fields.toString();
    }

}
