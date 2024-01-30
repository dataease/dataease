package io.dataease.provider.engine.doris;

import com.google.gson.Gson;
import io.dataease.plugins.common.dto.dataset.DataTableInfoDTO;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.commons.utils.TableUtils;
import io.dataease.dto.datasource.DorisConfiguration;
import io.dataease.provider.DDLProviderImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("dorisEngineDDL")
public class DorisDDLProvider extends DDLProviderImpl {
    private static final String creatTableSql = "CREATE TABLE IF NOT EXISTS `TABLE_NAME`" +
            "Column_Fields" +
            "UNIQUE KEY(`UNIQUE_KEY`)\n" +
            "DISTRIBUTED BY HASH(`DISTRIBUTED_BY_HASH`) BUCKETS BUCKETS_NUM\n" +
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
    public String replaceTable(String name) {
        return "ALTER TABLE DORIS_TABLE  REPLACE WITH TABLE DORIS_TMP_TABLE PROPERTIES('swap' = 'false')"
                .replace("DORIS_TABLE", name).replace("DORIS_TMP_TABLE", TableUtils.tmpName(name));
    }

    @Override
    public String createTableSql(DataTableInfoDTO dataTableInfoDTO, String tableName, List<DatasetTableField> datasetTableFields, Datasource engine, String version) {
        DorisConfiguration dorisConfiguration = new Gson().fromJson(engine.getConfiguration(), DorisConfiguration.class);

        String sql = creatTableSql.replace("TABLE_NAME", tableName)
                .replace("BUCKETS_NUM", dorisConfiguration.getBucketNum().toString())
                .replace("ReplicationNum", dorisConfiguration.getReplicationNum().toString());
        if (dataTableInfoDTO.isSetKey() && CollectionUtils.isNotEmpty(dataTableInfoDTO.getKeys())) {
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < dataTableInfoDTO.getKeys().size(); i++) {
                for (DatasetTableField datasetTableField : datasetTableFields) {
                    if (datasetTableField.getOriginName().equalsIgnoreCase(dataTableInfoDTO.getKeys().get(i))) {
                        keys.add(datasetTableField.getDataeaseName());
                    }
                }
            }
            sql = sql.replace("`UNIQUE_KEY`", "`" + String.join("`, `", keys) + "`")
                    .replace("DISTRIBUTED_BY_HASH", keys.get(0)).replace("Column_Fields", createDorisTableColumnSql(datasetTableFields, version, keys));
        } else {
            if(!datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.toList()).contains("dataease_uuid")){
                List<DatasetTableField> tempList = new ArrayList<>();
                DatasetTableField datasetTableField = new DatasetTableField();
                datasetTableField.setDeExtractType(0);
                datasetTableField.setType("String");
                datasetTableField.setDeType(0);
                datasetTableField.setDataeaseName("dataease_uuid");
                datasetTableField.setOriginName("dataease_uuid");
                datasetTableField.setSize(50);
                tempList.add(0, datasetTableField);
                for (int i = 0; i < datasetTableFields.size(); i++) {
                    tempList.add(datasetTableFields.get(i));
                }
                sql = sql.replace("UNIQUE_KEY", "dataease_uuid").replace("DISTRIBUTED_BY_HASH", "dataease_uuid").replace("Column_Fields", createDorisTableColumnSql(tempList, version, null));
            }else {
                sql = sql.replace("UNIQUE_KEY", "dataease_uuid").replace("DISTRIBUTED_BY_HASH", "dataease_uuid").replace("Column_Fields", createDorisTableColumnSql(datasetTableFields, version, null));
            }

        }

        return sql;
    }

    private String createDorisTableColumnSql(final List<DatasetTableField> datasetTableFields, String version, List<String> keys) {
        StringBuilder Column_Fields = new StringBuilder("`");
        for (DatasetTableField datasetTableField : datasetTableFields) {
            Column_Fields.append(datasetTableField.getDataeaseName()).append("` ");
            Integer size = datasetTableField.getSize() * 3;
            if (datasetTableField.getSize() == 0 || datasetTableField.getSize() > 65533 || datasetTableField.getSize() * 3 > 65533) {
                size = 65533;
            }
            switch (datasetTableField.getDeExtractType()) {
                case 0:
                    if (size <= 65533 || (keys != null && keys.contains(datasetTableField.getDataeaseName()))) {
                        Column_Fields.append("VARCHAR(length)".replace("length", String.valueOf(size))).append(",`");
                    } else {
                        Column_Fields.append("STRING".replace("length", String.valueOf(size))).append(",`");
                    }
                    break;
                case 1:
                    Column_Fields.append("DATETIME").append(",`");
                    break;
                case 2:
                    Column_Fields.append("bigint").append(",`");
                    break;
                case 3:
                    if (datasetTableField.getType().equalsIgnoreCase("DECIMAL") && datasetTableField.getAccuracy() != 0) {
                        if (Integer.valueOf(version.split("5.7.")[1]) < 99) {
                            Column_Fields.append("DECIMAL(" + datasetTableField.getSize() + "," + datasetTableField.getAccuracy() + ")").append(",`");
                        } else {
                            Column_Fields.append("DecimalV3(" + datasetTableField.getSize() + "," + datasetTableField.getAccuracy() + ")").append(",`");
                        }

                    } else {
                        Column_Fields.append("DECIMAL(27,8)").append(",`");
                    }
                    break;
                case 4:
                    Column_Fields.append("TINYINT(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
                default:
                    Column_Fields.append("STRING".replace("length", String.valueOf(size))).append(",`");
                    break;
            }
        }

        Column_Fields = new StringBuilder(Column_Fields.substring(0, Column_Fields.length() - 2));
        Column_Fields = new StringBuilder("(" + Column_Fields + ")\n");
        return Column_Fields.toString();
    }

}
