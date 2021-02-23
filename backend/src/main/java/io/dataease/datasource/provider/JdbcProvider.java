package io.dataease.datasource.provider;

import com.google.gson.Gson;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.MysqlConfigrationDTO;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.request.DatasourceRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;

@Service("jdbc")
public class JdbcProvider extends DatasourceProvider{

    @Override
    public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();
        try (
            Connection connection = getConnection(datasourceRequest);
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(datasourceRequest.getQuery())
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    int columType = metaData.getColumnType(j + 1);
                    switch (columType) {
                        case java.sql.Types.DATE:
                            row[j] = rs.getDate(j + 1).toString();
                            break;
                        default:
                            row[j] = rs.getString(j + 1);
                            break;
                    }
                }
                list.add(row);
            }
        } catch (SQLException e){
            throw new Exception("ERROR:" + e.getMessage(), e);
        }catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<String> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<String> tables = new ArrayList<>();

        String queryStr = "show tables";
        if(StringUtils.isNotEmpty(datasourceRequest.getQuery())){
            queryStr = datasourceRequest.getQuery();
        }
        try (Connection con = getConnection(datasourceRequest); Statement ps = con.createStatement()) {
            ResultSet resultSet = ps.executeQuery(queryStr);
            while (resultSet.next()){
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }
        return tables;
    }

    @Override
    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception{
        List<TableFiled> list = new LinkedList<>();
        try (
            Connection connection = getConnection(datasourceRequest);
        ) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", datasourceRequest.getTable().toUpperCase(), "%");
            while (resultSet.next()) {
                String tableName=resultSet.getString("TABLE_NAME");
                String database = resultSet.getString("TABLE_CAT");
                if(tableName.equals(datasourceRequest.getTable()) && database.equalsIgnoreCase(getDatabase(datasourceRequest))){
                    TableFiled tableFiled = new TableFiled();
                    String colName = resultSet.getString("COLUMN_NAME");
                    tableFiled.setFieldName(colName);
                    String remarks = resultSet.getString("REMARKS");
                    if(remarks == null || remarks.equals("")){
                        remarks = colName;
                    }
                    tableFiled.setRemarks(remarks);
                    String dbType = resultSet.getString("TYPE_NAME");
                    tableFiled.setFieldType(dbType);
                    list.add(tableFiled);
                }
            }
        } catch (SQLException e){
            throw new Exception("ERROR:" + e.getMessage(), e);
        }catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return list;
    };

    @Override
    public void test(DatasourceRequest datasourceRequest) throws Exception {
        String queryStr = "show tables";
        try (Connection con = getConnection(datasourceRequest); Statement ps = con.createStatement()) {
            ResultSet resultSet = ps.executeQuery(queryStr);
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }
    }

    private Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        String username = null;
        String password = null;
        String driver = null;
        String jdbcurl = null;
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType){
            case mysql:
                MysqlConfigrationDTO mysqlConfigrationDTO  = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigrationDTO.class);
                username = mysqlConfigrationDTO.getUsername();
                password = mysqlConfigrationDTO.getPassword();
                driver = mysqlConfigrationDTO.getDriver();
                jdbcurl = mysqlConfigrationDTO.getJdbc();
                break;
            default:
                break;
        }

        Class.forName(driver);
        Properties props = new Properties();
        props.setProperty("user", username);
        if (StringUtils.isNotBlank(password)) {
            props.setProperty("password", password);
        }
        return DriverManager.getConnection(jdbcurl, props);
    }

    private String getDatabase(DatasourceRequest datasourceRequest){
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
                MysqlConfigrationDTO mysqlConfigrationDTO = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigrationDTO.class);
                return mysqlConfigrationDTO.getDataBase();
            default:
                return null;
        }
    }

    private static String getSchema(Connection conn) throws Exception {
        String schema;
        schema = conn.getMetaData().getUserName();
        System.out.println(schema);
        if ((schema == null) || (schema.length() == 0)) {
            throw new Exception("ORACLE数据库模式不允许为空");
        }
        return schema.toUpperCase().toString();
    }

    private static String changeDbType(String dbType) {
        dbType = dbType.toUpperCase();
        switch(dbType){
            case "VARCHAR":
            case "VARCHAR2":
            case "CHAR":
                return "1";
            case "NUMBER":
            case "DECIMAL":
                return "4";
            case "INT":
            case "SMALLINT":
            case "INTEGER":
                return "2";
            case "BIGINT":
                return "6";
            case "DATETIME":
            case "TIMESTAMP":
            case "DATE":
                return "7";
            default:
                return "1";
        }
    }


}
