package io.dataease.datasource.provider;

import com.google.gson.Gson;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.MysqlConfigrationDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import io.dataease.datasource.constants.DatasourceTypes.*;
import java.util.Properties;


@Service("jdbc")
public class JdbcProvider extends DatasourceProvider{

    @Override
    public List<String[]> getData() throws Exception {
        List<String[]> list = new LinkedList<>();
        try (
            Connection connection = getConnection();
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(getQuery())
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    int columType = metaData.getColumnType(j + 1);
                    switch (columType) {
                        case java.sql.Types.DATE:
                            row[j] = rs.getDate(j + 1).toString();
                            break;
                        default:
                            row[j] = rs.getString(j + 1);
                            break;
                    }
                }
                list.add(row);
            }
        } catch (Exception e) {
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<String> getTables() throws Exception {
        List<String> tables = new ArrayList<>();

        String queryStr = "show tables";
        try (Connection con = getConnection(); Statement ps = con.createStatement()) {
            ResultSet resultSet = ps.executeQuery(queryStr);
            while (resultSet.next()){
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }
        return tables;
    }

    @Override
    public void test() throws Exception {
        String queryStr = "show tables";
        try (Connection con = getConnection(); Statement ps = con.createStatement()) {
            ResultSet resultSet = ps.executeQuery(queryStr);
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage(), e);
        }
    }


    private Connection getConnection() throws Exception {
        String username = null;
        String password = null;
        String driver = null;
        String jdbcurl = null;
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(getDatasource().getType());
        switch (datasourceType){
            case mysql:
                MysqlConfigrationDTO mysqlConfigrationDTO  = new Gson().fromJson(getDatasource().getConfiguration(), MysqlConfigrationDTO.class);
                username = mysqlConfigrationDTO.getUsername();
                password = mysqlConfigrationDTO.getPassword();
                driver = mysqlConfigrationDTO.getDriver();
                jdbcurl = mysqlConfigrationDTO.getJdbc();
                break;
            default:
                break;
        }

        Class.forName(driver);
        Properties props = new Properties();
        props.setProperty("user", username);
        if (StringUtils.isNotBlank(password)) {
            props.setProperty("password", password);
        }
        return DriverManager.getConnection(jdbcurl, props);
    }

}
