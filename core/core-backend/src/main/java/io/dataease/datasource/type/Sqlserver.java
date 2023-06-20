package io.dataease.datasource.type;

import io.dataease.api.ds.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("sqlServer")
public class Sqlserver extends DatasourceConfiguration {
    private String type = DatasourceType.sqlServer.getType();
    private String name = DatasourceType.sqlServer.getName();
    private String catalog = DatasourceCatalog.OLAP.getType();
    private String catalogDesc = DatasourceCatalog.OLAP.getDesc();
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
