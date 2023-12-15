package io.dataease.api.sync.datasource.vo.model.config;

import io.dataease.api.sync.datasource.vo.model.SyncDatasourceConfiguration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("syncOracle")
public class Oracle extends SyncDatasourceConfiguration {
    private String driver = "oracle.jdbc.driver.OracleDriver";
    private String extraParams = "";

    public String getJdbc() {
        if (StringUtils.isNotEmpty(getConnectionType()) && getConnectionType().equalsIgnoreCase("serviceName")) {
            return "jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }else {
            return "jdbc:oracle:thin:@HOSTNAME:PORT:DATABASE"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }
    }
}
