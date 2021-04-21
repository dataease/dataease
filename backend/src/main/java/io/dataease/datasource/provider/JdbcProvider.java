package io.dataease.datasource.provider;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.MysqlConfigrationDTO;
import io.dataease.datasource.dto.SqlServerConfigration;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.request.DatasourceRequest;
import org.apache.arrow.util.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Service("jdbc")
public class JdbcProvider extends DatasourceProvider {

    private static Map<String, ArrayBlockingQueue<Connection>> jdbcConnection = new HashMap<>();
    private static int poolSize = 20;
    @Override
    public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(datasourceRequest.getQuery());
            list = fetchResult(rs);
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }finally {
            returnSource(connection, datasourceRequest.getDatasource().getId());
        }
        return list;
    }

    @VisibleForTesting
    public void exec(DatasourceRequest datasourceRequest) throws Exception {
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            stat.execute(datasourceRequest.getQuery());
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }finally {
            returnSource(connection, datasourceRequest.getDatasource().getId());
        }
    }


    @Override
    public ResultSet getDataResultSet(DatasourceRequest datasourceRequest) throws Exception {
        ResultSet rs;
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            rs = stat.executeQuery(datasourceRequest.getQuery());
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }finally {
            returnSource(connection, datasourceRequest.getDatasource().getId());
        }
        return rs;
    }

    @Override
    public List<String[]> getPageData(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(datasourceRequest.getQuery() + MessageFormat.format(" LIMIT {0}, {1}", (datasourceRequest.getStartPage() - 1) * datasourceRequest.getPageSize(), datasourceRequest.getPageSize()));
            list = fetchResult(rs);
        } catch (SQLException e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }finally {
            returnSource(connection, datasourceRequest.getDatasource().getId());
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
        Connection con = null;
        try {
            con = getConnectionFromPool(datasourceRequest);
            Statement ps = con.createStatement();
            ResultSet resultSet = ps.executeQuery(queryStr);
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
            return tables;
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }finally {
            returnSource(con, datasourceRequest.getDatasource().getId());
        }
    }

    @Override
    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        List<TableFiled> list = new LinkedList<>();
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
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
        }finally {
            returnSource(connection, datasourceRequest.getDatasource().getId());
        }
        return list;
    }

    @Override
    public void test(DatasourceRequest datasourceRequest) throws Exception {
        String queryStr = getTablesSql(datasourceRequest);
        Connection con = null;
        try {
            con = getConnection(datasourceRequest);
            Statement ps = con.createStatement();
            ResultSet resultSet = ps.executeQuery(queryStr);
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }finally {
            con.close();
        }
    }


    public Long count(DatasourceRequest datasourceRequest) throws Exception {
        Connection con = null;
        try {
            con = getConnectionFromPool(datasourceRequest); Statement ps = con.createStatement();
            ResultSet resultSet = ps.executeQuery(datasourceRequest.getQuery());
            while (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }finally {
            returnSource(con, datasourceRequest.getDatasource().getId());
        }
        return 0L;
    }

    private void returnSource(Connection connection, String dataSourceId) throws Exception{
        if(connection != null && !connection.isClosed()){
            ArrayBlockingQueue<Connection> connections = jdbcConnection.get(dataSourceId);
            connections.put(connection);
        }
    }

    private Connection getConnectionFromPool(DatasourceRequest datasourceRequest)throws Exception {
        ArrayBlockingQueue<Connection> connections = jdbcConnection.get(datasourceRequest.getDatasource().getId());
        if (connections == null) {
            initConnectionPool(datasourceRequest);
        }
        connections = jdbcConnection.get(datasourceRequest.getDatasource().getId());
        Connection co = connections.take();
        return co;
    }

    @Override
    public void initConnectionPool(DatasourceRequest datasourceRequest)throws Exception{
        ArrayBlockingQueue<Connection> connections = jdbcConnection.get(datasourceRequest.getDatasource().getId());
        if (connections == null) {
            connections = new ArrayBlockingQueue<>(poolSize);
            for (int i = 0; i < poolSize ; i++) {
                Connection connection = getConnection(datasourceRequest);
                connections.add(connection);
            }
            jdbcConnection.put(datasourceRequest.getDatasource().getId(), connections);
        }else {
            for (int i = 0; i < poolSize ; i++) {
                Connection connection = connections.take();
                connection.close();
                connection = getConnection(datasourceRequest);
                connections.add(connection);
            }
        }
    }

    private static Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
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
