package io.dataease.plugins.datasource.iotdb.provider;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * 连接配置信息
 */
@Getter
@Setter
public class IotdbConfig extends JdbcConfiguration {

    private String driver = "org.apache.iotdb.jdbc.IoTDBDriver";//驱动类名
    private String extraParams;


    /**
     * JDBC 拼接
     */
    public String getJdbc() {
        return "jdbc:iotdb://HOST:PORT/"
                .replace("HOST", getHost().trim())
                .replace("PORT", getPort().toString());
    }
}
