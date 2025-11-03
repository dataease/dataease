package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Component("oracle")
public class Oracle extends DatasourceConfiguration {
    private String driver = "oracle.jdbc.driver.OracleDriver";
    private String extraParams = "";
    private List<String> getOracleIllegalParameters() {
        return Arrays.asList(
                // 原有参数（如RMI相关）
                "java.naming.factory.initial", "java.naming.provider.url", "rmi",
                // 新增：LDAP协议及相关危险参数
                "ldap://", "ldaps://", "java.naming.factory.object", "java.naming.factory.state",
                // 其他JDBC危险参数
                "autoDeserialize", "connectionProperties", "initSQL", "dns", "file", "ftp"
        );
    }


    public String getJdbc() {
        if(StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")){
            if (!getJdbcUrl().startsWith("jdbc:oracle")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            for (String illegalParameter : getOracleIllegalParameters()) {
                if (getJdbcUrl().toLowerCase().contains(illegalParameter.toLowerCase())) {
                    DEException.throwException("Illegal jdbcUrl: " + illegalParameter);
                }
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
