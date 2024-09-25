package io.dataease.datasource.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDriver;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.type.*;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.provider.DriverShim;
import io.dataease.extensions.datasource.provider.ExtendedJdbcClassLoader;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.i18n.Translator;
import io.dataease.utils.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component("calciteProvider")
public class CalciteProvider extends Provider {

    @Resource
    protected CoreDatasourceMapper coreDatasourceMapper;
    @Resource
    private EngineManage engineManage;
    protected ExtendedJdbcClassLoader extendedJdbcClassLoader;
    private Map<Long, ExtendedJdbcClassLoader> customJdbcClassLoaders = new HashMap<>();
    private final String FILE_PATH = "/opt/dataease2.0/drivers";
    private final String CUSTOM_PATH = "/opt/dataease2.0/custom-drivers/";
    private static String split = "DE";

    @Resource
    private CommonThreadPool commonThreadPool;

    @PostConstruct
    public void init() throws Exception {
        try {
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
        } catch (Exception e) {

        }
    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        List<String> schemas = new ArrayList<>();
        String queryStr = getSchemaSql(datasourceRequest.getDatasource());
        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource()); Statement statement = getStatement(con.getConnection(), 30); ResultSet resultSet = statement.executeQuery(queryStr)) {
            while (resultSet.next()) {
                schemas.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return schemas;
    }

    @Override
    public List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) {
        List<DatasetTableDTO> tables = new ArrayList<>();

        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource()); Statement statement = getStatement(con.getConnection(), 30)) {
            datasourceRequest.setDsVersion(con.getConnection().getMetaData().getDatabaseMajorVersion());
            List<String> tablesSqls = getTablesSql(datasourceRequest);
            for (String tablesSql : tablesSqls) {
                ResultSet resultSet = statement.executeQuery(tablesSql);
                while (resultSet.next()) {
                    tables.add(getTableDesc(datasourceRequest, resultSet));
                }
            }
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return tables;
    }


    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case pg:
                DatasourceConfiguration configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Pg.class);
                List<String> schemas = getSchema(datasourceRequest);
                if (CollectionUtils.isEmpty(schemas) || !schemas.contains(configuration.getSchema())) {
                    DEException.throwException("无效的 schema！");
                }
                break;
            default:
                break;
        }

        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource())) {
            datasourceRequest.setDsVersion(con.getConnection().getMetaData().getDatabaseMajorVersion());
            String querySql = getTablesSql(datasourceRequest).get(0);
            Statement statement = getStatement(con.getConnection(), 30);
            ResultSet resultSet = statement.executeQuery(querySql);
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            throw e;
        }
        return "Success";
    }

    @Override
    public Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException {
        // 不跨数据源
        if (datasourceRequest.getDsList().size() == 1) {
            return jdbcFetchResultField(datasourceRequest);
        }

        List<TableField> datasetTableFields = new ArrayList<>();
        List<String[]> list = new LinkedList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Connection connection = take();
        try {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            statement = calciteConnection.prepareStatement(datasourceRequest.getQuery());
            resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                TableField tableField = new TableField();
                tableField.setOriginName(metaData.getColumnLabel(i));
                tableField.setType(metaData.getColumnTypeName(i));
                tableField.setPrecision(metaData.getPrecision(i));
                int deType = FieldUtils.transType2DeType(tableField.getType());
                tableField.setDeExtractType(deType);
                tableField.setDeType(deType);
                tableField.setScale(metaData.getScale(i));
                datasetTableFields.add(tableField);
            }
            list = getDataResult(resultSet);
        } catch (Exception | AssertionError e) {
            String msg;
            if (e.getCause() != null && e.getCause().getCause() != null) {
                msg = e.getMessage() + " [" + e.getCause().getCause().getMessage() + "]";
            } else {
                msg = e.getMessage();
            }
            DEException.throwException(Translator.get("i18n_fetch_error") + msg);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fields", datasetTableFields);
        map.put("data", list);
        return map;
    }


    private List<TableField> fetchResultField(ResultSet rs) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int j = 0; j < columnCount; j++) {
            String columnName = metaData.getColumnName(j + 1);
            String label = StringUtils.isNotEmpty(metaData.getColumnLabel(j + 1)) ? metaData.getColumnLabel(j + 1) : columnName;
            TableField tableField = new TableField();
            tableField.setOriginName(columnName);
            tableField.setName(label);
            tableField.setType(metaData.getColumnTypeName(j + 1));
            tableField.setFieldType(tableField.getType());
            int deType = FieldUtils.transType2DeType(tableField.getType());
            tableField.setDeExtractType(deType);
            tableField.setDeType(deType);
            fieldList.add(tableField);
        }
        return fieldList;
    }

    @Override
    public List<TableField> fetchTableField(DatasourceRequest datasourceRequest) throws DEException {
        List<TableField> datasetTableFields = new ArrayList<>();
        DatasourceSchemaDTO datasourceSchemaDTO = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(datasourceSchemaDTO);

        DatasourceConfiguration datasourceConfiguration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), DatasourceConfiguration.class);

        String table = datasourceRequest.getTable();
        if (StringUtils.isEmpty(table)) {
            ResultSet resultSet = null;
            try (ConnectionObj con = getConnection(datasourceRequest.getDatasource()); Statement statement = getStatement(con.getConnection(), 30)) {
                if (DatasourceConfiguration.DatasourceType.valueOf(datasourceSchemaDTO.getType()) == DatasourceConfiguration.DatasourceType.oracle) {
                    statement.executeUpdate("ALTER SESSION SET CURRENT_SCHEMA = " + datasourceConfiguration.getSchema());
                }
                resultSet = statement.executeQuery(datasourceRequest.getQuery());
                datasetTableFields.addAll(getField(resultSet, datasourceRequest));
            } catch (Exception e) {
                DEException.throwException(e.getMessage());
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            ResultSet resultSet = null;
            try (ConnectionObj con = getConnection(datasourceRequest.getDatasource()); Statement statement = getStatement(con.getConnection(), 30)) {
                datasourceRequest.setDsVersion(con.getConnection().getMetaData().getDatabaseMajorVersion());
                if (datasourceRequest.getDatasource().getType().equalsIgnoreCase("mongo") || isDorisCatalog(datasourceRequest)) {
                    resultSet = statement.executeQuery("select * from " + table + " limit 0 offset 0 ");
                    return fetchResultField(resultSet);
                }
                resultSet = statement.executeQuery(getTableFiledSql(datasourceRequest));
                while (resultSet.next()) {
                    TableField tableFieldDesc = getTableFieldDesc(datasourceRequest, resultSet);
                    boolean repeat = false;
                    for (TableField ele : datasetTableFields) {
                        if (StringUtils.equalsIgnoreCase(ele.getOriginName(), tableFieldDesc.getOriginName())) {
                            repeat = true;
                            break;
                        }
                    }
                    if (!repeat) {
                        datasetTableFields.add(tableFieldDesc);
                    }
                }
            } catch (Exception e) {
                DEException.throwException(e.getMessage());
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return datasetTableFields;
    }

    private boolean isDorisCatalog(DatasourceRequest datasourceRequest) {
        if (!datasourceRequest.getDatasource().getType().equalsIgnoreCase("doris")) {
            return false;
        }
        DatasourceConfiguration configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
        String database = "";
        if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
            database = configuration.getDataBase();
        } else {
            Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
            Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
            matcher.find();
            String[] databasePrams = matcher.group(3).split("\\?");
            database = databasePrams[0];
        }
        return database.contains(".");
    }

    @Override
    public ConnectionObj getConnection(DatasourceDTO coreDatasource) throws Exception {
        ConnectionObj connectionObj = new ConnectionObj();
        DatasourceConfiguration configuration = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(coreDatasource.getType());
        switch (datasourceType) {
            case mysql:
            case mongo:
            case StarRocks:
            case doris:
            case TiDB:
            case mariadb:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Mysql.class);
                break;
            case impala:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Impala.class);
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
            default:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Mysql.class);
        }
        startSshSession(configuration, connectionObj, null);
        Properties props = new Properties();
        if (StringUtils.isNotBlank(configuration.getUsername())) {
            props.setProperty("user", configuration.getUsername());
        }
        if (StringUtils.isNotBlank(configuration.getPassword())) {
            props.setProperty("password", configuration.getPassword());
        }
        String driverClassName = configuration.getDriver();
        ExtendedJdbcClassLoader jdbcClassLoader = extendedJdbcClassLoader;
        Connection conn = null;
        try {
            Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
            conn = driverClass.connect(configuration.getJdbc(), props);

        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        connectionObj.setConnection(conn);
        return connectionObj;
    }

    private DatasetTableDTO getTableDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        DatasetTableDTO tableDesc = new DatasetTableDTO();
        tableDesc.setDatasourceId(datasourceRequest.getDatasource().getId());
        tableDesc.setType("db");
        tableDesc.setTableName(resultSet.getString(1));
        if (resultSet.getMetaData().getColumnCount() > 1) {
            tableDesc.setName(resultSet.getString(2));
        } else {
            tableDesc.setName(resultSet.getString(1));
        }
        return tableDesc;
    }

    private List<String> getDriver() {
        List<String> drivers = new ArrayList<>();
        Map<String, DatasourceConfiguration> beansOfType = CommonBeanFactory.getApplicationContext().getBeansOfType((DatasourceConfiguration.class));
        beansOfType.keySet().forEach(key -> drivers.add(beansOfType.get(key).getDriver()));
        return drivers;
    }

    public Map<String, Object> jdbcFetchResultField(DatasourceRequest datasourceRequest) throws DEException {
        DatasourceSchemaDTO value = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(value);

        DatasourceConfiguration datasourceConfiguration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), DatasourceConfiguration.class);

        Map<String, Object> map = new LinkedHashMap<>();
        List<TableField> fieldList = new ArrayList<>();
        List<String[]> dataList = new LinkedList<>();

        // schema
        ResultSet resultSet = null;
        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource()); Statement statement = getPreparedStatement(con.getConnection(), datasourceConfiguration.getQueryTimeout(), datasourceRequest.getQuery(), datasourceRequest.getTableFieldWithValues())) {
            if (DatasourceConfiguration.DatasourceType.valueOf(value.getType()) == DatasourceConfiguration.DatasourceType.oracle) {
                statement.executeUpdate("ALTER SESSION SET CURRENT_SCHEMA = " + datasourceConfiguration.getSchema());
            }

            if (CollectionUtils.isNotEmpty(datasourceRequest.getTableFieldWithValues())) {
                LogUtil.info("execWithPreparedStatement sql: " + datasourceRequest.getQuery());
                for (int i = 0; i < datasourceRequest.getTableFieldWithValues().size(); i++) {
                    ((PreparedStatement) statement).setObject(i + 1, datasourceRequest.getTableFieldWithValues().get(i).getValue(), datasourceRequest.getTableFieldWithValues().get(i).getType());
                    LogUtil.info("execWithPreparedStatement param[" + (i + 1) + "]: " + datasourceRequest.getTableFieldWithValues().get(i).getValue());
                }
                resultSet = ((PreparedStatement) statement).executeQuery();
            } else {
                resultSet = statement.executeQuery(datasourceRequest.getQuery());
            }
            fieldList = getField(resultSet, datasourceRequest);
            dataList = getData(resultSet, datasourceRequest);
        } catch (SQLException e) {
            DEException.throwException("SQL ERROR: " + e.getMessage());
        } catch (Exception e) {
            DEException.throwException("Data source connection exception: " + e.getMessage());
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        map.put("fields", fieldList);
        map.put("data", dataList);
        return map;
    }

    @Override
    public void exec(DatasourceRequest datasourceRequest) throws DEException {
        DatasourceSchemaDTO value = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(value);

        DatasourceConfiguration datasourceConfiguration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), DatasourceConfiguration.class);

        // schema
        ResultSet resultSet = null;
        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource()); Statement statement = getPreparedStatement(con.getConnection(), datasourceConfiguration.getQueryTimeout(), datasourceRequest.getQuery(), datasourceRequest.getTableFieldWithValues())) {
            if (DatasourceConfiguration.DatasourceType.valueOf(value.getType()) == DatasourceConfiguration.DatasourceType.oracle) {
                statement.executeUpdate("ALTER SESSION SET CURRENT_SCHEMA = " + datasourceConfiguration.getSchema());
            }

            if (CollectionUtils.isNotEmpty(datasourceRequest.getTableFieldWithValues())) {
                LogUtil.info("execWithPreparedStatement sql: " + datasourceRequest.getQuery());
                for (int i = 0; i < datasourceRequest.getTableFieldWithValues().size(); i++) {
                    ((PreparedStatement) statement).setObject(i + 1, datasourceRequest.getTableFieldWithValues().get(i).getValue(), datasourceRequest.getTableFieldWithValues().get(i).getType());
                    LogUtil.info("execWithPreparedStatement param[" + (i + 1) + "]: " + datasourceRequest.getTableFieldWithValues().get(i).getValue());
                }
                ((PreparedStatement) statement).execute();
            } else {
                statement.execute(datasourceRequest.getQuery());
            }

        } catch (SQLException e) {
            DEException.throwException("SQL ERROR: " + e.getMessage());
        } catch (Exception e) {
            DEException.throwException("Data source connection exception: " + e.getMessage());
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int executeUpdate(DatasourceRequest datasourceRequest) throws DEException {
        DatasourceSchemaDTO value = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(value);

        DatasourceConfiguration datasourceConfiguration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), DatasourceConfiguration.class);

        // schema
        ResultSet resultSet = null;
        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource()); Statement statement = getPreparedStatement(con.getConnection(), datasourceConfiguration.getQueryTimeout(), datasourceRequest.getQuery(), datasourceRequest.getTableFieldWithValues())) {
            if (DatasourceConfiguration.DatasourceType.valueOf(value.getType()) == DatasourceConfiguration.DatasourceType.oracle) {
                statement.executeUpdate("ALTER SESSION SET CURRENT_SCHEMA = " + datasourceConfiguration.getSchema());
            }

            if (CollectionUtils.isNotEmpty(datasourceRequest.getTableFieldWithValues())) {
                LogUtil.info("execWithPreparedStatement sql: " + datasourceRequest.getQuery());
                for (int i = 0; i < datasourceRequest.getTableFieldWithValues().size(); i++) {
                    ((PreparedStatement) statement).setObject(i + 1, datasourceRequest.getTableFieldWithValues().get(i).getValue(), datasourceRequest.getTableFieldWithValues().get(i).getType());
                    LogUtil.info("execWithPreparedStatement param[" + (i + 1) + "]: " + datasourceRequest.getTableFieldWithValues().get(i).getValue());
                }
                return ((PreparedStatement) statement).executeUpdate();
            } else {
                return statement.executeUpdate(datasourceRequest.getQuery());
            }

        } catch (SQLException e) {
            DEException.throwException("SQL ERROR: " + e.getMessage());
        } catch (Exception e) {
            DEException.throwException("Data source connection exception: " + e.getMessage());
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }

    private List<TableField> getField(ResultSet rs, DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int j = 0; j < columnCount; j++) {
            String f = metaData.getColumnName(j + 1);
            if (StringUtils.equalsIgnoreCase(f, "DE_ROWNUM")) {
                continue;
            }
            String l = StringUtils.isNotEmpty(metaData.getColumnLabel(j + 1)) ? metaData.getColumnLabel(j + 1) : f;
            String t = metaData.getColumnTypeName(j + 1).toUpperCase();
            TableField field = new TableField();
            field.setOriginName(l);
            field.setName(l);
            field.setFieldType(t);
            field.setType(t);
            fieldList.add(field);
        }
        return fieldList;
    }

    private List<String[]> getData(ResultSet rs, DatasourceRequest datasourceRequest) throws Exception {
        String charset = null;
        String targetCharset = "UTF-8";
        if (datasourceRequest != null && datasourceRequest.getDatasource().getType().equalsIgnoreCase("oracle")) {
            DatasourceConfiguration jdbcConfiguration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), DatasourceConfiguration.class);

            if (StringUtils.isNotEmpty(jdbcConfiguration.getCharset()) && !jdbcConfiguration.getCharset().equalsIgnoreCase("Default")) {
                charset = jdbcConfiguration.getCharset();
            }
            if (StringUtils.isNotEmpty(jdbcConfiguration.getTargetCharset()) && !jdbcConfiguration.getTargetCharset().equalsIgnoreCase("Default")) {
                targetCharset = jdbcConfiguration.getTargetCharset();
            }
        }
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
                    case Types.NUMERIC:
                        BigDecimal bigDecimal = rs.getBigDecimal(j + 1);
                        row[j] = bigDecimal == null ? null : bigDecimal.toString();
                        break;
                    default:
                        if (metaData.getColumnTypeName(j + 1).toLowerCase().equalsIgnoreCase("blob")) {
                            row[j] = rs.getBlob(j + 1) == null ? "" : rs.getBlob(j + 1).toString();
                        } else {
                            if (charset != null && StringUtils.isNotEmpty(rs.getString(j + 1))) {
                                String originStr = new String(rs.getString(j + 1).getBytes(charset), targetCharset);
                                row[j] = new String(originStr.getBytes("UTF-8"), "UTF-8");
                            } else {
                                row[j] = rs.getString(j + 1);
                            }
                        }

                        break;
                }
            }
            list.add(row);
        }
        return list;
    }

    @Override
    public void hidePW(DatasourceDTO datasourceDTO) {
        DatasourceConfiguration configuration = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceDTO.getType());
        switch (datasourceType) {
            case mysql:
            case mongo:
            case mariadb:
            case TiDB:
            case StarRocks:
            case doris:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Mysql.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case pg:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Pg.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case redshift:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Redshift.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case ck:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), CK.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case impala:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Impala.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split(";");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            default:
                break;
        }
    }

    private TableField getTableFieldDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        TableField tableField = new TableField();
        tableField.setOriginName(resultSet.getString(1));
        tableField.setType(resultSet.getString(2).toUpperCase());
        tableField.setFieldType(tableField.getType());
        int deType = FieldUtils.transType2DeType(tableField.getType());
        tableField.setDeExtractType(deType);
        tableField.setDeType(deType);
        tableField.setName(resultSet.getString(3));
        try {
            tableField.setPrimary(resultSet.getInt(4) > 0);
        } catch (Exception e) {
        }
        return tableField;
    }

    public Connection initConnection(Map<Long, DatasourceSchemaDTO> dsMap) {
        Connection connection = getCalciteConnection();
        CalciteConnection calciteConnection = null;
        try {
            calciteConnection = connection.unwrap(CalciteConnection.class);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        SchemaPlus rootSchema = buildSchema(datasourceRequest, calciteConnection);
        return connection;
    }

    private void registerDriver() {
        for (String driverClass : getDriver()) {
            try {
                Driver driver = (Driver) extendedJdbcClassLoader.loadClass(driverClass).newInstance();
                DriverManager.registerDriver(new DriverShim(driver));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Connection getCalciteConnection() {
        registerDriver();
        Properties info = new Properties();
        info.setProperty(CalciteConnectionProperty.LEX.camelName(), "JAVA");
        info.setProperty(CalciteConnectionProperty.FUN.camelName(), "all");
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        info.setProperty(CalciteConnectionProperty.PARSER_FACTORY.camelName(), "org.apache.calcite.sql.parser.impl.SqlParserImpl#FACTORY");
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty("remarks", "true");
        Connection connection = null;
        try {
            Class.forName("org.apache.calcite.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:calcite:", info);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return connection;
    }

    // 构建root schema
    private SchemaPlus buildSchema(DatasourceRequest datasourceRequest, CalciteConnection calciteConnection) {
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Map<Long, DatasourceSchemaDTO> dsList = datasourceRequest.getDsList();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsList.entrySet()) {
            DatasourceSchemaDTO ds = next.getValue();
            commonThreadPool.addTask(() -> {
                try {
                    BasicDataSource dataSource = new BasicDataSource();
                    Schema schema = null;
                    DatasourceConfiguration configuration = null;
                    DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(ds.getType());
                    try {
                        if (rootSchema.getSubSchema(ds.getSchemaAlias()) != null) {
                            JdbcSchema jdbcSchema = rootSchema.getSubSchema(ds.getSchemaAlias()).unwrap(JdbcSchema.class);
                            BasicDataSource basicDataSource = (BasicDataSource) jdbcSchema.getDataSource();
                            basicDataSource.close();
                            rootSchema.removeSubSchema(ds.getSchemaAlias());
                        }
                        switch (datasourceType) {
                            case mysql:
                            case mongo:
                            case mariadb:
                            case TiDB:
                            case StarRocks:
                            case doris:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Mysql.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case impala:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Impala.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case sqlServer:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Sqlserver.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case oracle:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Oracle.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case db2:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Db2.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case ck:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), CK.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case pg:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Pg.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case redshift:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Redshift.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case h2:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), H2.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            default:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Mysql.class);
                                dataSource.setUrl(configuration.getJdbc());
                                dataSource.setUsername(configuration.getUsername());
                                dataSource.setPassword(configuration.getPassword());
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return rootSchema;
    }

    private List<String[]> getDataResult(ResultSet rs) {
        List<String[]> list = new LinkedList<>();
        try {
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
                            row[j] = rs.getBoolean(j + 1) ? "true" : "false";
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
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return list;
    }

    private String getTableFiledSql(DatasourceRequest datasourceRequest) {
        String sql = "";
        DatasourceConfiguration configuration = null;
        String database = "";
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case StarRocks:
            case doris:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                if (database.contains(".")) {
                    sql = "select * from " + datasourceRequest.getTable() + " limit 0 offset 0 ";
                } else {
                    sql = String.format("SELECT COLUMN_NAME,DATA_TYPE,COLUMN_COMMENT  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '%s' AND   TABLE_NAME = '%s'", database, datasourceRequest.getTable());
                }
                break;
            case mysql:
            case mongo:
            case mariadb:
            case TiDB:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                sql = String.format("SELECT COLUMN_NAME,DATA_TYPE,COLUMN_COMMENT,IF(COLUMN_KEY='PRI',1,0) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '%s' AND   TABLE_NAME = '%s'", database, datasourceRequest.getTable());
                break;
            case oracle:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Oracle.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                sql = String.format("SELECT a.COLUMN_NAME , a.DATA_TYPE , b.COMMENTS ,0 FROM all_tab_columns a LEFT JOIN all_col_comments b ON a.owner = b.owner AND a.table_name = b.table_name AND a.column_name = b.column_name WHERE a.owner = '%s' AND a.table_name = '%s'   ORDER BY a.table_name, a.column_id", configuration.getSchema(), datasourceRequest.getTable());
                break;
            case db2:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Db2.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                sql = String.format("SELECT COLNAME , TYPENAME , REMARKS FROM SYSCAT.COLUMNS WHERE TABSCHEMA = '%s' AND TABNAME = '%s' ", configuration.getSchema(), datasourceRequest.getTable());
                break;
            case sqlServer:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Sqlserver.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }

                sql = String.format("SELECT \n" + "    c.name ,t.name ,ep.value, 0  \n" + "FROM \n" + "    sys.columns AS c\n" + "LEFT JOIN  sys.extended_properties AS ep ON c.object_id = ep.major_id AND c.column_id = ep.minor_id\n" + "LEFT JOIN sys.types AS t ON c.user_type_id = t.user_type_id\n" + "LEFT JOIN sys.objects AS o ON c.object_id = o.object_id\n" + "WHERE  o.name = '%s'", datasourceRequest.getTable());
                break;
            case pg:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Pg.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                sql = String.format("SELECT\n" + "    a.attname AS ColumnName,\n" + "    t.typname,\n" + "    b.description AS ColumnDescription,\n" + "    0\n" + "FROM\n" + "    pg_class c\n" + "    JOIN pg_attribute a ON a.attrelid = c.oid\n" + "    LEFT JOIN pg_description b ON a.attrelid = b.objoid AND a.attnum = b.objsubid\n" + "    JOIN pg_type t ON a.atttypid = t.oid\n" + "where\n" + " \tc.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = '%s') \n" + "    AND c.relname = '%s'\n" + "    AND a.attnum > 0\n" + "    AND NOT a.attisdropped\n" + "ORDER BY\n" + "    a.attnum;", configuration.getSchema(), datasourceRequest.getTable());
                break;
            case redshift:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                sql = String.format("SELECT\n" + "    a.attname AS ColumnName,\n" + "    t.typname,\n" + "    b.description AS ColumnDescription,\n" + "    0\n" + "FROM\n" + "    pg_class c\n" + "    JOIN pg_attribute a ON a.attrelid = c.oid\n" + "    LEFT JOIN pg_description b ON a.attrelid = b.objoid AND a.attnum = b.objsubid\n" + "    JOIN pg_type t ON a.atttypid = t.oid\n" + "WHERE\n" + "    c.relname = '%s'\n" + "    AND a.attnum > 0\n" + "    AND NOT a.attisdropped\n" + "ORDER BY\n" + "    a.attnum\n" + "   ", datasourceRequest.getTable());
                break;
            case ck:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);

                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:clickhouse://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                sql = String.format(" SELECT\n" + "    name,\n" + "    type,\n" + "    comment,\n" + "    0\n" + "FROM\n" + "    system.columns\n" + "WHERE\n" + "    database = '%s'  \n" + "    AND table = '%s' ", database, datasourceRequest.getTable());
                break;
            case impala:
                sql = String.format("DESCRIBE `%s`", datasourceRequest.getTable());
                break;
            default:
                break;
        }

        return sql;
    }

    private List<String> getTablesSql(DatasourceRequest datasourceRequest) throws DEException {
        List<String> tableSqls = new ArrayList<>();
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        DatasourceConfiguration configuration = null;
        String database = "";
        switch (datasourceType) {
            case StarRocks:
            case doris:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                if (database.contains(".")) {
                    tableSqls.add("show tables");
                } else {
                    tableSqls.add(String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' ;", database));
                }
                break;
            case mongo:
                tableSqls.add("show tables");
                break;
            case mysql:
            case mariadb:
            case TiDB:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                tableSqls.add(String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' ;", database));
                break;
            case oracle:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Oracle.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("select table_name, comments, owner  from all_tab_comments where owner='" + configuration.getSchema() + "' AND table_type = 'TABLE'");
                tableSqls.add("select table_name, comments, owner  from all_tab_comments where owner='" + configuration.getSchema() + "' AND table_type = 'VIEW'");
                break;
            case db2:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Db2.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("select TABNAME, REMARKS from syscat.tables  WHERE TABSCHEMA ='DE_SCHEMA' AND \"TYPE\" = 'T'".replace("DE_SCHEMA", configuration.getSchema()));
                break;
            case sqlServer:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Sqlserver.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("SELECT   \n" + "    t.name AS TableName,  \n" + "    ep.value AS TableDescription  \n" + "FROM   \n" + "    sys.tables t  \n" + "LEFT OUTER JOIN   sys.schemas sc ON sc.schema_id =t.schema_id \n" + "LEFT OUTER JOIN   \n" + "    sys.extended_properties ep ON t.object_id = ep.major_id   \n" + "                               AND ep.minor_id = 0   \n" + "                               AND ep.class = 1  \n" + "                               AND ep.name = 'MS_Description'\n" + "where sc.name ='DS_SCHEMA'".replace("DS_SCHEMA", configuration.getSchema()));
                tableSqls.add("SELECT   \n" + "    t.name AS TableName,  \n" + "    ep.value AS TableDescription  \n" + "FROM   \n" + "    sys.views t  \n" + "LEFT OUTER JOIN   sys.schemas sc ON sc.schema_id =t.schema_id \n" + "LEFT OUTER JOIN   \n" + "    sys.extended_properties ep ON t.object_id = ep.major_id   \n" + "                               AND ep.minor_id = 0   \n" + "                               AND ep.class = 1  \n" + "                               AND ep.name = 'MS_Description'\n" + "where sc.name ='DS_SCHEMA'".replace("DS_SCHEMA", configuration.getSchema()));
                break;
            case pg:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Pg.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("SELECT  \n" + "    relname AS TableName,  \n" + "    obj_description(relfilenode::regclass, 'pg_class') AS TableDescription  \n" + "FROM  \n" + "    pg_class  \n" + "WHERE  \n" + "   relkind in  ('r','p')  \n" + "    AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'SCHEMA') ".replace("SCHEMA", configuration.getSchema()));
                break;
            case redshift:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                tableSqls.add("SELECT  \n" + "    relname AS TableName,  \n" + "    obj_description(relfilenode::regclass, 'pg_class') AS TableDescription  \n" + "FROM  \n" + "    pg_class  \n" + "WHERE  \n" + "   relkind in  ('r','p')  \n" + "    AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'SCHEMA') ".replace("SCHEMA", configuration.getSchema()));
                break;
            case ck:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:clickhouse://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                if (datasourceRequest.getDsVersion() < 22) {
                    tableSqls.add("SELECT name, name FROM system.tables where database='DATABASE';".replace("DATABASE", database));
                } else {
                    tableSqls.add("SELECT name, comment FROM system.tables where database='DATABASE';".replace("DATABASE", database));
                }


                break;
            default:
                tableSqls.add("show tables");
        }
        return tableSqls;

    }

    private String getSchemaSql(DatasourceDTO datasource) throws DEException {
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

    public Statement getStatement(Connection connection, int queryTimeout) {
        if (connection == null) {
            DEException.throwException("Failed to get connection!");
        }
        Statement stat = null;
        try {
            stat = connection.createStatement();
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return stat;
    }

    public Statement getPreparedStatement(Connection connection, int queryTimeout, String sql, List<TableFieldWithValue> values) throws Exception {
        if (connection == null) {
            throw new Exception("Failed to get connection!");
        }
        if (CollectionUtils.isNotEmpty(values)) {
            PreparedStatement stat = null;
            try {
                stat = connection.prepareStatement(sql);
                stat.setQueryTimeout(queryTimeout);
            } catch (Exception e) {
                DEException.throwException(e.getMessage());
            }
            return stat;
        } else {
            return getStatement(connection, queryTimeout);
        }
    }

    protected boolean isDefaultClassLoader(String customDriver) {
        return StringUtils.isEmpty(customDriver) || customDriver.equalsIgnoreCase("default");
    }

    protected ExtendedJdbcClassLoader getCustomJdbcClassLoader(CoreDriver coreDriver) {
        if (coreDriver == null) {
            DEException.throwException("Can not found custom Driver");
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

    private synchronized ExtendedJdbcClassLoader addCustomJdbcClassLoader(CoreDriver coreDriver) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();
            if (classLoader.toString().contains("ExtClassLoader")) {
                break;
            }
        }
        try {
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
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }


    private Connection connection = null;

    public static int capacity = 10;

    public void initConnectionPool() {
        LogUtil.info("Begin to init datasource pool...");
        QueryWrapper<CoreDatasource> datasourceQueryWrapper = new QueryWrapper();
        List<CoreDatasource> coreDatasources = coreDatasourceMapper.selectList(datasourceQueryWrapper).stream().filter(coreDatasource -> !Arrays.asList("folder", "API", "Excel").contains(coreDatasource.getType())).collect(Collectors.toList());
        CoreDatasource engine = engineManage.deEngine();
        if (engine != null) {
            coreDatasources.add(engine);
        }
        Map<Long, DatasourceSchemaDTO> dsMap = new HashMap<>();
        for (CoreDatasource coreDatasource : coreDatasources) {
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
            dsMap.put(datasourceSchemaDTO.getId(), datasourceSchemaDTO);
        }
        LogUtil.info("dsMap size..." + dsMap.keySet().size());
        try {
            commonThreadPool.addTask(() -> {
                try {
                    connection = initConnection(dsMap);
                } catch (Exception e) {
                }
            });

        } catch (Exception e) {

        }
    }

    public void update(DatasourceDTO datasourceDTO) throws DEException {
        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, datasourceDTO);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        try {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = buildSchema(datasourceRequest, calciteConnection);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
    }

    public void updateDsPoolAfterCheckStatus(DatasourceDTO datasourceDTO) throws DEException {
        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, datasourceDTO);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        try {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();
            if (rootSchema.getSubSchema(datasourceSchemaDTO.getSchemaAlias()) == null) {
                buildSchema(datasourceRequest, calciteConnection);
            }
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
    }

    public void delete(CoreDatasource datasource) throws DEException {
        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, datasource);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        try {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();
            if (rootSchema.getSubSchema(datasourceSchemaDTO.getSchemaAlias()) != null) {
                JdbcSchema jdbcSchema = rootSchema.getSubSchema(datasourceSchemaDTO.getSchemaAlias()).unwrap(JdbcSchema.class);
                BasicDataSource basicDataSource = (BasicDataSource) jdbcSchema.getDataSource();
                basicDataSource.close();
                rootSchema.removeSubSchema(datasourceSchemaDTO.getSchemaAlias());
            }
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        Provider.getLPorts().remove(datasource.getId());
        if (Provider.getSessions().get(datasource.getId()) != null) {
            Provider.getSessions().get(datasource.getId()).disconnect();
        }
        Provider.getSessions().remove(datasource.getId());
    }


    public Connection take() {
        if (connection == null) {
            DEException.throwException("初始化连接池失败!");
        }
        return connection;
    }


}
