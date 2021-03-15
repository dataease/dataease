package io.dataease.datasource.provider;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.MysqlConfigrationDTO;
import io.dataease.datasource.dto.SqlServerConfigration;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.request.DatasourceRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

@Service("jdbc")
public class JdbcProvider extends DatasourceProvider {


    @Override
    public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();
        try (
                Connection connection = getConnection(datasourceRequest);
                Statement stat = connection.createStatement();
                ResultSet rs = stat.executeQuery(datasourceRequest.getQuery())
        ) {
            list = fetchResult(rs);
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public ResultSet getDataResultSet(DatasourceRequest datasourceRequest) throws Exception {
        ResultSet rs;
        try {
            Connection connection = getConnection(datasourceRequest);
            Statement stat = connection.createStatement();
            rs = stat.executeQuery(datasourceRequest.getQuery());
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return rs;
    }

    @Override
    public List<String[]> getPageData(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();
        try (
                Connection connection = getConnection(datasourceRequest);
                Statement stat = connection.createStatement();
                ResultSet rs = stat.executeQuery(datasourceRequest.getQuery() + MessageFormat.format(" LIMIT {0}, {1}", (datasourceRequest.getStartPage() - 1) * datasourceRequest.getPageSize(), datasourceRequest.getPageSize()))
        ) {
            list = fetchResult(rs);
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<String[]> fetchResult(ResultSet rs) throws Exception {
        List<String[]> list = new LinkedList<>();
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
        return list;
    }

    @Override
    public List<TableFiled> fetchResultField(ResultSet rs) throws Exception {
        List<TableFiled> fieldList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int j = 0; j < columnCount; j++) {
            String f = metaData.getColumnName(j + 1);
            String l = StringUtils.isNotEmpty(metaData.getColumnLabel(j + 1)) ? metaData.getColumnLabel(j + 1) : f;
            String t = metaData.getColumnTypeName(j + 1);
            TableFiled field = new TableFiled();
            field.setFieldName(l);
            field.setRemarks(l);
            field.setFieldType(t);
            fieldList.add(field);
        }
        return fieldList;
    }

    @Override
    public List<String> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<String> tables = new ArrayList<>();
        String queryStr = getTablesSql(datasourceRequest);
        try (Connection con = getConnection(datasourceRequest); Statement ps = con.createStatement()) {
            ResultSet resultSet = ps.executeQuery(queryStr);
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }
        return tables;
    }

    @Override
    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        List<TableFiled> list = new LinkedList<>();
        try (
                Connection connection = getConnection(datasourceRequest);
        ) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", datasourceRequest.getTable().toUpperCase(), "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String database = resultSet.getString("TABLE_CAT");
                if (tableName.equals(datasourceRequest.getTable()) && database.equalsIgnoreCase(getDatabase(datasourceRequest))) {
                    TableFiled tableFiled = new TableFiled();
                    String colName = resultSet.getString("COLUMN_NAME");
                    tableFiled.setFieldName(colName);
                    String remarks = resultSet.getString("REMARKS");
                    if (remarks == null || remarks.equals("")) {
                        remarks = colName;
                    }
                    tableFiled.setRemarks(remarks);
                    String dbType = resultSet.getString("TYPE_NAME");
                    tableFiled.setFieldType(dbType);
                    list.add(tableFiled);
                }
            }
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return list;
    }

    ;

    @Override
    public void test(DatasourceRequest datasourceRequest) throws Exception {
        String queryStr = getTablesSql(datasourceRequest);
        try (Connection con = getConnection(datasourceRequest); Statement ps = con.createStatement()) {
            ResultSet resultSet = ps.executeQuery(queryStr);
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }
    }


    public Long count(DatasourceRequest datasourceRequest) throws Exception {
        try (Connection con = getConnection(datasourceRequest); Statement ps = con.createStatement()) {
            ResultSet resultSet = ps.executeQuery(datasourceRequest.getQuery());
            while (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }
        return 0L;
    }

    private Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        String username = null;
        String password = null;
        String driver = null;
        String jdbcurl = null;
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
                MysqlConfigrationDTO mysqlConfigrationDTO = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigrationDTO.class);
                username = mysqlConfigrationDTO.getUsername();
                password = mysqlConfigrationDTO.getPassword();
                driver = mysqlConfigrationDTO.getDriver();
                jdbcurl = mysqlConfigrationDTO.getJdbc();
                break;
            case sqlServer:
                SqlServerConfigration sqlServerConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfigration.class);
                username = sqlServerConfigration.getUsername();
                password = sqlServerConfigration.getPassword();
                driver = sqlServerConfigration.getDriver();
                jdbcurl = sqlServerConfigration.getJdbc();
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

    private String getDatabase(DatasourceRequest datasourceRequest) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
                MysqlConfigrationDTO mysqlConfigrationDTO = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigrationDTO.class);
                return mysqlConfigrationDTO.getDataBase();
            case sqlServer:
                SqlServerConfigration sqlServerConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfigration.class);
                return sqlServerConfigration.getDataBase();
            default:
                return null;
        }
    }

    private String getTablesSql(DatasourceRequest datasourceRequest) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
                return "show tables;";
            case sqlServer:
                return "SELECT TABLE_NAME FROM fit2cloud2.INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE';";
            default:
                return "show tables;";
        }
    }
}
