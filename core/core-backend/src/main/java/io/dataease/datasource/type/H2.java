package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = true)
@Data
@Component("h2")
public class H2 extends DatasourceConfiguration {
    private String driver = "org.h2.Driver";

    public String getJdbc() {
        String jdbcUrl = StringUtils.defaultIfBlank(jdbc, getJdbcUrl());
        if (StringUtils.isBlank(jdbcUrl)) {
            DEException.throwException("Illegal jdbcUrl: " + jdbc);
        }
        return JdbcUrlSecurityPolicy.validate("h2", getDriver(), jdbcUrl, getExtraParams());
    }
}
