package io.dataease.datasource.type;

import io.dataease.api.ds.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Data
@Component("db2")
public class Db2 extends DatasourceConfiguration {
    private String driver = "com.ibm.db2.jcc.DB2Driver";
    private String extraParams = "";

    public String getJdbc() {
        if(StringUtils.isEmpty(extraParams.trim())){
            return "jdbc:db2://HOSTNAME:PORT/DATABASE:currentSchema=SCHEMA;"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("SCHEMA",getSchema().trim());
        }else {
            return "jdbc:db2://HOSTNAME:PORT/DATABASE:EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
    }
}
