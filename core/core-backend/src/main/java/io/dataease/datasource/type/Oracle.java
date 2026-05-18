package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Component("oracle")
public class Oracle extends DatasourceConfiguration {
    private String driver = "oracle.jdbc.driver.OracleDriver";
    private String extraParams = "";

    public String getJdbc() {
        String jdbcUrl;
        if(StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")){
            jdbcUrl = getJdbcUrl();
        } else if (StringUtils.isNotEmpty(getConnectionType()) && getConnectionType().equalsIgnoreCase("serviceName")) {
            jdbcUrl = "jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            jdbcUrl = "jdbc:oracle:thin:@HOSTNAME:PORT:DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }
        return JdbcUrlSecurityPolicy.validate("oracle", getDriver(), jdbcUrl, getExtraParams());
    }

    private static final Pattern SERVICE_PATTERN = Pattern.compile(":@//[^/]+/([^?]+)");
    private static final Pattern SID_PATTERN = Pattern.compile(":@[^:]+:(\\d+):([^?]+)");
    @Override
    protected void convertDatabase(String jdbcUrl) {
        Matcher serviceMatcher = SERVICE_PATTERN.matcher(jdbcUrl);
        if (serviceMatcher.find()) {
            setDataBase(serviceMatcher.group(1));
        } else {
            Matcher sidMatcher = SID_PATTERN.matcher(jdbcUrl);
            if (sidMatcher.find()) {
                setDataBase(sidMatcher.group(2));
            }
        }
    }
}
