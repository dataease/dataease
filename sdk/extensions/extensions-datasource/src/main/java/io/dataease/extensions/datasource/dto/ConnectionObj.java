package io.dataease.extensions.datasource.dto;

import com.jcraft.jsch.Session;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;

import java.sql.Connection;

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
            System.out.println("session.disconnect()");
            session.disconnect();
        }

        if(lPort != null){
            Provider.getLPorts().remove(Long.valueOf(lPort));
        }

    }
}
