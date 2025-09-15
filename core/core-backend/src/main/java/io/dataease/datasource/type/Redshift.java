package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.awt.SystemColor.info;

@Data
@Component("redshift")
public class Redshift extends DatasourceConfiguration {
    private String driver = "com.amazon.redshift.jdbc42.Driver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList("socketFactory", "socketFactoryArg", "sslfactory", "sslhostnameverifier", "sslpasswordcallback", "authenticationPluginClassName");

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            for (String illegalParameter : illegalParameters) {
                if (URLDecoder.decode(getJdbcUrl()).contains(illegalParameter)) {
                    DEException.throwException("Illegal parameter: " + illegalParameter);
                }
            }
            if (!getJdbcUrl().startsWith("jdbc:redshift")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            return getJdbcUrl();
        }
        String jdbcUrl = "jdbc:redshift://HOSTNAME:PORT/DATABASE"
                .replace("HOSTNAME", getLHost().trim())
                .replace("PORT", getLPort().toString().trim())
                .replace("DATABASE", getDataBase().trim());
        for (String illegalParameter : illegalParameters) {
            if (URLDecoder.decode(jdbcUrl).contains(illegalParameter)) {
                DEException.throwException("Illegal parameter: " + illegalParameter);
            }
        }
        return jdbcUrl;
    }

    private static final Pattern DB_NAME_PATTERN = Pattern.compile("//[^/]+/([^?]+)");

    @Override
    protected Pattern getDatabasePattern() {
        return DB_NAME_PATTERN;
    }

    @Override
    protected void convertParameters() {
        Map<String, String> parameters = getParameters();
        if (parameters.containsKey("UID")) {
            setUsername(parameters.get("UID"));
        }
        if (parameters.containsKey("PWD")) {
            setPassword(parameters.get("PWD"));
        }
    }
}
