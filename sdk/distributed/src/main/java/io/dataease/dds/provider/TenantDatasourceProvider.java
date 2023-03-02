package io.dataease.dds.provider;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TenantDatasourceProvider {

    private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    private static final String DS_NAME_PREFIX = "tenant_%s_%s";

    private static final String ALL_TENANT_SQL = "select d.*, i.create_time from tenant_db d left join tenant_info i on d.tenant_id = i.id";
    private static final String ONE_TENANT_SQL = "select d.*, i.create_time from tenant_db d left join tenant_info i on d.tenant_id = i.id where d.tenant_id = ? ";


    /**
     * 这里需要加上异常处理
     * 某个数据源不能构建成功 continue 不能影响全局
     * @param manage
     * @return
     */
    public static Map<String, DataSource> getDbInfo(DataSource manage) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = manage.getConnection();
            statement = connection.prepareStatement(ALL_TENANT_SQL);
            resultSet = statement.executeQuery();
            List<Map<String, Object>> mapList = parseResultSet(resultSet);
            if (!CollectionUtils.isEmpty(mapList)) {
                return mapList.stream().collect(Collectors.toMap(TenantDatasourceProvider::getDsKey, TenantDatasourceProvider::buildHikari));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(connection,statement, resultSet);
        }
        return null;
    }



    public static Map<String, DataSource> getDbInfo(DataSource manage, Long tenantId) throws Exception{


        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = manage.getConnection();
            statement = connection.prepareStatement(ONE_TENANT_SQL);
            statement.setLong(1, tenantId);
            resultSet = statement.executeQuery();
            List<Map<String, Object>> mapList = parseResultSet(resultSet);
            if (!CollectionUtils.isEmpty(mapList)) {
                return mapList.stream().collect(Collectors.toMap(TenantDatasourceProvider::getDsKey, TenantDatasourceProvider::buildHikari));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(connection,statement, resultSet);
        }
        return null;

    }

    private static String getDsKey(Map<String, Object> row) {
        Long tenantId = (Long) row.get("tenant_id");
        Long createTime = (Long) row.get("create_time");
        Object dsKeyObj = null;
        if (!ObjectUtils.isEmpty((dsKeyObj = row.get("ds_key")))) {
            return dsKeyObj.toString();
        }
        return String.format(DS_NAME_PREFIX, tenantId.toString(), createTime.toString());
    }

    private static DataSource buildHikari(Map<String, Object> param) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(param.getOrDefault("driver_class", MYSQL_DRIVER_CLASS_NAME).toString());
        config.setJdbcUrl(param.get("url").toString());
        config.setUsername(param.get("username").toString());
        config.setPassword(param.get("pwd").toString());
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return hikariDataSource;
    }

    private static List<Map<String, Object>> parseResultSet(ResultSet resultSet) throws Exception{
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                Object cellVal = resultSet.getObject(i);
                String columnLabel = metaData.getColumnLabel(i);
                row.put(columnLabel, cellVal);
            }
            resultList.add(row);
        }
        return resultList;
    }

    public static void close(Connection connection, PreparedStatement statement, ResultSet rs) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
