package io.dataease.plugins.datasource.provider;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallFilter;
import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.entity.Status;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public abstract class DefaultJdbcProvider extends Provider {
    public Map<String, DruidDataSource> getJdbcConnection() {
        return jdbcConnection;
    }

    protected Map<String, DruidDataSource> jdbcConnection = new HashMap<>();
    protected ExtendedJdbcClassLoader extendedJdbcClassLoader;
    private Map<String, ExtendedJdbcClassLoader> customJdbcClassLoaders = new HashMap<>();

    static private final String FILE_PATH = "/opt/dataease/drivers";
    static private final String THIRDPART_PATH = "/opt/dataease/plugins/thirdpart";
    static private final String DEFAULT_PATH = "/opt/dataease/plugins/default";
    static private final String CUSTOM_PATH = "/opt/dataease/custom-drivers/";

    abstract public boolean isUseDatasourcePool();

    @PostConstruct
    public void init() throws Exception {
        List<String> builtinPlugins = Arrays.asList("maxcompute", "presto", "dm", "mongobi", "kylin", "kingbase");
        String jarPath = FILE_PATH;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!getType().equalsIgnoreCase("built-in")) {
            if (builtinPlugins.contains(getType())) {
                jarPath = DEFAULT_PATH + "/" + getType() + "Driver";
            } else {
                jarPath = THIRDPART_PATH + "/" + getType() + "Driver";
            }
            while (classLoader.getParent() != null) {
                classLoader = classLoader.getParent();
                if (classLoader.toString().contains("ExtClassLoader")) {
                    break;
                }
            }
        }
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

    abstract public String getType();

    @Override
    public List<String[]> getData(DatasourceRequest dsr) throws Exception {
        List<String[]> list = new LinkedList<>();
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(dsr.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(dsr); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(dsr.getQuery())) {
            list = getDataResult(rs);
            if (dsr.isPageable() && (dsr.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.sqlServer.name()) || dsr.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.db2.name()))) {
                Integer realSize = dsr.getPage() * dsr.getPageSize() < list.size() ? dsr.getPage() * dsr.getPageSize() : list.size();
                list = list.subList((dsr.getPage() - 1) * dsr.getPageSize(), realSize);
            }

        } catch (SQLException e) {
            DataEaseException.throwException("SQL ERROR" + e.getMessage());
        } catch (Exception e) {
            DataEaseException.throwException("Data source connection exception: " + e.getMessage());
        }
        return list;
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

    public PreparedStatement getPreparedStatement(Connection connection, int queryTimeout, String sql) throws Exception {
        if (connection == null) {
            throw new Exception("Failed to get connection!");
        }
        PreparedStatement stat = connection.prepareStatement(sql);
        try {
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
        }
        return stat;
    }

    public void exec(DatasourceRequest datasourceRequest) throws Exception {
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = connection.createStatement()) {
            Boolean result = stat.execute(datasourceRequest.getQuery());
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
    }

    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tables = new ArrayList<>();
        String queryStr = getTablesSql(datasourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnectionFromPool(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
            while (resultSet.next()) {
                tables.add(getTableDesc(datasourceRequest, resultSet));
            }
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        String queryView = getViewSql(datasourceRequest);
        if (queryView != null) {
            try (Connection con = getConnectionFromPool(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryView)) {
                while (resultSet.next()) {
                    tables.add(getTableDesc(datasourceRequest, resultSet));
                }
            } catch (Exception e) {
                DataEaseException.throwException(e);
            }
        }
        return tables;
    }

    @Override
    public Status checkDsStatus(DatasourceRequest datasourceRequest) throws Exception {
        Status status = new Status();
        status.setStatus(checkStatus(datasourceRequest));
        return status;
    }

    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        String queryStr = getTablesSql(datasourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
        } catch (Exception e) {
            DataEaseException.throwException(e.getMessage());
        }
        return "Success";
    }

    @Override
    public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(datasourceRequest.getQuery())) {
            return getDataResult(rs);
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<TableField> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(datasourceRequest.getQuery())) {
            return fetchResultField(rs, datasourceRequest);
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            e.printStackTrace();
            DataEaseException.throwException("Data source connection exception: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        List<String[]> dataList;
        List<TableField> fieldList;
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(datasourceRequest.getQuery())) {
            fieldList = fetchResultField(rs, datasourceRequest);
            result.put("fieldList", fieldList);
            dataList = getDataResult(rs);
            result.put("dataList", dataList);
            return result;
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return new HashMap<>();
    }

    @Override
    public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception {
        if (!isUseDatasourcePool()) {
            return;
        }
        DruidDataSource dataSource = null;
        switch (type) {
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
                    jdbcConnection.remove(datasourceRequest.getDatasource().getId());
                }
                checkStatus(datasourceRequest);
                addToPool(datasourceRequest);
                break;
            case "delete":
                dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
                if (dataSource != null) {
                    dataSource.close();
                    jdbcConnection.remove(datasourceRequest.getDatasource().getId());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        List<String> schemas = new ArrayList<>();
        String queryStr = getSchemaSql(datasourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
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
    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        System.out.println("``````````` 进入获取表字段 default");
        List<TableField> list = new LinkedList<>();
        try (Connection connection = getConnectionFromPool(datasourceRequest)) {

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", datasourceRequest.getTable(), "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String database;
                if (datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.ck.name()) || datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.impala.name())) {
                    database = resultSet.getString("TABLE_SCHEM");
                } else {
                    database = resultSet.getString("TABLE_CAT");
                }
                if (database != null) {
                    if (tableName.equals(datasourceRequest.getTable()) && database.equalsIgnoreCase(getDatabase(datasourceRequest))) {
                        TableField tableField = getTableFiled(resultSet, datasourceRequest);
                        list.add(tableField);
                    }
                } else {
                    if (tableName.equals(datasourceRequest.getTable())) {
                        TableField tableField = getTableFiled(resultSet, datasourceRequest);
                        list.add(tableField);
                    }
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            if (datasourceRequest.getDatasource().getType().equalsIgnoreCase("ds_doris")) {
                datasourceRequest.setQuery("select * from " + datasourceRequest.getTable());
                return fetchResultField(datasourceRequest);
            } else {
                DataEaseException.throwException("Data source connection exception: " + e.getMessage());
            }

        }
        return list;
    }

    @Override
    public String getTablesSql(DatasourceRequest datasourceRequest) throws Exception {
        return "show tables;";
    }

    @Override
    public String getViewSql(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    @Override
    public String getSchemaSql(DatasourceRequest datasourceRequest) {
        return null;
    }

    @Override
    public Connection getConnectionFromPool(DatasourceRequest datasourceRequest) throws Exception {
        if (!isUseDatasourcePool()) {
            return getConnection(datasourceRequest);
        }
        if (datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.mongo.name()) ||
                datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.impala.name())
                || datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.hive.name())) {
            return getConnection(datasourceRequest);
        }
        synchronized (datasourceRequest.getDatasource().getId()) {
            DruidDataSource dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
            if (dataSource == null) {
                handleDatasource(datasourceRequest, "add");
            }
            dataSource = jdbcConnection.get(datasourceRequest.getDatasource().getId());
            Connection co = dataSource.getConnection();
            return co;
        }
    }

    @Override
    public void addToPool(DatasourceRequest datasourceRequest) throws Exception {
        if (!isUseDatasourcePool()) {
            return;
        }
        DruidDataSource druidDataSource = new DruidDataSource();
        JdbcConfiguration jdbcConfiguration = setCredential(datasourceRequest, druidDataSource);
        druidDataSource.setInitialSize(jdbcConfiguration.getInitialPoolSize());// 初始连接数
        druidDataSource.setMinIdle(jdbcConfiguration.getMinPoolSize()); // 最小连接数
        druidDataSource.setMaxActive(jdbcConfiguration.getMaxPoolSize()); // 最大连接数
        if (datasourceRequest.getDatasource().getType().equals(DatasourceTypes.mongo.name()) || datasourceRequest.getDatasource().getType().equals(DatasourceTypes.hive.name()) || datasourceRequest.getDatasource().getType().equals(DatasourceTypes.impala.name())) {
            WallFilter wallFilter = new WallFilter();
            wallFilter.setDbType(DatasourceTypes.mysql.name());
            druidDataSource.setProxyFilters(Arrays.asList(new Filter[]{wallFilter}));
        }
        druidDataSource.init();
        jdbcConnection.put(datasourceRequest.getDatasource().getId(), druidDataSource);
    }

    @Override
    public JdbcConfiguration setCredential(DatasourceRequest datasourceRequest, DruidDataSource dataSource) throws Exception {
        return null;
    }

    public void reloadCustomJdbcClassLoader(DeDriver deDriver) throws Exception {
        if (customJdbcClassLoaders.get(deDriver.getId()) != null) {
            customJdbcClassLoaders.remove(deDriver.getId());
        }
        addCustomJdbcClassLoader(deDriver);
    }

    private TableField getTableFiled(ResultSet resultSet, DatasourceRequest datasourceRequest) throws SQLException {
        TableField tableField = new TableField();
        String colName = resultSet.getString("COLUMN_NAME");
        tableField.setFieldName(colName);
        String remarks = resultSet.getString("REMARKS");
        if (remarks == null || remarks.equals("")) {
            remarks = colName;
        }
        tableField.setRemarks(remarks);
        String dbType = resultSet.getString("TYPE_NAME").toUpperCase();
        tableField.setType(resultSet.getInt("DATA_TYPE"));
        tableField.setFieldType(dbType);
        if (dbType.equalsIgnoreCase("LONG")) {
            tableField.setFieldSize(65533);
        }
        if (StringUtils.isNotEmpty(dbType) && dbType.toLowerCase().contains("date") && tableField.getFieldSize() < 50) {
            tableField.setFieldSize(50);
        }

        String size = resultSet.getString("COLUMN_SIZE");
        if (size == null) {
            tableField.setFieldSize(1);
        } else {
            tableField.setFieldSize(Integer.valueOf(size));
        }
        return tableField;
    }

    private List<TableField> fetchResultField(ResultSet rs, DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int j = 0; j < columnCount; j++) {
            String f = metaData.getColumnName(j + 1);
            String l = StringUtils.isNotEmpty(metaData.getColumnLabel(j + 1)) ? metaData.getColumnLabel(j + 1) : f;
            String t = metaData.getColumnTypeName(j + 1);
            if (datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.hive.name()) && l.contains(".")) {
                l = l.split("\\.")[1];
            }
            TableField field = new TableField();
            field.setFieldName(l);
            field.setRemarks(l);
            field.setFieldType(t);
            field.setType(metaData.getColumnType(j + 1));
            field.setFieldSize(metaData.getColumnDisplaySize(j + 1));
            if (t.equalsIgnoreCase("LONG")) {
                field.setFieldSize(65533);
            } //oracle LONG
            if (StringUtils.isNotEmpty(t) && t.toLowerCase().contains("date") && field.getFieldSize() < 50) {
                field.setFieldSize(50);
            }
            fieldList.add(field);
        }
        return fieldList;
    }

    private String getDatabase(DatasourceRequest datasourceRequest) {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        return jdbcConfiguration.getDataBase();
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
                        row[j] = rs.getString(j + 1);
                        break;
                }
            }
            list.add(row);
        }
        return list;
    }

    private TableDesc getTableDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        TableDesc tableDesc = new TableDesc();
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceRequest.getDatasource().getType());
        if (datasourceType == DatasourceTypes.oracle) {
            tableDesc.setRemark(resultSet.getString(3));
        }
        if (datasourceType == DatasourceTypes.mysql) {
            tableDesc.setRemark(resultSet.getString(2));
        }
        tableDesc.setName(resultSet.getString(1));
        return tableDesc;
    }


    protected ExtendedJdbcClassLoader getCustomJdbcClassLoader(DeDriver deDriver) throws Exception {
        if (deDriver == null) {
            throw new Exception("Can not found custom Driver");
        }
        ExtendedJdbcClassLoader customJdbcClassLoader = customJdbcClassLoaders.get(deDriver.getId());
        if (customJdbcClassLoader == null) {
            return addCustomJdbcClassLoader(deDriver);
        } else {
            if (StringUtils.isNotEmpty(customJdbcClassLoader.getDriver()) && customJdbcClassLoader.getDriver().equalsIgnoreCase(deDriver.getDriverClass())) {
                return customJdbcClassLoader;
            } else {
                customJdbcClassLoaders.remove(deDriver.getId());
                return addCustomJdbcClassLoader(deDriver);
            }
        }
    }

    private synchronized ExtendedJdbcClassLoader addCustomJdbcClassLoader(DeDriver deDriver) throws Exception {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();
            if (classLoader.toString().contains("ExtClassLoader")) {
                break;
            }
        }
        ExtendedJdbcClassLoader customJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(CUSTOM_PATH + deDriver.getId()).toURI().toURL()}, classLoader);
        customJdbcClassLoader.setDriver(deDriver.getDriverClass());

        File file = new File(CUSTOM_PATH + deDriver.getId());
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

        customJdbcClassLoaders.put(deDriver.getId(), customJdbcClassLoader);
        return customJdbcClassLoader;
    }

    protected boolean isDefaultClassLoader(String customDriver) {
        return StringUtils.isEmpty(customDriver) || customDriver.contains("default");
    }

    @Override
    public void checkConfiguration(Datasource datasource) throws Exception {
        if (StringUtils.isEmpty(datasource.getConfiguration())) {
            throw new Exception("Datasource configuration is empty");
        }
        try {
            JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasource.getConfiguration(), JdbcConfiguration.class);
            if (jdbcConfiguration.getQueryTimeout() < 0) {
                throw new Exception("Querytimeout cannot be less than zero.");
            }
        } catch (Exception e) {
            throw new Exception("Invalid configuration: " + e.getMessage());
        }
    }


    public String dsVersion(DatasourceRequest datasourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        try (Connection con = getConnectionFromPool(datasourceRequest)) {
            return String.valueOf(con.getMetaData().getDatabaseMajorVersion());
        } catch (Exception e) {
            DataEaseException.throwException(e.getMessage());
        }
        return "";
    }

}
