package io.dataease.datasource.provider;

import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.datasource.dao.auto.entity.CoreDriver;
import io.dataease.datasource.dao.auto.mapper.CoreDriverMapper;
import io.dataease.datasource.model.TableField;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.JsonUtil;
import org.apache.calcite.jdbc.CalciteConnection;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;



@Component("calciteProvider")

public class CalciteProvider extends Provider{

    @Resource
    private CoreDriverMapper coreDriverMapper;

    protected ExtendedJdbcClassLoader extendedJdbcClassLoader;
    private Map<String, ExtendedJdbcClassLoader> customJdbcClassLoaders = new HashMap<>();

    private final String FILE_PATH = "/opt/dataease/drivers";
    private final String CUSTOM_PATH = "/opt/dataease/custom-drivers/";

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

    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        String querySql = getTablesSql(datasourceRequest).get(0);
        DatasourceConfiguration datasourceConfiguration = JsonUtil.parse(datasourceRequest.getDatasource().getConfiguration(), DatasourceConfiguration.class);
        int queryTimeout = datasourceConfiguration.getQueryTimeout() > 0 ? datasourceConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(querySql)) {
        } catch (Exception e) {
            throw e;
        }
        return "Success";

    }

    @Override
    public List<TableField> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> datasetTableFields = new ArrayList<>();
        DatasourceConfiguration datasourceConfiguration = (DatasourceConfiguration) CommonBeanFactory.getBean(datasourceRequest.getDatasource().getType());
        int queryTimeout = datasourceConfiguration.getQueryTimeout() > 0 ? datasourceConfiguration.getQueryTimeout() : 0;

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(datasourceConfiguration.getJdbc());
        dataSource.setUsername(datasourceConfiguration.getUsername());
        dataSource.setPassword(datasourceConfiguration.getPassword());
        Connection connection = null;
        CalciteConnection calciteConnection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getCalciteConnection(datasourceConfiguration);
            calciteConnection = connection.unwrap(CalciteConnection.class);
            buildSchema(datasourceRequest, calciteConnection, dataSource);
            statement = getStatement(calciteConnection, queryTimeout);
            resultSet = statement.executeQuery(buildTableFiledSql(datasourceRequest));
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                TableField tableField = new TableField();
                tableField.setFieldName(metaData.getColumnName(i));
                datasetTableFields.add(tableField);
            }
        } catch (Exception e) {

        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (calciteConnection != null) calciteConnection.close();
            if (connection != null) connection.close();
        }
        return datasetTableFields;
    }

    public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();

        DatasourceConfiguration datasourceConfiguration = (DatasourceConfiguration) CommonBeanFactory.getBean(datasourceRequest.getDatasource().getType());
        int queryTimeout = datasourceConfiguration.getQueryTimeout() > 0 ? datasourceConfiguration.getQueryTimeout() : 0;
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(datasourceConfiguration.getJdbc());
        dataSource.setUsername(datasourceConfiguration.getUsername());
        dataSource.setPassword(datasourceConfiguration.getPassword());
        Connection connection = null;
        CalciteConnection calciteConnection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getCalciteConnection(datasourceConfiguration);
            calciteConnection = connection.unwrap(CalciteConnection.class);
            buildSchema(datasourceRequest, calciteConnection, dataSource);
            statement = getStatement(calciteConnection, queryTimeout);
            resultSet = statement.executeQuery(buildTableFiledSql(datasourceRequest));
            list = getDataResult(resultSet);
        } catch (Exception e) {

        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (calciteConnection != null) calciteConnection.close();
            if (connection != null) connection.close();
        }
        return list;
    }

    @Override
    public List<Map<String, String>> getTables(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    private SchemaPlus buildSchema(DatasourceRequest datasourceRequest, CalciteConnection calciteConnection, BasicDataSource dataSource) {
        DatasourceConfiguration datasourceConfiguration = (DatasourceConfiguration) CommonBeanFactory.getBean(datasourceRequest.getDatasource().getType());
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Schema schema = null;
        switch (datasourceRequest.getDatasource().getType()) {
            case "mysql":
                schema = JdbcSchema.create(rootSchema, datasourceConfiguration.getDataBase(), dataSource, null, datasourceConfiguration.getDataBase());
                rootSchema.add(datasourceConfiguration.getDataBase(), schema);
                return rootSchema;
            default:
                schema = JdbcSchema.create(rootSchema, datasourceConfiguration.getDataBase(), dataSource, null, datasourceConfiguration.getDataBase());
                rootSchema.add(datasourceConfiguration.getDataBase(), schema);
                return rootSchema;
        }

    }

    private Connection getCalciteConnection(DatasourceConfiguration datasourceConfiguration) throws Exception {
        Driver driver = (Driver) extendedJdbcClassLoader.loadClass(datasourceConfiguration.getDriver()).newInstance();
        Thread.currentThread().setContextClassLoader(extendedJdbcClassLoader);
        Class.forName("org.apache.calcite.jdbc.Driver");
        DriverManager.registerDriver(new DriverShim(driver));
        Properties info = new Properties();
        info.setProperty("lex", "SQL_SERVER"); //TODO
        info.setProperty("conformance", "ORACLE_10"); //TODO
        info.setProperty("caseSensitive", "false");
        info.setProperty("remarks", "true");
        info.setProperty("parserFactory", "org.apache.calcite.sql.parser.impl.SqlParserImpl#FACTORY");
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        return connection;
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

    private String buildTableFiledSql(DatasourceRequest datasourceRequest) {
        switch (datasourceRequest.getDatasource().getType()) {
            case "mysql":
                return "SELECT * FROM " + datasourceRequest.getTable() + " WHERE 1>2";
            default:
                return "SELECT * FROM " + datasourceRequest.getTable() + " WHERE 1>2";
        }
    }

    private List<String> getTablesSql(DatasourceRequest datasourceRequest) {
        DatasourceConfiguration datasourceConfiguration = (DatasourceConfiguration) CommonBeanFactory.getBean(datasourceRequest.getDatasource().getType());
        return datasourceConfiguration.getShowTableSqls();
    }

    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        DatasourceConfiguration datasourceConfiguration = (DatasourceConfiguration) CommonBeanFactory.getBean(datasourceRequest.getDatasource().getType());
        Properties props = new Properties();
        if (StringUtils.isNotBlank(datasourceConfiguration.getUsername())) {
            props.setProperty("user", datasourceConfiguration.getUsername());
            if (StringUtils.isNotBlank(datasourceConfiguration.getPassword())) {
                props.setProperty("password", datasourceConfiguration.getPassword());
            }
        }

        Connection conn;
        CoreDriver coreDriver = null;
        String driverClassName;
        ExtendedJdbcClassLoader jdbcClassLoader;
        if (isDefaultClassLoader(datasourceConfiguration.getCustomDriver())) {
            driverClassName = datasourceConfiguration.getDriver();
            jdbcClassLoader = extendedJdbcClassLoader;
        } else {
            if (coreDriver == null) {
                coreDriver = coreDriverMapper.selectById(datasourceConfiguration.getCustomDriver());
            }
            driverClassName = coreDriver.getDriverClass();
            jdbcClassLoader = getCustomJdbcClassLoader(coreDriver);
        }

        Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(jdbcClassLoader);
            conn = driverClass.connect(datasourceConfiguration.getJdbc(), props);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
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
