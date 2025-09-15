package io.dataease.datasource.type;

import io.dataease.exception.DEException;
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
        if(StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")){
            if (!getJdbcUrl().startsWith("jdbc:oracle")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            return getJdbcUrl();
        }
        if (StringUtils.isNotEmpty(getConnectionType()) && getConnectionType().equalsIgnoreCase("serviceName")) {
            return "jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }else {
            return "jdbc:oracle:thin:@HOSTNAME:PORT:DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }
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
