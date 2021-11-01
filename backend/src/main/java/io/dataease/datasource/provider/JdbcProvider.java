package io.dataease.datasource.provider;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallFilter;
import com.google.gson.Gson;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.*;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.provider.QueryProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("jdbc")
public class JdbcProvider extends DatasourceProvider {
    private static Map<String, DruidDataSource> jdbcConnection = new HashMap<>();
    public ExtendedJdbcClassLoader extendedJdbcClassLoader;
    static private String FILE_PATH = "/opt/dataease/drivers";
    private static final String REG_WITH_SQL_FRAGMENT = "((?i)WITH[\\s\\S]+(?i)AS?\\s*\\([\\s\\S]+\\))\\s*(?i)SELECT";
    public static final Pattern WITH_SQL_FRAGMENT = Pattern.compile(REG_WITH_SQL_FRAGMENT);

    @PostConstruct
    public void init() throws Exception{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        extendedJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(FILE_PATH).toURI().toURL()}, classLoader);
        File file = new File(FILE_PATH);
        File[] array = file.listFiles();
        Optional.ofNullable(array).ifPresent(files -> {
            for (File tmp : array) {
                if (tmp.getName().endsWith(".jar")) {
                    try {
                        extendedJdbcClassLoader.addFile(tmp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

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
        try (Connection connection = getConnectionFromPool(dsr); Statement stat = connection.createStatement(); ResultSet rs = stat.executeQuery(rebuildSqlWithFragment(dsr.getQuery()) )){

            list = fetchResult(rs);

            if(dsr.isPageable() && dsr.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.sqlServer.name())){
                Integer realSize = dsr.getPage() * dsr.getPageSize() < list.size() ? dsr.getPage() * dsr.getPageSize(): list.size();
                list = list.subList((dsr.getPage() - 1) * dsr.getPageSize(), realSize);
            }

        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return list;
    }

    public void exec(DatasourceRequest datasourceRequest) throws Exception {
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = connection.createStatement()){
            Boolean result = stat.execute(datasourceRequest.getQuery());
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
    }

    @Override
    public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception {
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = connection.createStatement(); ResultSet rs = stat.executeQuery(rebuildSqlWithFragment(datasourceRequest.getQuery()))){
            return fetchResult(rs);
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
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
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = connection.createStatement(); ResultSet rs = stat.executeQuery(rebuildSqlWithFragment(datasourceRequest.getQuery()))){
            return fetchResultField(rs, datasourceRequest);
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(Translator.get("i18n_datasource_connect_error") + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        List<String[]> dataList = new LinkedList<>();
        List<TableFiled> fieldList = new ArrayList<>();
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = connection.createStatement(); ResultSet rs = stat.executeQuery(rebuildSqlWithFragment(datasourceRequest.getQuery()))){
            dataList = fetchResult(rs);
            fieldList = fetchResultField(rs, datasourceRequest);
            result.put("dataList", dataList);
            result.put("fieldList", fieldList);
            return result;
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
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
        String queryStr = getTablesSql(datasourceRequest);
        try (Connection con = getConnectionFromPool(datasourceRequest); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(queryStr)){
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }

        String queryView = getViewSql(datasourceRequest);
        if(queryView != null){
            try (Connection con = getConnectionFromPool(datasourceRequest); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(queryView)){
                while (resultSet.next()) {
                    tables.add(resultSet.getString(1));
                }
            } catch (Exception e) {
                DataEaseException.throwException(e);
            }
        }

        return tables;
    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        List<String> schemas = new ArrayList<>();
        String queryStr = getSchemaSql(datasourceRequest);
        try (Connection con = getConnection(datasourceRequest); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(queryStr)){
            while (resultSet.next()) {
                schemas.add(resultSet.getString(1));
            }
            return schemas;
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return new ArrayList<>();
    }

    @Override
    public void checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        String queryStr = getTablesSql(datasourceRequest);
        try (Connection con = getConnection(datasourceRequest); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(queryStr)){

        } catch (Exception e) {
            e.printStackTrace();
            DataEaseException.throwException(e.getMessage());
        }
    }

    @Override
    public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception {
        DruidDataSource dataSource = null;
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

    private Connection getConnectionFromPool(DatasourceRequest datasourceRequest) throws Exception {
        DruidDataSource dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
        if (dataSource == null) {
            handleDatasource(datasourceRequest, "add");
        }
        dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
        Connection co = dataSource.getConnection();
        return co;
    }

    private Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
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
                driver = "com.mysql.jdbc.Driver";
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
            case mongo:
                MongodbConfiguration mongodbConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MongodbConfiguration.class);
                username = mongodbConfiguration.getUsername();
                password = mongodbConfiguration.getPassword();
                driver = mongodbConfiguration.getDriver();
                jdbcurl = mongodbConfiguration.getJdbc();
                break;
            default:
                break;
        }

        Driver driverClass = (Driver) extendedJdbcClassLoader.loadClass(driver).newInstance();
        props.setProperty("user", username);
        if (StringUtils.isNotBlank(password)) {
            props.setProperty("password", password);
        }

        Connection conn = driverClass.connect(jdbcurl, props);
        return conn;
    }

    private void addToPool(DatasourceRequest datasourceRequest) throws PropertyVetoException, SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        JdbcConfiguration jdbcConfiguration = setCredential(datasourceRequest, druidDataSource);
        druidDataSource.setInitialSize(jdbcConfiguration.getInitialPoolSize());// 初始连接数
        druidDataSource.setMinIdle(jdbcConfiguration.getMinPoolSize()); // 最小连接数
        druidDataSource.setMaxActive(jdbcConfiguration.getMaxPoolSize()); // 最大连接数
        if(datasourceRequest.getDatasource().getType().equals(DatasourceTypes.mongo.name())){
            WallFilter wallFilter = new WallFilter();
            wallFilter.setDbType(DatasourceTypes.mysql.name());
            druidDataSource.setProxyFilters(Arrays.asList(new Filter[]{wallFilter}));
        }
        druidDataSource.init();
        jdbcConnection.put(datasourceRequest.getDatasource().getId(), druidDataSource);
    }


    private JdbcConfiguration setCredential(DatasourceRequest datasourceRequest, DruidDataSource dataSource) throws PropertyVetoException {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        JdbcConfiguration jdbcConfiguration = new JdbcConfiguration();
        switch (datasourceType) {
            case mysql:
            case mariadb:
            case de_doris:
            case ds_doris:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MysqlConfiguration.class);
                dataSource.setUrl(mysqlConfiguration.getJdbc());
                dataSource.setDriverClassName(mysqlConfiguration.getDriver());
                dataSource.setValidationQuery("select 1");
                jdbcConfiguration = mysqlConfiguration;
                break;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), SqlServerConfiguration.class);
                dataSource.setDriverClassName(sqlServerConfiguration.getDriver());
                dataSource.setUrl(sqlServerConfiguration.getJdbc());
                dataSource.setValidationQuery("select 1");
                jdbcConfiguration = sqlServerConfiguration;
                break;
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), OracleConfiguration.class);
                dataSource.setDriverClassName(oracleConfiguration.getDriver());
                dataSource.setUrl(oracleConfiguration.getJdbc());
                dataSource.setValidationQuery("select 1 from dual");
                jdbcConfiguration = oracleConfiguration;
                break;
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), PgConfiguration.class);
                dataSource.setDriverClassName(pgConfiguration.getDriver());
                dataSource.setUrl(pgConfiguration.getJdbc());
                jdbcConfiguration = pgConfiguration;
                break;
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), CHConfiguration.class);
                dataSource.setDriverClassName(chConfiguration.getDriver());
                dataSource.setUrl(chConfiguration.getJdbc());
                jdbcConfiguration = chConfiguration;
                break;
            case mongo:
                MongodbConfiguration mongodbConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MongodbConfiguration.class);
                dataSource.setDriverClassName(mongodbConfiguration.getDriver());
                dataSource.setUrl(mongodbConfiguration.getJdbc());
                jdbcConfiguration = mongodbConfiguration;
                break;
            default:
                break;
        }

        dataSource.setUsername(jdbcConfiguration.getUsername());
        dataSource.setDriverClassLoader(extendedJdbcClassLoader);
        dataSource.setPassword(jdbcConfiguration.getPassword());

        return jdbcConfiguration;
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
            case redshift:
                RedshiftConfigration redshiftConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), RedshiftConfigration.class);
                if(StringUtils.isEmpty(redshiftConfigration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT tablename FROM  pg_tables WHERE  schemaname='SCHEMA' ;".replace("SCHEMA", redshiftConfigration.getSchema());
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
            case redshift:
                RedshiftConfigration redshiftConfigration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), RedshiftConfigration.class);
                if(StringUtils.isEmpty(redshiftConfigration.getSchema())){
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT viewname FROM  pg_views WHERE schemaname='SCHEMA' ;".replace("SCHEMA", redshiftConfigration.getSchema());
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

    private static String rebuildSqlWithFragment(String sql) {
        if (!sql.toLowerCase().startsWith("with")) {
            Matcher matcher = WITH_SQL_FRAGMENT.matcher(sql);
            if (matcher.find()) {
                String withFragment = matcher.group();
                if (!com.alibaba.druid.util.StringUtils.isEmpty(withFragment)) {
                    if (withFragment.length() > 6) {
                        int lastSelectIndex = withFragment.length() - 6;
                        sql = sql.replace(withFragment, withFragment.substring(lastSelectIndex));
                        withFragment = withFragment.substring(0, lastSelectIndex);
                    }
                    sql = withFragment + " " + sql;
                    sql = sql.replaceAll(" " + "{2,}", " ");
                }
            }
        }
        return sql;
    }

}
