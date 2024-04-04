package io.dataease.plugins.datasource.kylin.provider;

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
import io.dataease.plugins.datasource.provider.DefaultJdbcProvider;
import io.dataease.plugins.datasource.provider.ExtendedJdbcClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;


@Component()
public class KylinDsProvider extends DefaultJdbcProvider {
    @Resource
    private DeDriverMapper deDriverMapper;

    @Override
    public String getType() {
        return "kylin";
    }

    @Override
    public boolean isUseDatasourcePool() {
        return false;
    }

    @Override
    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        io.dataease.plugins.datasource.kylin.provider.KylinConfig prestoConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), KylinConfig.class);

        String defaultDriver = prestoConfig.getDriver();
        String customDriver = prestoConfig.getCustomDriver();

        String url = prestoConfig.getJdbc();
        Properties props = new Properties();
        DeDriver deDriver = null;
        if (StringUtils.isNotBlank(prestoConfig.getUsername())) {
            props.setProperty("user", prestoConfig.getUsername());
            if (StringUtils.isNotBlank(prestoConfig.getPassword())) {
                props.setProperty("password", prestoConfig.getPassword());
            }
        }
        String surpportVersions = null;
        Connection conn;
        String driverClassName ;
        ExtendedJdbcClassLoader jdbcClassLoader;
        if(isDefaultClassLoader(customDriver)){
            driverClassName = defaultDriver;
            jdbcClassLoader =  extendedJdbcClassLoader;
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
        }else {
            if(deDriver == null){
                deDriver = deDriverMapper.selectByPrimaryKey(customDriver);
            }
            surpportVersions = deDriver.getSurpportVersions();
            driverClassName = deDriver.getDriverClass();
            jdbcClassLoader = getCustomJdbcClassLoader(deDriver);
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(jdbcClassLoader);
            Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
            conn= driverClass.connect(url, props);
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
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

    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tables = new ArrayList<>();

        List<String> tableNames = new ArrayList<>();
        try (Connection con = getConnectionFromPool(datasourceRequest)) {
            DatabaseMetaData databaseMetaData = con.getMetaData();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", "%", "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if(!tableNames.contains(tableName)){
                    tableNames.add(tableName);
                    TableDesc tableDesc = new TableDesc();
                    tableDesc.setName(tableName);
                    tables.add(tableDesc);
                }
            }
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }

        return tables;
    }


    @Override
    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> list = new LinkedList<>();
        try (Connection connection = getConnectionFromPool(datasourceRequest)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String tableNamePattern = datasourceRequest.getTable();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", tableNamePattern, "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String database;
                if (datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.pg.name()) || datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.ck.name()) || datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceTypes.impala.name())) {
                    database = resultSet.getString("TABLE_SCHEM");
                } else {
                    database = resultSet.getString("TABLE_CAT");
                }
                if (tableName.equals(datasourceRequest.getTable())) {
                    TableField tableField = getTableFiled(resultSet, datasourceRequest);
                    list.add(tableField);
                }

            }
            resultSet.close();
        } catch (SQLException e) {
            DataEaseException.throwException(e);
        } catch (Exception e) {
        }
        return list;
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

        if (StringUtils.isNotEmpty(tableField.getFieldType()) && tableField.getFieldType().equalsIgnoreCase("DECIMAL")) {
            tableField.setAccuracy(Integer.valueOf(resultSet.getString("DECIMAL_DIGITS")));
        }
        return tableField;
    }

    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        try (Connection con = getConnection(datasourceRequest)) {
            List<TableDesc> tables = new ArrayList<>();
            List<String> tableNames = new ArrayList<>();
            ResultSet resultSet = con.getMetaData().getColumns(null, "%", "%", "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if(!tableNames.contains(tableName)){
                    tableNames.add(tableName);
                    TableDesc tableDesc = new TableDesc();
                    tableDesc.setName(tableName);
                    tables.add(tableDesc);
                }
            }
        } catch (Exception e) {
            DataEaseException.throwException(e.getMessage());
        }
        return "Success";
    }


}
