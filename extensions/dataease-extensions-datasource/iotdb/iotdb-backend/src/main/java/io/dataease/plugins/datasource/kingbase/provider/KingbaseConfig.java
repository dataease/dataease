package io.dataease.plugins.datasource.kingbase.provider;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * 连接配置信息
 */
@Getter
@Setter
public class KingbaseConfig extends JdbcConfiguration {

    private String driver = "com.kingbase8.Driver";//驱动类名
    private String extraParams;


    /**
     * JDBC 拼接
     */
    public String getJdbc() {
        return "jdbc:kingbase8://HOST:PORT/DATABASE"
                .replace("HOST", getHost().trim())
                .replace("PORT", getPort().toString())
                .replace("DATABASE", getDataBase().trim());
    }
}
