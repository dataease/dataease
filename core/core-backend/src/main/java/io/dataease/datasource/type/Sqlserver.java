package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Data
@Component("sqlServer")
public class Sqlserver extends DatasourceConfiguration {
    private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList("autoDeserialize", "queryInterceptors", "statementInterceptors", "detectCustomCollations");
    private List<String> showTableSqls = Arrays.asList("show tables");

    public String getJdbc() {
        if(StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")){
            if (!getJdbcUrl().startsWith("jdbc:sqlserver")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            return getJdbcUrl();
        }
        String jdbcUrl = "";
        if (StringUtils.isEmpty(extraParams.trim())) {
            jdbcUrl =  "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            jdbcUrl =  "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE;EXTRA_PARAMS"
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

    private static final Pattern DB_NAME_PATTERN = Pattern.compile(";databaseName=([^;]+)");

    @Override
    protected Pattern getDatabasePattern() {
        return DB_NAME_PATTERN;
    }
}
