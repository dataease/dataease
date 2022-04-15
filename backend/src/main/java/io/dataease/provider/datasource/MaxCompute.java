package io.dataease.provider.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.gson.Gson;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.provider.DefaultJdbcProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;


@Component("maxcompute")
public class MaxCompute extends DefaultJdbcProvider {

    @Override
    public String getName(){
        return "maxcompute";
    }

    @Override
    public boolean isUseDatasourcePool(){
        return false;
    }

    @Override
    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        Properties props = new Properties();
        MaxcomputeConfig maxcomputeConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MaxcomputeConfig.class);
        String username = maxcomputeConfig.getUsername();
        String password = maxcomputeConfig.getPassword();
        String driver = maxcomputeConfig.getDriver();
        String url = maxcomputeConfig.getUrl();
        Driver driverClass = (Driver) extendedJdbcClassLoader.loadClass(driver).newInstance();

        if (StringUtils.isNotBlank(username)) {
            props.setProperty("user", username);
            if (StringUtils.isNotBlank(password)) {
                props.setProperty("password", password);
            }
        }
        Connection conn = driverClass.connect(url, props);
        return conn;
    }

    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tables = new ArrayList<>();
        String queryStr = getTablesSql(datasourceRequest);
        try (Connection con = getConnectionFromPool(datasourceRequest); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(queryStr)) {
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
    public List<TableField> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        datasourceRequest.setQuery("select * from " + datasourceRequest.getTable() + " limit 0");
        return fetchResultField(datasourceRequest);
    }

}
