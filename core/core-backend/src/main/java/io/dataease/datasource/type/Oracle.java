package io.dataease.datasource.type;

import io.dataease.api.ds.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("oracle")
public class Oracle extends DatasourceConfiguration {
    private String type = DatasourceType.oracle.getType();
    private String name = DatasourceType.oracle.getName();
    private String catalog = DatasourceCatalog.OLAP.getType();
    private String catalogDesc = DatasourceCatalog.OLAP.getDesc();
    private String driver = "oracle.jdbc.driver.OracleDriver";
    private String extraParams = "";

    public String getJdbc() {
        if(getConnectionType().equalsIgnoreCase("serviceName")){
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
