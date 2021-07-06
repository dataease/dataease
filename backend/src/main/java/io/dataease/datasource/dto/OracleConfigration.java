package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleConfigration extends JdbcDTO {

    private String driver = "oracle.jdbc.driver.OracleDriver";
    private String connectionType;
    private String schema;

    public String getJdbc() {
        // 连接参数先写死，后边要把编码、时区等参数放到数据源的设置中
        if(getConnectionType().equalsIgnoreCase("serviceName")){
            return "jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getHost())
                    .replace("PORT", getPort().toString())
                    .replace("DATABASE", getDataBase());
        }else {
            return "jdbc:oracle:thin:@HOSTNAME:PORT:DATABASE"
                    .replace("HOSTNAME", getHost())
                    .replace("PORT", getPort().toString())
                    .replace("DATABASE", getDataBase());
        }
    }
}
