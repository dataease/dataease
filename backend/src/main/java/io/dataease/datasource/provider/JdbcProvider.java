package io.dataease.datasource.provider;

import com.google.gson.Gson;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.*;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.provider.QueryProvider;
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
            connection = getConnection(dsr);
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(dsr.getQuery());
            list = fetchResult(rs);

            if(dsr.isPageable() && dsr.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.sqlServer.name())){
                Integer realSize = dsr.getPage() * dsr.getPageSize() < list.size() ? dsr.getPage() * dsr.getPageSize(): list.size();
                list = list.subList((dsr.getPage() - 1) * dsr.getPageSize(), realSize);
            }

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
                        if(rs.getDate(j + 1) != null){
                            row[j] = rs.getDate(j + 1).toString();
                        }
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
            return fetchResultField(rs, datasourceRequest);
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
            fieldList = fetchResultField(rs, datasourceRequest);
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

    private List<TableFiled> fetchResultField(ResultSet rs, DatasourceRequest datasourceRequest) throws Exception {
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

            if(datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.ck.name())){
                QueryProvider qp = ProviderFactory.getQueryProvider(datasourceRequest.getDatasource().getType());
                field.setFieldSize(qp.transFieldSize(t));
            }else {
                field.setFieldSize(metaData.getColumnDisplaySize(j + 1));
            }
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
        Connection con = null;
        try {
            String queryStr = getTablesSql(datasourceRequest);
            con = getConnection(datasourceRequest);
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
            resultSet.close();
            statement.close();

            String queryView = getViewSql(datasourceRequest);
            if(StringUtils.isNotEmpty(queryView)){
                con = getConnection(datasourceRequest);
                statement = con.createStatement();
                resultSet = statement.executeQuery(queryView);
                while (resultSet.next()) {
                    tables.add(resultSet.getString(1));
                }
                resultSet.close();
                statement.close();
            }


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

//    @Override
//    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
//        List<TableFiled> list = new LinkedList<>();
//        Connection connection = null;
//        ResultSet resultSet = null;
//        try {
//            connection = getConnectionFromPool(datasourceRequest);
//            DatabaseMetaData databaseMetaData = connection.getMetaData();
//            resultSet = databaseMetaData.getColumns(null, "%", datasourceRequest.getTable(), "%");
//            while (resultSet.next()) {
//                String tableName = resultSet.getString("TABLE_NAME");
//                String database = null;
//                if(datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.ck.name())){
//                    database = resultSet.getString("TABLE_SCHEM");
//                }else {
//                    database = resultSet.getString("TABLE_CAT");
//                }
//                if(database != null){
//                    if (tableName.equals(datasourceRequest.getTable()) && database.equalsIgnoreCase(getDatabase(datasourceRequest))) {
//                        TableFiled tableFiled = getTableFiled(resultSet, datasourceRequest);
//                        list.add(tableFiled);
//                    }
//                }else {
//                    if (tableName.equals(datasourceRequest.getTable())) {
//                        TableFiled tableFiled = getTableFiled(resultSet, datasourceRequest);
//                        list.add(tableFiled);
//                    }
//                }
//            }
//            resultSet.close();
//
//            Statement stat = connection.createStatement();
//            resultSet = stat.executeQuery(datasourceRequest.getQuery());
//            return fetchResultField(resultSet, datasourceRequest);
//
//
//        } catch (SQLException e) {
//            DataEaseException.throwException(e);
//        } catch (Exception e) {
//            DataEaseException.throwException(e);
//        } finally {
//            if(connection != null){
//                connection.close();
//            }
//        }
//        return list;
//    }

    private TableFiled getTableFiled(ResultSet resultSet, DatasourceRequest datasourceRequest) throws SQLException {
        TableFiled tableFiled = new TableFiled();
        String colName = resultSet.getString("COLUMN_NAME");
        tableFiled.setFieldName(colName);
        String remarks = resultSet.getString("REMARKS");
        if (remarks == null || remarks.equals("")) {
            remarks = colName;
        }
        tableFiled.setRemarks(remarks);
        String dbType = resultSet.getString("TYPE_NAME").toUpperCase();
        tableFiled.setFieldType(dbType);
        if(dbType.equalsIgnoreCase("LONG")){tableFiled.setFieldSize(65533);}
        if(StringUtils.isNotEmpty(dbType) && dbType.toLowerCase().contains("date") && tableFiled.getFieldSize() < 50 ){
            tableFiled.setFieldSize(50);
        }

        if(datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.ck.name())){
            QueryProvider qp = ProviderFactory.getQueryProvider(datasourceRequest.getDatasource().getType());
            tableFiled.setFieldSize(qp.transFieldSize(dbType));
        }else {
            tableFiled.setFieldSize(Integer.valueOf(resultSet.getString("COLUMN_SIZE")));
        }
        return tableFiled;
    }

    @Override
    public void checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        Connection con = null;
        try {
            con = getConnection(datasourceRequest);
            Statement statement = con.createStatement();
            String queryStr = getTablesSql(datasourceRequest);
            ResultSet resultSet = statement.executeQuery(queryStr);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            DataEaseException.throwException(e.getMessage());
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
        JdbcConfiguration jdbcConfiguration = setCredential(datasourceRequest, dataSource);
        dataSource.setMaxIdleTime(jdbcConfiguration.getMaxIdleTime()); // 最大空闲时间
        dataSource.setAcquireIncrement(jdbcConfiguration.getAcquireIncrement());// 增长数
        dataSource.setInitialPoolSize(jdbcConfiguration.getInitialPoolSize());// 初始连接数
        dataSource.setMinPoolSize(jdbcConfiguration.getMinPoolSize()); // 最小连接数
        dataSource.setMaxPoolSize(jdbcConfiguration.getMaxPoolSize()); // 最大连接数
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
            case mariadb:
            case de_doris:
            case ds_doris:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfiguration.class);
                username = mysqlConfiguration.getUsername();
                password = mysqlConfiguration.getPassword();
                driver = mysqlConfiguration.getDriver();
                jdbcurl = mysqlConfiguration.getJdbc();
                break;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfiguration.class);
                username = sqlServerConfiguration.getUsername();
                password = sqlServerConfiguration.getPassword();
                driver = sqlServerConfiguration.getDriver();
                jdbcurl = sqlServerConfiguration.getJdbc();
                break;
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfiguration.class);
                username = oracleConfiguration.getUsername();
                password = oracleConfiguration.getPassword();
                driver = oracleConfiguration.getDriver();
                jdbcurl = oracleConfiguration.getJdbc();
                props.put( "oracle.net.CONNECT_TIMEOUT" , "5000") ;
