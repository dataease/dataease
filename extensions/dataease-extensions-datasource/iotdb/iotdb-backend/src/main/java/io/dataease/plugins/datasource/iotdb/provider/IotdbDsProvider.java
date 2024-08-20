package io.dataease.plugins.datasource.iotdb.provider;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.base.mapper.DeDriverMapper;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.provider.DefaultJdbcProvider;
import io.dataease.plugins.datasource.provider.ExtendedJdbcClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;


@Component()
public class IotdbDsProvider extends DefaultJdbcProvider {
    @Resource
    private DeDriverMapper deDriverMapper;

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

    /**
     * device 查数据 - 已适配
     * @param datasourceRequest
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        List<String[]> dataList;
        List<TableField> fieldList;
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(datasourceRequest); Statement stat = getStatement(connection, queryTimeout); ResultSet resultSet = stat.executeQuery(datasourceRequest.getQuery())) {
            if (resultSet != null) {
                fieldList = new ArrayList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    TableField tableField = new TableField();
                    tableField.setFieldName(metaData.getColumnLabel(i + 1));
                    tableField.setRemarks(metaData.getColumnLabel(i + 1));
                    tableField.setFieldType(metaData.getColumnTypeName(i + 1));
                    tableField.setFieldSize(metaData.getPrecision(i + 1));
                    fieldList.add(tableField);
                }
                result.put("fieldList", fieldList);
                dataList = getDataResult(resultSet);
                result.put("dataList", dataList);
            }
            return result;
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return new HashMap<>();
    }

    @Override
    public String getType() {
        return "iotdb";
    }

    @Override
    public boolean isUseDatasourcePool() {
        return false;
    }

    /**
     * 连接数据源
     */
    @Override
    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        IotdbConfig ioTDBConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(),
                IotdbConfig.class);

        String username = ioTDBConfig.getUsername();
        String password = ioTDBConfig.getPassword();
        String defaultDriver = ioTDBConfig.getDriver();
        String customDriver = ioTDBConfig.getCustomDriver();
        String url = ioTDBConfig.getJdbc();
        Properties props = new Properties();
        DeDriver deDriver = null;

        if (StringUtils.isNotBlank(username)) {
            props.setProperty("user", username);
            if (StringUtils.isNotBlank(password)) {
                props.setProperty("password", password);
            }
        }
        String surpportVersions = null;
        Connection conn;
        String driverClassName;
        ExtendedJdbcClassLoader jdbcClassLoader;
        if (isDefaultClassLoader(customDriver)) {
            driverClassName = defaultDriver;
            jdbcClassLoader = extendedJdbcClassLoader;
            DeDriver driver = deDriverMapper.selectByPrimaryKey("default-" + datasourceRequest.getDatasource().getType());
            if (driver == null) {
                for (DataSourceType value : SpringContextUtil.getApplicationContext().getBeansOfType(DataSourceType.class).values()) {
                    if (value.getType().equalsIgnoreCase(datasourceRequest.getDatasource().getType())) {
                        surpportVersions = value.getSurpportVersions();
                    }
                }
            } else {
                surpportVersions = driver.getSurpportVersions();
            }
        } else {
            if (deDriver == null) {
                deDriver = deDriverMapper.selectByPrimaryKey(customDriver);
            }
            driverClassName = deDriver.getDriverClass();
            jdbcClassLoader = getCustomJdbcClassLoader(deDriver);
            surpportVersions = deDriver.getSurpportVersions();
        }

        Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(jdbcClassLoader);
            conn = driverClass.connect(url, props);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        if(StringUtils.isNotEmpty(surpportVersions) && surpportVersions.split(",").length > 0){
            if(! Arrays.asList(surpportVersions.split(",")).contains(String.valueOf(conn.getMetaData().getDatabaseMajorVersion()))){
                conn.close();
                DataEaseException.throwException("当前驱动不支持此版本!");
            };
        }
        return conn;
    }

    public List<String[]> getDataResult(ResultSet rs) throws Exception {
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
    /**
     * 获取表名称
     */
    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tables = new ArrayList<>();
        String queryStr = getTablesSql(datasourceRequest);
        JdbcConfiguration jdbcConfiguration =
                new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = Math.max(jdbcConfiguration.getQueryTimeout(), 0);
        try (Connection con = getConnectionFromPool(datasourceRequest); Statement statement = getStatement(con,
                queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
            while (resultSet.next()) {
                tables.add(getTableDesc(datasourceRequest, resultSet));
            }
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }

        return tables;
    }

    /**
     * 获取表名称
     */
    private TableDesc getTableDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        TableDesc tableDesc = new TableDesc();
        tableDesc.setName(resultSet.getString(1));
        return tableDesc;
    }

    /**
     * 获取表字段信息 - 已适配
     */
    @Override
    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> list = new LinkedList<>();
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery("show timeseries "+datasourceRequest.getTable()+".*")) {
            if (resultSet != null) {
                while (resultSet.next()) {
                    final ResultSetMetaData metaData = resultSet.getMetaData();
                    final TableField tableField = new TableField();
                    tableField.setFieldName(resultSet.getString(1));
                    tableField.setFieldType(resultSet.getString(4));
                    tableField.setRemarks(resultSet.getString(1));
                    tableField.setFieldSize(metaData.getPrecision(1));
                    list.add(tableField);
                }
            }
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return list;
    }

    /**
     * 获取数据源
     */
    private String getDatabase(DatasourceRequest datasourceRequest) {
        JdbcConfiguration jdbcConfiguration =
                new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        return jdbcConfiguration.getDataBase();
    }

    /**
     * 获取表字段
     */
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

        if (StringUtils.isNotEmpty(tableField.getFieldType()) && tableField.getFieldType().equalsIgnoreCase("DECIMAL")) {
            tableField.setAccuracy(Integer.valueOf(resultSet.getString("DECIMAL_DIGITS")));
        }

        return tableField;
    }

    /**
     * 检验数据源状态 - 已适配
     */
    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {

        String queryStr = getTablesSql(datasourceRequest);
        JdbcConfiguration jdbcConfiguration =
                new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = Math.max(jdbcConfiguration.getQueryTimeout(), 0);
        try (Connection con = getConnection(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
        } catch (Exception e) {
            DataEaseException.throwException(e.getMessage());
        }
        return "Success";
    }

    /**
     * 显示对应的表的 SQL 语句 - 已适配
     */
    @Override
    public String getTablesSql(DatasourceRequest datasourceRequest) throws Exception {
        return "show devices";
    }

    /**
     * 获取所有的字段 - 已适配
     */
    @Override
    public String getSchemaSql(DatasourceRequest datasourceRequest) {
        return "show timeseries";
    }
}
