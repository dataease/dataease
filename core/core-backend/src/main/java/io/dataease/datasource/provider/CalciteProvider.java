package io.dataease.datasource.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.commons.exception.DataEaseException;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.datasource.dao.auto.entity.CoreDriver;
import io.dataease.datasource.dao.auto.mapper.CoreDriverMapper;
import io.dataease.datasource.model.TableField;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DEException;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
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

public class CalciteProvider extends Provider {

    @Resource
    protected CoreDriverMapper coreDriverMapper;

    protected ExtendedJdbcClassLoader extendedJdbcClassLoader;
    private Map<Long, ExtendedJdbcClassLoader> customJdbcClassLoaders = new HashMap<>();
    protected ObjectMapper objectMapper = new ObjectMapper();
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

    @Override
    public List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<DatasetTableDTO> tables = new ArrayList<>();
        List<String> tablesSqls = getTablesSql(datasourceRequest);
        for (String tablesSql : tablesSqls) {
            try (Connection con = getConnection(datasourceRequest.getDatasource().getConfiguration()); Statement statement = getStatement(con, 30); ResultSet resultSet = statement.executeQuery(tablesSql)) {
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
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        if (datasourceType == DatasourceConfiguration.DatasourceType.oracle) {
            tableDesc.setName(resultSet.getString(3));
        }
        if (datasourceType == DatasourceConfiguration.DatasourceType.mysql) {
            tableDesc.setName(resultSet.getString(2));
        }
        tableDesc.setTableName(resultSet.getString(1));
        return tableDesc;
    }

    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        String querySql = getTablesSql(datasourceRequest).get(0);
        try (Connection con = getConnection(datasourceRequest.getDatasource().getConfiguration()); Statement statement = getStatement(con, 30); ResultSet resultSet = statement.executeQuery(querySql)) {
        } catch (Exception e) {
            throw e;
        }
        return "Success";

    }

    @Override
    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> datasetTableFields = new ArrayList<>();
        List<String[]> list = new LinkedList<>();
        Connection connection = null;
        CalciteConnection calciteConnection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            connection = getCalciteConnection(datasourceRequest);
            calciteConnection = connection.unwrap(CalciteConnection.class);
            Thread.currentThread().setContextClassLoader(extendedJdbcClassLoader);
            buildSchema(datasourceRequest, calciteConnection);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(datasourceRequest.getQuery());
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                TableField tableField = new TableField();
                tableField.setFieldName(metaData.getColumnName(i));
                tableField.setType(metaData.getColumnTypeName(i));
                tableField.setPrecision(metaData.getPrecision(i));
                tableField.setScale(metaData.getScale(i));
                datasetTableFields.add(tableField);
            }
            list = getDataResult(resultSet);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (calciteConnection != null) calciteConnection.close();
            if (connection != null) connection.close();
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fields", datasetTableFields);
        map.put("data", list);
        return map;
    }


    // 构建root schema
    private SchemaPlus buildSchema(DatasourceRequest datasourceRequest, CalciteConnection calciteConnection) throws Exception {
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Map<Long, DatasourceSchemaDTO> dsList = datasourceRequest.getDsList();

        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsList.entrySet()) {
            DatasourceSchemaDTO ds = next.getValue();
            JsonNode rootNode = objectMapper.readTree(ds.getConfiguration());

            // build schema
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setUrl(rootNode.get("jdbc").asText());
            dataSource.setUsername(rootNode.get("username").asText());
            dataSource.setPassword(rootNode.get("password").asText());
            dataSource.setDefaultQueryTimeout(Integer.valueOf(rootNode.get("queryTimeout").asText()));
            Schema schema = null;

            switch (datasourceRequest.getDatasource().getType()) {
                case "mysql":
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, rootNode.get("dataBase").asText());
                    rootSchema.add(ds.getSchemaAlias(), schema);
                    break;
                default:
                    schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, rootNode.get("dataBase").asText());
                    rootSchema.add(ds.getSchemaAlias(), schema);
            }


        }
        return rootSchema;
    }

    private void registerDriver(DatasourceRequest datasourceRequest) throws Exception {
        for (Map.Entry<Long, DatasourceSchemaDTO> next : datasourceRequest.getDsList().entrySet()) {
            DatasourceSchemaDTO ds = next.getValue();
            JsonNode rootNode = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration());
            Driver driver = (Driver) extendedJdbcClassLoader.loadClass(rootNode.get("driver").asText()).newInstance();
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

    private List<String> getTablesSql(DatasourceRequest datasourceRequest) throws Exception {
        JsonNode rootNode = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration());
        TypeReference<List<String>> listTypeReference = new TypeReference<List<String>>() {
        };
        return objectMapper.readValue(rootNode.get("showTableSqls").asText(), listTypeReference);
    }

    public Connection getConnection(String datasourceConfiguration) throws Exception {
        JsonNode rootNode = objectMapper.readTree(datasourceConfiguration);
        Properties props = new Properties();
        if (StringUtils.isNotBlank(rootNode.get("username").asText())) {
            props.setProperty("user", rootNode.get("username").asText());
            if (StringUtils.isNotBlank(rootNode.get("password").asText())) {
                props.setProperty("password", rootNode.get("password").asText());
            }
        }

        Connection conn;
        CoreDriver coreDriver = null;
        String driverClassName;
        ExtendedJdbcClassLoader jdbcClassLoader;
        if (isDefaultClassLoader(rootNode.get("customDriver").asText())) {
            driverClassName = rootNode.get("driver").asText();
            jdbcClassLoader = extendedJdbcClassLoader;
        } else {
            if (coreDriver == null) {
                coreDriver = coreDriverMapper.selectById(rootNode.get("customDriver").asText());
            }
            driverClassName = coreDriver.getDriverClass();
            jdbcClassLoader = getCustomJdbcClassLoader(coreDriver);
        }

        Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(jdbcClassLoader);
            conn = driverClass.connect(rootNode.get("jdbc").asText(), props);
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
