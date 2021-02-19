package io.dataease;

import io.dataease.datasource.JdbcDataProvider;

import java.util.HashMap;
import java.util.Map;

public class MyTest {

    public static void main(String[] args) throws Exception {
        Map<String, String> dataSource = new HashMap<>();
        dataSource.put("driver", "com.mysql.cj.jdbc.Driver");
        dataSource.put("jdbc_url", "jdbc:mysql://localhost:3306/dataease?characterEncoding=utf8&useSSL=true");
        dataSource.put("database", "dataease");
        dataSource.put("user", "root");
        dataSource.put("passwd", "jinlong");
        dataSource.put("", "");
        Map<String, String> query = new HashMap<>();
        query.put("sql", "select table_name from information_schema.tables where table_schema='DATABASE';".replace("DATABASE", dataSource.get("database")));
//        query.put("sql", "SELECT * FROM dataease.test_users;");
        JdbcDataProvider jdbcDataProvider = new JdbcDataProvider();
        jdbcDataProvider.setDataSource(dataSource);
        jdbcDataProvider.setQuery(query);
//        jdbcDataProvider.test();
//        jdbcDataProvider.getData();
        for (String showTable : jdbcDataProvider.showTables()) {
            System.out.println(showTable);
        }


    }

}
