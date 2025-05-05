package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

@Data
@Component("pg")
public class Pg extends DatasourceConfiguration {
    private String driver = "org.postgresql.Driver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList("socketFactory", "socketFactoryArg");

    public String getJdbc() {
        if(StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")){
            for (String illegalParameter : illegalParameters) {
                if (URLDecoder.decode(getJdbcUrl()).contains(illegalParameter)) {
                    DEException.throwException("Illegal parameter: " + illegalParameter);
                }
            }
            return getJdbcUrl();
        }
        String jdbcUrl = "";
        if(StringUtils.isEmpty(extraParams.trim())){
            if (StringUtils.isEmpty(getSchema())) {
                jdbcUrl =  "jdbc:postgresql://HOSTNAME:PORT/DATABASE"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim());
            } else {
                jdbcUrl = "jdbc:postgresql://HOSTNAME:PORT/DATABASE?currentSchema=SCHEMA"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim())
                        .replace("SCHEMA", getSchema().trim());
            }
        }else {
            jdbcUrl = "jdbc:postgresql://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());

        }
        for (String illegalParameter : illegalParameters) {
            if (URLDecoder.decode(jdbcUrl).toLowerCase().contains(illegalParameter.toLowerCase()) || URLDecoder.decode(jdbcUrl).contains(illegalParameter.toLowerCase())) {
                DEException.throwException("Illegal parameter: " + illegalParameter);
            }
        }
        return jdbcUrl;
    }
}
