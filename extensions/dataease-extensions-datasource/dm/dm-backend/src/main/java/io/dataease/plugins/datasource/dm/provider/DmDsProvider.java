package io.dataease.plugins.datasource.dm.provider;

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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;


@Component()
public class DmDsProvider extends DefaultJdbcProvider {
    @Resource
    private DeDriverMapper deDriverMapper;

    @Override
    public String getType() {
        return "dm";
    }

    @Override
    public boolean isUseDatasourcePool() {
        return false;
    }

    @Override
    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        DmConfig dmConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), DmConfig.class);

        String defaultDriver = dmConfig.getDriver();
        String customDriver = dmConfig.getCustomDriver();

        String url = dmConfig.getJdbc();
        Properties props = new Properties();
        DeDriver deDriver = null;
        if (StringUtils.isNotEmpty(dmConfig.getAuthMethod()) && dmConfig.getAuthMethod().equalsIgnoreCase("kerberos")) {
            System.setProperty("java.security.krb5.conf", "/opt/dataease/conf/krb5.conf");
            ExtendedJdbcClassLoader classLoader;
            if (isDefaultClassLoader(customDriver)) {
                classLoader = extendedJdbcClassLoader;
            } else {
                deDriver = deDriverMapper.selectByPrimaryKey(customDriver);
                classLoader = getCustomJdbcClassLoader(deDriver);
            }
            Class<?> ConfigurationClass = classLoader.loadClass("org.apache.hadoop.conf.Configuration");
            Method set = ConfigurationClass.getMethod("set", String.class, String.class);
            Object obj = ConfigurationClass.newInstance();
            set.invoke(obj, "hadoop.security.authentication", "Kerberos");

            Class<?> UserGroupInformationClass = classLoader.loadClass("org.apache.hadoop.security.UserGroupInformation");
            Method setConfiguration = UserGroupInformationClass.getMethod("setConfiguration", ConfigurationClass);
            Method loginUserFromKeytab = UserGroupInformationClass.getMethod("loginUserFromKeytab", String.class, String.class);
            setConfiguration.invoke(null, obj);
            loginUserFromKeytab.invoke(null, dmConfig.getUsername(), "/opt/dataease/conf/" + dmConfig.getPassword());
        } else {
            if (StringUtils.isNotBlank(dmConfig.getUsername())) {
                props.setProperty("user", dmConfig.getUsername());
                if (StringUtils.isNotBlank(dmConfig.getPassword())) {
                    props.setProperty("password", dmConfig.getPassword());
                }
            }
        }

        Connection conn;
        String surpportVersions = null;
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
            surpportVersions = deDriver.getSurpportVersions();
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
        if (StringUtils.isNotEmpty(surpportVersions) && surpportVersions.split(",").length > 0) {
            if (!Arrays.asList(surpportVersions.split(",")).contains(String.valueOf(conn.getMetaData().getDatabaseMajorVersion()))) {
                conn.close();
                DataEaseException.throwException("当前驱动不支持此版本!");
            }
        }
        return conn;
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

        return tables;
    }

    private TableDesc getTableDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        TableDesc tableDesc = new TableDesc();
        tableDesc.setName(resultSet.getString(1));
        return tableDesc;
    }

    @Override
    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> list = new LinkedList<>();
        try (Connection connection = getConnectionFromPool(datasourceRequest)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", datasourceRequest.getTable(), "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String database;
                database = resultSet.getString("TABLE_CAT");
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
            DataEaseException.throwException("Data source connection exception: " + e.getMessage());
        }
        return list;
    }

    private String getDatabase(DatasourceRequest datasourceRequest) {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        return jdbcConfiguration.getDataBase();
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

        if (datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.hive.name()) && tableField.getFieldType().equalsIgnoreCase("BOOLEAN")) {
            tableField.setFieldSize(1);
        } else {
            String size = resultSet.getString("COLUMN_SIZE");
            if (size == null) {
                tableField.setFieldSize(1);
            } else {
                tableField.setFieldSize(Integer.valueOf(size));
            }
        }
        return tableField;
    }

    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        String queryStr = getTablesSql(datasourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(datasourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
        } catch (Exception e) {
            e.printStackTrace();
            DataEaseException.throwException(e.getMessage());
        }
        return "Success";
    }


    @Override
    public String getTablesSql(DatasourceRequest datasourceRequest) throws Exception {
        DmConfig dmConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), DmConfig.class);
        if (StringUtils.isEmpty(dmConfig.getSchema())) {
            throw new Exception("Database schema is empty.");
        }
        return "select table_name from all_tab_comments where owner='OWNER' AND table_type = 'TABLE' ".replaceAll("OWNER", dmConfig.getSchema());
    }

    @Override
    public String getSchemaSql(DatasourceRequest datasourceRequest) {
        return "select OBJECT_NAME from dba_objects where object_type='SCH'";
    }

}
