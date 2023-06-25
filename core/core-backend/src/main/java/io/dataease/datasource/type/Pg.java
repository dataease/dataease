package io.dataease.datasource.type;

import io.dataease.api.ds.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Data
@Component("pg")
public class Pg extends DatasourceConfiguration {
    private String driver = "org.postgresql.Driver";
    private String extraParams = "";

    public String getJdbc() {
        if(StringUtils.isEmpty(extraParams.trim())){
            return "jdbc:postgresql://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }else {
            return "jdbc:postgresql://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());

        }
    }
}
