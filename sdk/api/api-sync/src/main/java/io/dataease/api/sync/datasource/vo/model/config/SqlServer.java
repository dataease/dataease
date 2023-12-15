package io.dataease.api.sync.datasource.vo.model.config;

import io.dataease.api.sync.datasource.vo.model.SyncDatasourceConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Component("syncSqlServer")
public class SqlServer extends SyncDatasourceConfiguration {
    private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList("autoDeserialize", "queryInterceptors", "statementInterceptors", "detectCustomCollations");
    private List<String> showTableSqls = Arrays.asList("show tables");

    public String getJdbc() {
        if (StringUtils.isEmpty(extraParams.trim())) {
            return "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            return "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE;EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
    }
}