//                props.put( "oracle.jdbc.ReadTimeout" , "5000" ) ;
                break;
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), PgConfiguration.class);
                username = pgConfiguration.getUsername();
                password = pgConfiguration.getPassword();
                driver = pgConfiguration.getDriver();
                jdbcurl = pgConfiguration.getJdbc();
                break;
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), CHConfiguration.class);
                username = chConfiguration.getUsername();
                password = chConfiguration.getPassword();
                driver = chConfiguration.getDriver();
                jdbcurl = chConfiguration.getJdbc();
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


    private JdbcConfiguration setCredential(DatasourceRequest datasourceRequest, ComboPooledDataSource dataSource) throws PropertyVetoException {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        JdbcConfiguration jdbcConfiguration = new JdbcConfiguration();
        switch (datasourceType) {
            case mysql:
            case mariadb:
            case de_doris:
            case ds_doris:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfiguration.class);
                dataSource.setUser(mysqlConfiguration.getUsername());
                dataSource.setDriverClass(mysqlConfiguration.getDriver());
                dataSource.setPassword(mysqlConfiguration.getPassword());
                dataSource.setJdbcUrl(mysqlConfiguration.getJdbc());
                jdbcConfiguration = mysqlConfiguration;
                break;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfiguration.class);
                dataSource.setUser(sqlServerConfiguration.getUsername());
                dataSource.setDriverClass(sqlServerConfiguration.getDriver());
                dataSource.setPassword(sqlServerConfiguration.getPassword());
                dataSource.setJdbcUrl(sqlServerConfiguration.getJdbc());
                jdbcConfiguration = sqlServerConfiguration;
                break;
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfiguration.class);
                dataSource.setUser(oracleConfiguration.getUsername());
                dataSource.setDriverClass(oracleConfiguration.getDriver());
                dataSource.setPassword(oracleConfiguration.getPassword());
                dataSource.setJdbcUrl(oracleConfiguration.getJdbc());
                jdbcConfiguration = oracleConfiguration;
                break;
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), PgConfiguration.class);
                dataSource.setUser(pgConfiguration.getUsername());
                dataSource.setDriverClass(pgConfiguration.getDriver());
                dataSource.setPassword(pgConfiguration.getPassword());
                dataSource.setJdbcUrl(pgConfiguration.getJdbc());
                jdbcConfiguration = pgConfiguration;
                break;
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), CHConfiguration.class);
                dataSource.setUser(chConfiguration.getUsername());
                dataSource.setDriverClass(chConfiguration.getDriver());
                dataSource.setPassword(chConfiguration.getPassword());
                dataSource.setJdbcUrl(chConfiguration.getJdbc());
                jdbcConfiguration = chConfiguration;
                break;
            default:
                break;
        }
        return jdbcConfiguration;
    }

    private String getDatabase(DatasourceRequest datasourceRequest) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
            case de_doris:
            case ds_doris:
            case mariadb:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfiguration.class);
                return mysqlConfiguration.getDataBase();
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfiguration.class);
                return sqlServerConfiguration.getDataBase();
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), PgConfiguration.class);
                return pgConfiguration.getDataBase();
            default:
                JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
                return jdbcConfiguration.getDataBase();
        }
    }

    private String getTablesSql(DatasourceRequest datasourceRequest) throws Exception {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
            case mariadb:
            case de_doris:
            case ds_doris:
                return "show tables;";
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfiguration.class);
                if(StringUtils.isEmpty(sqlServerConfiguration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT TABLE_NAME FROM DATABASE.INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = 'DS_SCHEMA' ;"
                        .replace("DATABASE", sqlServerConfiguration.getDataBase())
                        .replace("DS_SCHEMA", sqlServerConfiguration.getSchema());
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfiguration.class);
                if(StringUtils.isEmpty(oracleConfiguration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "select table_name, owner from all_tables where owner='" + oracleConfiguration.getSchema() + "'";
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), PgConfiguration.class);
                if(StringUtils.isEmpty(pgConfiguration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT tablename FROM  pg_tables WHERE  schemaname='SCHEMA' ;".replace("SCHEMA", pgConfiguration.getSchema());
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), CHConfiguration.class);
                return "SELECT name FROM system.tables where database='DATABASE';".replace("DATABASE", chConfiguration.getDataBase());
            default:
                return "show tables;";
        }
    }

    private String getViewSql(DatasourceRequest datasourceRequest) throws Exception {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case mysql:
            case mariadb:
            case de_doris:
            case ds_doris:
            case ck:
                return null;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfiguration.class);
                if(StringUtils.isEmpty(sqlServerConfiguration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT TABLE_NAME FROM DATABASE.INFORMATION_SCHEMA.VIEWS WHERE  TABLE_SCHEMA = 'DS_SCHEMA' ;"
                        .replace("DATABASE", sqlServerConfiguration.getDataBase())
                        .replace("DS_SCHEMA", sqlServerConfiguration.getSchema());
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfiguration.class);
                if(StringUtils.isEmpty(oracleConfiguration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "select VIEW_NAME  from all_views where owner='" + oracleConfiguration.getSchema() + "'";
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), PgConfiguration.class);
                if(StringUtils.isEmpty(pgConfiguration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT viewname FROM  pg_views WHERE schemaname='SCHEMA' ;".replace("SCHEMA", pgConfiguration.getSchema());
            default:
                return null;
        }
    }

    private String getSchemaSql(DatasourceRequest datasourceRequest) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case oracle:
                return "select * from all_users";
            case sqlServer:
                return "select name from sys.schemas;";
            case pg:
                return "SELECT nspname FROM pg_namespace;";
            default:
                return "show tables;";
        }
    }

}
