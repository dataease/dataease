package io.dataease.plugins.datasource.mongo.provider;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.base.mapper.DeDriverMapper;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.mongo.query.MongoConstants;
import io.dataease.plugins.datasource.provider.DefaultJdbcProvider;
import io.dataease.plugins.datasource.provider.ExtendedJdbcClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


@Component()
public class MongobiDsProvider extends DefaultJdbcProvider {
    @Resource
    private DeDriverMapper deDriverMapper;

    @Override
    public String getType() {
        return "mongobi";
    }

    @Override
    public boolean isUseDatasourcePool() {
        return false;
    }

    @Override
    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        MongoConfig mongoConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MongoConfig.class);

        String defaultDriver = mongoConfig.getDriver();
        String customDriver = mongoConfig.getCustomDriver();

        String url = mongoConfig.getJdbc();
        Properties props = new Properties();
        DeDriver deDriver = null;
        if (StringUtils.isNotBlank(mongoConfig.getUsername())) {
            props.setProperty("user", mongoConfig.getUsername());
            if (StringUtils.isNotBlank(mongoConfig.getPassword())) {
                props.setProperty("password", mongoConfig.getPassword());
            }
        }
        String surpportVersions = null;
        Connection conn;
        String driverClassName ;
        ExtendedJdbcClassLoader jdbcClassLoader;
        if(isDefaultClassLoader(customDriver)){
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
        }else {
            if(deDriver == null){
                deDriver = deDriverMapper.selectByPrimaryKey(customDriver);
            }
            driverClassName = deDriver.getDriverClass();
            surpportVersions = deDriver.getSurpportVersions();
            jdbcClassLoader = getCustomJdbcClassLoader(deDriver);
        }

        Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(jdbcClassLoader);
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

        datasourceRequest.setQuery("select * from " +  String.format(MongoConstants.KEYWORD_TABLE, datasourceRequest.getTable()) + " limit 0");
        return fetchResultField(datasourceRequest);
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
        return "show tables";
    }

}
