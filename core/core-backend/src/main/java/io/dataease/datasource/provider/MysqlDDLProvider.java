package io.dataease.datasource.provider;


import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.commons.exception.DataEaseException;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.model.TableField;

import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.request.EngineRequest;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("mysqlEngineDDL")
public class MysqlDDLProvider extends DDLProvider {


    public void exec(EngineRequest engineRequest) throws Exception {
        JsonNode rootNode = objectMapper.readTree(engineRequest.getEngine().getConfiguration());
        int queryTimeout = Integer.valueOf(rootNode.get("queryTimeout").asText());
        try (Connection connection = getConnection(engineRequest.getEngine().getConfiguration()); Statement stat = getStatement(connection, queryTimeout)) {
            Boolean result = stat.execute(engineRequest.getQuery());
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
    }
    private static final String creatTableSql =
            "CREATE TABLE IF NOT EXISTS `TABLE_NAME`" +
            "Column_Fields;" ;


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
                } else {
                    strings1[i] = strings[i].replace("'", "\\'");
                }
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
    public String createTableSql(String tableName, List<TableField> tableFields, CoreDeEngine engine) {
        String dorisTableColumnSql = createTableSql(tableFields);
        return creatTableSql.replace("TABLE_NAME", tableName).replace("Column_Fields", dorisTableColumnSql);
    }

    private String createTableSql(final List<TableField> tableFields) {
        StringBuilder Column_Fields = new StringBuilder("dataease_uuid  varchar(50), `");
        for (TableField tableField : tableFields) {
            Column_Fields.append(tableField.getDbFieldName()).append("` ");
            Long size = tableField.getSize() * 4;
            switch ((int) tableField.getDeType()) {
                case 0:
                    Column_Fields.append("longtext").append(",`");
                    break;
                case 1:
                    size  = size < 50? 50 : size;
                    if (size < 65533) {
                        Column_Fields.append("varchar(length)".replace("length", String.valueOf(tableField.getSize()))).append(",`");
                    }else {
                        Column_Fields.append("longtext").append(",`");
                    }
                    break;
                case 2:
                    Column_Fields.append("bigint(20)").append(",`");
                    break;
                case 3:
                    Column_Fields.append("varchar(100)").append(",`");
                    break;
                case 4:
                    Column_Fields.append("TINYINT(length)".replace("length", String.valueOf(tableField.getSize()))).append(",`");
                    break;
                default:
                    if (size < 65533) {
                        Column_Fields.append("varchar(length)".replace("length", String.valueOf(tableField.getSize()))).append(",`");
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
