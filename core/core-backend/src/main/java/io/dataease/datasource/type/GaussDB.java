package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Data
@Component("gaussdb")
public class GaussDB extends Pg {
    private String driver = "com.huawei.gaussdb.jdbc.Driver";
    private String extraParams = "";

    @Override
    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            return JdbcUrlSecurityPolicy.validate("gaussdb", getDriver(), getJdbcUrl(), getExtraParams());
        }
        String jdbcUrl = "";
        if (StringUtils.isEmpty(extraParams.trim())) {
            if (StringUtils.isEmpty(getSchema())) {
                jdbcUrl = "jdbc:gaussdb://HOSTNAME:PORT/DATABASE"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim());
            } else {
                jdbcUrl = "jdbc:gaussdb://HOSTNAME:PORT/DATABASE?currentSchema=\"SCHEMA\""
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim())
                        .replace("SCHEMA", getSchema().trim());
            }
        } else {
            jdbcUrl = "jdbc:gaussdb://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
            if (StringUtils.isNotEmpty(getSchema()) && !getExtraParams().contains("currentSchema")) {
                jdbcUrl += "&currentSchema=\"" + getSchema().trim() + "\"";
            }
        }
        return JdbcUrlSecurityPolicy.validate("gaussdb", getDriver(), jdbcUrl, getExtraParams());
    }
}
