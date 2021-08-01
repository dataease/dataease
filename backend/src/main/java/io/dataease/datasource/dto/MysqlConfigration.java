package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MysqlConfigration extends JdbcDTO {

    private String driver = "com.mysql.cj.jdbc.Driver";

    public String getJdbc() {
        // 连接参数先写死，后边要把编码、时区等参数放到数据源的设置中
        return "jdbc:mysql://HOSTNAME:PORT/DATABASE?characterEncoding=UTF-8&connectTimeout=5000"
                .replace("HOSTNAME", getHost())
                .replace("PORT", getPort().toString())
                .replace("DATABASE", getDataBase());
    }
}