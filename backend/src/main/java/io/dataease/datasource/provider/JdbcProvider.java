package io.dataease.datasource.provider;

import com.google.gson.Gson;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.MysqlConfigration;
import io.dataease.datasource.dto.OracleConfigration;
import io.dataease.datasource.dto.SqlServerConfigration;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.*;

@Service("jdbc")
public class JdbcProvider extends DatasourceProvider {

    private static Map<String, ComboPooledDataSource> jdbcConnection = new HashMap<>();
    private static int initPoolSize = 5;
    private static int maxConnections = 200;

    /**
     * 增加缓存机制 key 由 'provider_sql_' dsr.datasource.id dsr.table dsr.query共4部分组成，命中则使用缓存直接返回不再执行sql逻辑
     * @param dsr
     * @return
     * @throws Exception
     */
    /**
     * 这里使用声明式缓存不是很妥当
     * 改为chartViewService中使用编程式缓存
    @Cacheable(
            value = JdbcConstants.JDBC_PROVIDER_KEY,
            key = "'provider_sql_' + #dsr.datasource.id + '_' + #dsr.table + '_' + #dsr.query",
            condition = "#dsr.pageSize == null || #dsr.pageSize == 0L"
    )
     */
    @Override
    public List<String[]> getData(DatasourceRequest dsr) throws Exception {
        List<String[]> list = new LinkedList<>();
        Connection connection = null;
        try {
            connection = getConnectionFromPool(dsr);
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(dsr.getQuery());
            list = fetchResult(rs);
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        } finally {
            if(connection != null){
                connection.close();
            }
        }
        return list;
    }

