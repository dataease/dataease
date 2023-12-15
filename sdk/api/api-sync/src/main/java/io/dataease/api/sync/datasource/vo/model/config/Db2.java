package io.dataease.api.sync.datasource.vo.model.config;

import io.dataease.api.sync.datasource.vo.model.SyncDatasourceConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("syncDB2")
public class Db2 extends SyncDatasourceConfiguration {
    private String driver = "com.ibm.db2.jcc.DB2Driver";
    private String extraParams = "";

    public String getJdbc() {
        if (StringUtils.isEmpty(extraParams.trim())) {
            if (StringUtils.isEmpty(getSchema())) {
                return "jdbc:db2://HOSTNAME:PORT/DATABASE"
                        .replace("HOSTNAME", getHost().trim())
                        .replace("PORT", getPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim());
            } else {
                return "jdbc:db2://HOSTNAME:PORT/DATABASE:currentSchema=SCHEMA;"
                        .replace("HOSTNAME", getHost().trim())
                        .replace("PORT", getPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim())
                        .replace("SCHEMA", getSchema().trim());
            }
        } else {
            return "jdbc:db2://HOSTNAME:PORT/DATABASE:EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
    }
}
