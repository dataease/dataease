package io.dataease.extensions.sync.model.datasource;

import com.jcraft.jsch.Session;
import io.dataease.extensions.sync.provider.SyncProvider;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;
import lombok.Data;

import java.sql.Connection;

/**
 * 封装并统一管理与数据源相关的可关闭资源
 * 数据库连接、可选的 SSH 会话、本地端口映射和原始配置，提供统一的关闭逻辑
 */
@Data
public class ConnectionObj implements AutoCloseable {


    private Connection connection;
    private Session session;
    private Integer lPort;
    private DatasourceConfiguration configuration;

    @Override
    public void close() throws Exception {
        if (this.connection != null) {
            this.connection.close();
        }

        if (session != null) {
            session.disconnect();
        }

        if (lPort != null) {
            SyncProvider.getLOCAL_PORTS().remove(Long.valueOf(lPort));
        }

    }
}