    public void exec(DatasourceRequest datasourceRequest) throws Exception {
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            Boolean result = stat.execute(datasourceRequest.getQuery());
            stat.close();
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        } finally {
            if(connection != null){
                connection.close();
            }
        }
    }

    @Override
    public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception {
        ResultSet rs;
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            rs = stat.executeQuery(datasourceRequest.getQuery());
            return fetchResult(rs);
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        } finally {
            if(connection != null){
                connection.close();
            }
        }
        return new ArrayList<>();
    }

    private List<String[]> fetchResult(ResultSet rs) throws Exception {
        List<String[]> list = new LinkedList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int j = 0; j < columnCount; j++) {
                int columType = metaData.getColumnType(j + 1);
                switch (columType) {
                    case Types.DATE:
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
    public List<TableFiled> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        ResultSet rs;
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            rs = stat.executeQuery(datasourceRequest.getQuery());
            return fetchResultField(rs);
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(Translator.get("i18n_datasource_connect_error") + e.getMessage());
        } finally {
            if(connection != null){
                connection.close();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        ResultSet rs;
        Map<String, List> result = new HashMap<>();
        Connection connection = null;
        List<String[]> dataList = new LinkedList<>();
        List<TableFiled> fieldList = new ArrayList<>();
        try {
            connection = getConnectionFromPool(datasourceRequest);
            Statement stat = connection.createStatement();
            rs = stat.executeQuery(datasourceRequest.getQuery());
            dataList = fetchResult(rs);
            fieldList = fetchResultField(rs);
            result.put("dataList", dataList);
            result.put("fieldList", fieldList);
            return result;
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        } finally {
            if(connection != null){
                connection.close();
            }
        }
        return new HashMap<>();
    }

    private List<TableFiled> fetchResultField(ResultSet rs) throws Exception {
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
            field.setFieldSize(metaData.getColumnDisplaySize(j + 1));
            if(t.equalsIgnoreCase("LONG")){field.setFieldSize(65533);} //oracle LONG
            if(StringUtils.isNotEmpty(t) && t.toLowerCase().contains("date") && field.getFieldSize() < 50 ){
                field.setFieldSize(50);
            }
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
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
            resultSet.close();
            statement.close();
            return tables;
        } catch (Exception e) {
            DataEaseException.throwException(e);
        } finally {
            if(con != null){
                con.close();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        List<String> schemas = new ArrayList<>();
        String queryStr = getSchemaSql(datasourceRequest);
        Connection con = null;
        try {
            con = getConnection(datasourceRequest);
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);
            while (resultSet.next()) {
                schemas.add(resultSet.getString(1));
            }
            resultSet.close();
            statement.close();
            return schemas;
        } catch (Exception e) {
            DataEaseException.throwException(e);
        } finally {
            if(con != null){
                con.close();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        List<TableFiled> list = new LinkedList<>();
        Connection connection = null;
        try {
            connection = getConnectionFromPool(datasourceRequest);
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", datasourceRequest.getTable(), "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String database = resultSet.getString("TABLE_CAT");
                if(database != null){
                    if (tableName.equals(datasourceRequest.getTable()) && database.equalsIgnoreCase(getDatabase(datasourceRequest))) {
                        TableFiled tableFiled = getTableFiled(resultSet);
                        list.add(tableFiled);
                    }
                }else {
                    if (tableName.equals(datasourceRequest.getTable())) {
                        TableFiled tableFiled = getTableFiled(resultSet);
                        list.add(tableFiled);
                    }
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        } finally {
            if(connection != null){
                connection.close();
            }
        }
        return list;
    }

    private TableFiled getTableFiled(ResultSet resultSet) throws SQLException {
        TableFiled tableFiled = new TableFiled();
        String colName = resultSet.getString("COLUMN_NAME");
        tableFiled.setFieldName(colName);
        String remarks = resultSet.getString("REMARKS");
        if (remarks == null || remarks.equals("")) {
            remarks = colName;
        }
        tableFiled.setRemarks(remarks);
        tableFiled.setFieldSize(Integer.valueOf(resultSet.getString("COLUMN_SIZE")));
        String dbType = resultSet.getString("TYPE_NAME");
        tableFiled.setFieldType(dbType);
        if(dbType.equalsIgnoreCase("LONG")){tableFiled.setFieldSize(65533);}
        if(StringUtils.isNotEmpty(dbType) && dbType.toLowerCase().contains("date") && tableFiled.getFieldSize() < 50 ){
            tableFiled.setFieldSize(50);
        }
        return tableFiled;
    }

    @Override
    public void checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        String queryStr = getTablesSql(datasourceRequest);
        Connection con = null;
        try {
            con = getConnection(datasourceRequest);
            Statement ps = con.createStatement();
            ResultSet resultSet = ps.executeQuery(queryStr);
            resultSet.close();
            ps.close();
        } catch (Exception e) {
            DataEaseException.throwException(Translator.get("i18n_datasource_connect_error") + e.getMessage());
        } finally {
            if(con != null){con.close();}
        }
    }


    public Long count(DatasourceRequest datasourceRequest) throws Exception {
        Connection con = null;
        try {
            con = getConnectionFromPool(datasourceRequest);
            Statement ps = con.createStatement();
            ResultSet resultSet = ps.executeQuery(datasourceRequest.getQuery());
            while (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (Exception e) {
            DataEaseException.throwException( e);
        } finally {
            con.close();
        }
        return 0L;
    }

    private Connection getConnectionFromPool(DatasourceRequest datasourceRequest) throws Exception {
        ComboPooledDataSource dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
        if (dataSource == null) {
            handleDatasource(datasourceRequest, "add");
        }
        dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
        Connection co = dataSource.getConnection();
        return co;
    }

    @Override
    public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception {
        ComboPooledDataSource dataSource = null;
        switch (type){
            case "add":
                checkStatus(datasourceRequest);
                dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
                if (dataSource == null) {
                    addToPool(datasourceRequest);
                }
                break;
            case "edit":
                dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
                if (dataSource != null) {
                    dataSource.close();
                }
                checkStatus(datasourceRequest);
                addToPool(datasourceRequest);
                break;
            case "delete":
                dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
                if (dataSource != null) {
                    dataSource.close();
                }
                break;
            default:
                break;
        }
    }

    private void addToPool(DatasourceRequest datasourceRequest) throws PropertyVetoException {
        ComboPooledDataSource dataSource;
        dataSource = new ComboPooledDataSource();
        setCredential(datasourceRequest, dataSource);
        dataSource.setMaxIdleTime(30); // 最大空闲时间
        dataSource.setAcquireIncrement(5);// 增长数
        dataSource.setInitialPoolSize(initPoolSize);// 初始连接数
        dataSource.setMinPoolSize(initPoolSize); // 最小连接数
        dataSource.setMaxPoolSize(maxConnections); // 最大连接数
        dataSource.setAcquireRetryAttempts(30);// 获取连接重试次数
        dataSource.setIdleConnectionTestPeriod(60); // 每60s检查数据库空闲连接
        dataSource.setMaxStatements(0); // c3p0全局的PreparedStatements缓存的大小
        dataSource.setBreakAfterAcquireFailure(false);  // 获取连接失败将会引起所有等待连接池来获取连接的线程抛出异常。但是数据源仍有效保留，并在下次调用getConnection()的时候继续尝试获取连接。如果设为true，那么在尝试获取连接失败后该数据源将申明已断开并永久关闭。Default: false
        dataSource.setTestConnectionOnCheckout(false); // 在每个connection 提交是校验有效性
        dataSource.setTestConnectionOnCheckin(true); // 取得连接的同时将校验连接的有效性
        dataSource.setCheckoutTimeout(60000); // 从连接池获取连接的超时时间，如设为0则无限期等待。单位毫秒，默认为0
//        dataSource.setPreferredTestQuery("SELECT 1");
        dataSource.setDebugUnreturnedConnectionStackTraces(true);
        dataSource.setUnreturnedConnectionTimeout(3600);
        jdbcConnection.put(datasourceRequest.getDatasource().getId(), dataSource);
    }

    private static Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        String username = null;
        String password = null;
        String driver = null;
        String jdbcurl = null;
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        Properties props = new Properties();
        switch (datasourceType) {
            case mysql:
                MysqlConfigration mysqlConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigration.class);
                username = mysqlConfigration.getUsername();
                password = mysqlConfigration.getPassword();
                driver = mysqlConfigration.getDriver();
                jdbcurl = mysqlConfigration.getJdbc();
                break;
            case doris:
                MysqlConfigration dorisConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigration.class);
                username = dorisConfigration.getUsername();
                password = dorisConfigration.getPassword();
                driver = dorisConfigration.getDriver();
                jdbcurl = dorisConfigration.getJdbc();
                break;
            case sqlServer:
                SqlServerConfigration sqlServerConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfigration.class);
                username = sqlServerConfigration.getUsername();
                password = sqlServerConfigration.getPassword();
                driver = sqlServerConfigration.getDriver();
                jdbcurl = sqlServerConfigration.getJdbc();
                break;
            case oracle:
                OracleConfigration oracleConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfigration.class);
                username = oracleConfigration.getUsername();
                password = oracleConfigration.getPassword();
                driver = oracleConfigration.getDriver();
                jdbcurl = oracleConfigration.getJdbc();
                props.put( "oracle.net.CONNECT_TIMEOUT" , "5000") ;
//                props.put( "oracle.jdbc.ReadTimeout" , "5000" ) ;
                break;
            default:
                break;
        }

        Class.forName(driver);
        props.setProperty("user", username);
        if (StringUtils.isNotBlank(password)) {
            props.setProperty("password", password);
        }
        return DriverManager.getConnection(jdbcurl, props);
    }


    private void setCredential(DatasourceRequest datasourceRequest, ComboPooledDataSource dataSource) throws PropertyVetoException {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
                MysqlConfigration mysqlConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigration.class);
                dataSource.setUser(mysqlConfigration.getUsername());
                dataSource.setDriverClass(mysqlConfigration.getDriver());
                dataSource.setPassword(mysqlConfigration.getPassword());
                dataSource.setJdbcUrl(mysqlConfigration.getJdbc());
                break;
            case doris:
                MysqlConfigration dorisConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigration.class);
                dataSource.setUser(dorisConfigration.getUsername());
                dataSource.setDriverClass(dorisConfigration.getDriver());
                dataSource.setPassword(dorisConfigration.getPassword());
                dataSource.setJdbcUrl(dorisConfigration.getJdbc());
                break;
            case sqlServer:
                SqlServerConfigration sqlServerConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfigration.class);
                dataSource.setUser(sqlServerConfigration.getUsername());
                dataSource.setDriverClass(sqlServerConfigration.getDriver());
                dataSource.setPassword(sqlServerConfigration.getPassword());
                dataSource.setJdbcUrl(sqlServerConfigration.getJdbc());
                break;
            case oracle:
                OracleConfigration oracleConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfigration.class);
                dataSource.setUser(oracleConfigration.getUsername());
                dataSource.setDriverClass(oracleConfigration.getDriver());
                dataSource.setPassword(oracleConfigration.getPassword());
                dataSource.setJdbcUrl(oracleConfigration.getJdbc());
                break;
            default:
                break;
        }
    }

    private String getDatabase(DatasourceRequest datasourceRequest) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
                MysqlConfigration mysqlConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigration.class);
                return mysqlConfigration.getDataBase();
            case doris:
                MysqlConfigration dorisConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfigration.class);
                return dorisConfigration.getDataBase();
            case sqlServer:
                SqlServerConfigration sqlServerConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfigration.class);
                return sqlServerConfigration.getDataBase();
            default:
                return null;
        }
    }

    private String getTablesSql(DatasourceRequest datasourceRequest) throws Exception {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
                return "show tables;";
            case doris:
                return "show tables;";
            case sqlServer:
                SqlServerConfigration sqlServerConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfigration.class);
                return "SELECT TABLE_NAME FROM DATABASE.INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE';".replace("DATABASE", sqlServerConfigration.getDataBase());
            case oracle:
                OracleConfigration oracleConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfigration.class);
                if(StringUtils.isEmpty(oracleConfigration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "select table_name, owner from all_tables where owner='" + oracleConfigration.getSchema() + "'";
            default:
                return "show tables;";
        }
    }

    private String getSchemaSql(DatasourceRequest datasourceRequest) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case oracle:
                return "select * from all_users";
            default:
                return "show tables;";
        }
    }
}
