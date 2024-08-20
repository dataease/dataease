package io.dataease.dto.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * 连接配置信息
 */
@Getter
@Setter
public class IoTDBConfiguration extends JdbcConfiguration {

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
