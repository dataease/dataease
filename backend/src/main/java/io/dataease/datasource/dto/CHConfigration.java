package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CHConfigration extends JdbcDTO {

    private String driver = "ru.yandex.clickhouse.ClickHouseDriver";

    public String getJdbc() {
        // 连接参数先写死，后边要把编码、时区等参数放到数据源的设置中
        return "jdbc:clickhouse://HOSTNAME:PORT/DATABASE"
                .replace("HOSTNAME", getHost().trim())
                .replace("PORT", getPort().toString().trim())
                .replace("DATABASE", getDataBase().trim());
    }
}