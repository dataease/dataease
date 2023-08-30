package io.dataease.plugins.datasource.kingbase.provider;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.base.mapper.DeDriverMapper;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.provider.DefaultJdbcProvider;
import io.dataease.plugins.datasource.provider.ExtendedJdbcClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


@Component()
public class KingbaseDsProvider extends DefaultJdbcProvider {
    @Resource
    private DeDriverMapper deDriverMapper;

    @Override
    public String getType() {
        return "kingbase";
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
        KingbaseConfig kingbaseConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(),
                KingbaseConfig.class);

        String username = kingbaseConfig.getUsername();
        String password = kingbaseConfig.getPassword();
        String defaultDriver = kingbaseConfig.getDriver();
        String customDriver = kingbaseConfig.getCustomDriver();
        String url = kingbaseConfig.getJdbc();
        Properties props = new Properties();
        DeDriver deDriver = null;

        if (StringUtils.isNotBlank(username)) {
            props.setProperty("user", username);
            if (StringUtils.isNotBlank(password)) {
                props.setProperty("password", password);
            }
        }
        Connection conn;
        String driverClassName;
        ExtendedJdbcClassLoader jdbcClassLoader;
        if (isDefaultClassLoader(customDriver)) {
            driverClassName = defaultDriver;
            jdbcClassLoader = extendedJdbcClassLoader;
        } else {
            if (deDriver == null) {
                deDriver = deDriverMapper.selectByPrimaryKey(customDriver);
            }
            driverClassName = deDriver.getDriverClass();
            jdbcClassLoader = getCustomJdbcClassLoader(deDriver);
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
        return conn;
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
     * 获取表字段信息
     */
    @Override
    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {

        List<TableField> list = new LinkedList<>();
        try (Connection connection = getConnectionFromPool(datasourceRequest)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String tableNamePattern = datasourceRequest.getTable();
            String schemaPattern = "%";
            ResultSet resultSet = databaseMetaData.getColumns(null, schemaPattern, tableNamePattern, "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (tableName.equals(datasourceRequest.getTable())) {
                    TableField tableField = getTableFiled(resultSet, datasourceRequest);
                    list.add(tableField);
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
            DataEaseException.throwException("Data source connection exception: " + e.getMessage());
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
     * 检验数据源状态
     */
    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {

        String queryStr = getTablesSql(datasourceRequest);
        JdbcConfiguration jdbcConfiguration =
                new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = Math.max(jdbcConfiguration.getQueryTimeout(), 0);
        try (Connection con = getConnection(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
        } catch (Exception e) {
            e.printStackTrace();
            DataEaseException.throwException(e.getMessage());
        }
        return "Success";
    }

    /**
     * 显示对应的表的 SQL 语句
     */
    @Override
    public String getTablesSql(DatasourceRequest datasourceRequest) throws Exception {
        KingbaseConfig kingbaseConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(),
                KingbaseConfig.class);
        if (StringUtils.isEmpty(kingbaseConfig.getSchema())) {
            throw new Exception("Database schema is empty.");
        }
        return ("select tablename from pg_tables where schemaname = 'SCHEMA' ").replaceAll("SCHEMA",
                kingbaseConfig.getSchema());
    }

    /**
     * 获取所有的用户
     */
    @Override
    public String getSchemaSql(DatasourceRequest datasourceRequest) {
        return "SELECT nspname FROM pg_namespace";
    }
}
