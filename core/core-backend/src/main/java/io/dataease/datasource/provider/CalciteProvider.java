package io.dataease.datasource.provider;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.api.ds.vo.DatasourceConfiguration.DatasourceType;
import io.dataease.api.ds.vo.TableField;
import io.dataease.commons.exception.DataEaseException;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDriver;
import io.dataease.datasource.dao.auto.mapper.CoreDriverMapper;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.type.*;
import io.dataease.engine.func.scalar.ScalarFunctions;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.*;
import java.util.*;


@Component("calciteProvider")
public class CalciteProvider {

    //TODO mongo impala es hive
    @Resource
    protected CoreDriverMapper coreDriverMapper;

    protected ExtendedJdbcClassLoader extendedJdbcClassLoader;
    private Map<Long, ExtendedJdbcClassLoader> customJdbcClassLoaders = new HashMap<>();
    private final String FILE_PATH = "/opt/dataease/drivers";
    private final String CUSTOM_PATH = "/opt/dataease/custom-drivers/";
    private static Map<String, Connection> connectionMap = new HashMap<>();
    private static String split = "DE";

    @PostConstruct
    public void init() throws Exception {
        String jarPath = FILE_PATH;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        extendedJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(jarPath).toURI().toURL()}, classLoader);
        File file = new File(jarPath);
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


    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        List<String> schemas = new ArrayList<>();
        String queryStr = getSchemaSql(datasourceRequest.getDatasource());
        try (Connection con = getConnection(datasourceRequest.getDatasource()); Statement statement = getStatement(con, 30); ResultSet resultSet = statement.executeQuery(queryStr)) {
            while (resultSet.next()) {
                schemas.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return schemas;
    }

    public List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) throws DEException {
        List<DatasetTableDTO> tables = new ArrayList<>();
        List<String> tablesSqls = getTablesSql(datasourceRequest);
        for (String tablesSql : tablesSqls) {
            try (Connection con = getConnection(datasourceRequest.getDatasource()); Statement statement = getStatement(con, 30); ResultSet resultSet = statement.executeQuery(tablesSql)) {
                while (resultSet.next()) {
                    tables.add(getTableDesc(datasourceRequest, resultSet));
                }
            } catch (Exception e) {
                DataEaseException.throwException(e);
            }
        }
        return tables;
    }

    private DatasetTableDTO getTableDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        DatasetTableDTO tableDesc = new DatasetTableDTO();
        tableDesc.setDatasourceId(datasourceRequest.getDatasource().getId());
        tableDesc.setType("db");
        tableDesc.setTableName(resultSet.getString(1));
        tableDesc.setName(resultSet.getString(1));
        return tableDesc;
    }

    private String getDriver(DatasourceSchemaDTO ds) {
        DatasourceType datasourceType = DatasourceType.valueOf(ds.getType());
        switch (datasourceType) {
            case mysql:
            case TiDB:
            case StarRocks:
            case mariadb:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("mysql")).getDriver();
            case sqlServer:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("sqlServer")).getDriver();
            case oracle:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("oracle")).getDriver();
            case db2:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("db2")).getDriver();
            case ck:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("ck")).getDriver();
            case pg:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("pg")).getDriver();
            case redshift:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("redshift")).getDriver();
            case h2:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("h2")).getDriver();
            default:
                return ((DatasourceConfiguration) CommonBeanFactory.getBean("mysql")).getDriver();
        }
    }

    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        String querySql = getTablesSql(datasourceRequest).get(0);
        try (Connection con = getConnection(datasourceRequest.getDatasource()); Statement statement = getStatement(con, 30); ResultSet resultSet = statement.executeQuery(querySql)) {
        } catch (Exception e) {
            throw e;
        }
        return "Success";
    }

    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    public Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException {
        List<TableField> datasetTableFields = new ArrayList<>();
        List<String[]> list = new LinkedList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        CalciteConnection connection = null;
        try {
            connection = getConnection(datasourceRequest);
            statement = connection.prepareStatement(datasourceRequest.getQuery());
            resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                TableField tableField = new TableField();
                tableField.setOriginName(metaData.getColumnLabel(i));// 用ColumnName取的是原字段取不到as的别名，所以使用ColumnLabel
                tableField.setType(metaData.getColumnTypeName(i));
                tableField.setPrecision(metaData.getPrecision(i));
                tableField.setScale(metaData.getScale(i));
                datasetTableFields.add(tableField);
            }
            list = getDataResult(resultSet);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fields", datasetTableFields);
        map.put("data", list);
        return map;
    }

    private CalciteConnection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        Connection connection = getCalciteConnection(datasourceRequest);
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = buildSchema(datasourceRequest, calciteConnection);
        addCustomFunctions(rootSchema);
        return calciteConnection;
    }

    private void addCustomFunctions(SchemaPlus rootSchema) {
        // scalar functions
        Class<?> clazz = ScalarFunctions.class;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            rootSchema.add(method.getName().toUpperCase(), ScalarFunctionImpl.create(
                    ScalarFunctions.class, method.getName()));
        }
    }

    private void registerDriver(DatasourceRequest datasourceRequest) throws Exception {
        for (Map.Entry<Long, DatasourceSchemaDTO> next : datasourceRequest.getDsList().entrySet()) {
            DatasourceSchemaDTO ds = next.getValue();
            Driver driver = (Driver) extendedJdbcClassLoader.loadClass(getDriver(ds)).newInstance();
            DriverManager.registerDriver(new DriverShim(driver));
        }
    }

    private Connection getCalciteConnection(DatasourceRequest datasourceRequest) throws Exception {
        registerDriver(datasourceRequest);
        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        info.setProperty("caseSensitive", "false");
        info.setProperty("remarks", "true");
        info.setProperty("parserFactory", "org.apache.calcite.sql.parser.impl.SqlParserImpl#FACTORY");
        Class.forName("org.apache.calcite.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        return connection;
    }

    // 构建root schema
    private SchemaPlus buildSchema(DatasourceRequest datasourceRequest, CalciteConnection calciteConnection) throws Exception {
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Map<Long, DatasourceSchemaDTO> dsList = datasourceRequest.getDsList();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsList.entrySet()) {
            DatasourceSchemaDTO ds = next.getValue();
            // build schema
            BasicDataSource dataSource = new BasicDataSource();
            Schema schema = null;
            DatasourceConfiguration configuration = null;
            DatasourceType datasourceType = DatasourceType.valueOf(ds.getType());
            switch (datasourceType) {
                case mysql:
                case mariadb:
                case TiDB:
                case StarRocks:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), Mysql.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                case sqlServer:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), Sqlserver.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                case oracle:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), Oracle.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                case db2:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), Db2.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                case ck:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), CK.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                case pg:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), Pg.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                case redshift:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), Redshift.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                case h2:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), H2.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                default:
                    configuration = JsonUtil.parseObject(ds.getConfiguration(), Mysql.class);
                    dataSource.setUrl(configuration.getJdbc());
                    dataSource.setUsername(configuration.getUsername());
                    dataSource.setPassword(configuration.getPassword());
                    dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                    rootSchema.add(ds.getSchemaAlias(), schema);
            }
        }
        return rootSchema;
    }

    private List<String[]> getDataResult(ResultSet rs) throws Exception {
        List<String[]> list = new LinkedList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int j = 0; j < columnCount; j++) {
                int columnType = metaData.getColumnType(j + 1);
                switch (columnType) {
                    case Types.DATE:
                        if (rs.getDate(j + 1) != null) {
                            row[j] = rs.getDate(j + 1).toString();
                        }
                        break;
                    case Types.BOOLEAN:
                        row[j] = rs.getBoolean(j + 1) ? "1" : "0";
                        break;
                    default:
                        if (metaData.getColumnTypeName(j + 1).toLowerCase().equalsIgnoreCase("blob")) {
                            row[j] = rs.getBlob(j + 1) == null ? "" : rs.getBlob(j + 1).toString();
                        } else {
                            row[j] = rs.getString(j + 1);
                        }
                        break;
                }
            }
            list.add(row);
        }
        return list;
    }

    private List<String> getTablesSql(DatasourceRequest datasourceRequest) throws DEException {
        List<String> tableSqls = new ArrayList<>();
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        DatasourceConfiguration configuration = null;
        switch (datasourceType) {
            case mysql:
            case mariadb:
            case TiDB:
            case StarRocks:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                tableSqls.add(String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' ;", configuration.getDataBase()));
                break;
            case oracle:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Oracle.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("select table_name, owner, comments from all_tab_comments where owner='" + configuration.getSchema() + "' AND table_type = 'TABLE'");
                break;
            case db2:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Db2.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("select TABNAME from syscat.tables  WHERE TABSCHEMA ='DE_SCHEMA' AND \"TYPE\" = 'T'".replace("DE_SCHEMA", configuration.getSchema()));
                break;
            case sqlServer:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Sqlserver.class);
                tableSqls.add("SELECT TABLE_NAME FROM \"DATABASE\".INFORMATION_SCHEMA.VIEWS WHERE  TABLE_SCHEMA = 'DS_SCHEMA' ;"
                        .replace("DATABASE", configuration.getDataBase())
                        .replace("DS_SCHEMA", configuration.getSchema()));
                tableSqls.add("SELECT TABLE_NAME FROM \"DATABASE\".INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = 'DS_SCHEMA' ;"
                        .replace("DATABASE", configuration.getDataBase())
                        .replace("DS_SCHEMA", configuration.getSchema()));
                break;
            case pg:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Pg.class);
                tableSqls.add("SELECT tablename FROM  pg_tables WHERE  schemaname='SCHEMA' ;".replace("SCHEMA", configuration.getSchema()));
                break;
            case redshift:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                tableSqls.add("SELECT tablename FROM  pg_tables WHERE  schemaname='SCHEMA' ;".replace("SCHEMA", configuration.getSchema()));
                break;
            case ck:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                tableSqls.add("SELECT name FROM system.tables where database='DATABASE';".replace("DATABASE", configuration.getDataBase()));
                break;
            default:
                tableSqls.add("show tables");
        }
        return tableSqls;

    }

    private String getSchemaSql(CoreDatasource datasource) throws DEException {
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasource.getType());
        switch (datasourceType) {
            case oracle:
                return "select * from all_users";
            case sqlServer:
                return "select name from sys.schemas;";
            case db2:
                DatasourceConfiguration configuration = JsonUtil.parseObject(datasource.getConfiguration(), Db2.class);
                return "select SCHEMANAME from syscat.SCHEMATA   WHERE \"DEFINER\" ='USER'".replace("USER", configuration.getUsername().toUpperCase());
            case pg:
                return "SELECT nspname FROM pg_namespace;";
            case redshift:
                return "SELECT nspname FROM pg_namespace;";
            default:
                return "show tables;";
        }
    }


    public Connection getConnection(CoreDatasource coreDatasource) throws Exception {
        DatasourceConfiguration configuration = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(coreDatasource.getType());
        switch (datasourceType) {
            case mysql:
            case StarRocks:
            case TiDB:
            case mariadb:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Mysql.class);
                break;
            case sqlServer:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Sqlserver.class);
                break;
            case oracle:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Oracle.class);
                break;
            case db2:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Db2.class);
                break;
            case pg:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Pg.class);
                break;
            case redshift:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Redshift.class);
                break;
            case ck:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), CK.class);
                break;
            case h2:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), H2.class);
                break;
            default:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Mysql.class);
        }
        Properties props = new Properties();
        if (StringUtils.isNotBlank(configuration.getUsername())) {
            props.setProperty("user", configuration.getUsername());
            if (StringUtils.isNotBlank(configuration.getPassword())) {
                props.setProperty("password", configuration.getPassword());
            }
        }
        String driverClassName = configuration.getDriver();
        ExtendedJdbcClassLoader jdbcClassLoader = extendedJdbcClassLoader;
        Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
        Connection conn;
        try {
            conn = driverClass.connect(configuration.getJdbc(), props);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return conn;
    }

    public Statement getStatement(Connection connection, int queryTimeout) throws Exception {
        if (connection == null) {
            throw new Exception("Failed to get connection!");
        }
        Statement stat = connection.createStatement();
        try {
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
        }
        return stat;
    }

    protected boolean isDefaultClassLoader(String customDriver) {
        return StringUtils.isEmpty(customDriver) || customDriver.equalsIgnoreCase("default");
    }

    protected ExtendedJdbcClassLoader getCustomJdbcClassLoader(CoreDriver coreDriver) throws Exception {
        if (coreDriver == null) {
            throw new Exception("Can not found custom Driver");
        }
        ExtendedJdbcClassLoader customJdbcClassLoader = customJdbcClassLoaders.get(coreDriver.getId());
        if (customJdbcClassLoader == null) {
            return addCustomJdbcClassLoader(coreDriver);
        } else {
            if (StringUtils.isNotEmpty(customJdbcClassLoader.getDriver()) && customJdbcClassLoader.getDriver().equalsIgnoreCase(coreDriver.getDriverClass())) {
                return customJdbcClassLoader;
            } else {
                customJdbcClassLoaders.remove(coreDriver.getId());
                return addCustomJdbcClassLoader(coreDriver);
            }
        }
    }

    private synchronized ExtendedJdbcClassLoader addCustomJdbcClassLoader(CoreDriver coreDriver) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();
            if (classLoader.toString().contains("ExtClassLoader")) {
                break;
            }
        }
        ExtendedJdbcClassLoader customJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(CUSTOM_PATH + coreDriver.getId()).toURI().toURL()}, classLoader);
        customJdbcClassLoader.setDriver(coreDriver.getDriverClass());
        File file = new File(CUSTOM_PATH + coreDriver.getId());
        File[] array = file.listFiles();
        Optional.ofNullable(array).ifPresent(files -> {
            for (File tmp : array) {
                if (tmp.getName().endsWith(".jar")) {
                    try {
                        customJdbcClassLoader.addFile(tmp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        customJdbcClassLoaders.put(coreDriver.getId(), customJdbcClassLoader);
        return customJdbcClassLoader;
    }
}
