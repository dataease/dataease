package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("db2")
public class Db2 extends DatasourceConfiguration {
    private String driver = "com.ibm.db2.jcc.DB2Driver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList(
            // 原有参数（如RMI相关）
            "java.naming.factory.initial", "java.naming.provider.url", "rmi",
            // 新增：LDAP协议及相关危险参数
            "ldap://", "ldaps://", "java.naming.factory.object", "java.naming.factory.state",
            // 其他JDBC危险参数
            "autoDeserialize", "connectionProperties", "initSQL", "dns", "file", "ftp", "iiop", "corbaname", "iiopname"
    );

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            for (String illegalParameter : illegalParameters) {
                if (getJdbcUrl().toLowerCase().contains(illegalParameter.toLowerCase())) {
                    DEException.throwException("Illegal parameter: " + illegalParameter);
                }
            }
            if (!getJdbcUrl().startsWith("jdbc:db2")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            return getJdbcUrl();
        }
        String url = "";
        if (StringUtils.isEmpty(extraParams.trim())) {
            if (StringUtils.isEmpty(getSchema())) {
                url = "jdbc:db2://HOSTNAME:PORT/DATABASE"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim());
            } else {
                url = "jdbc:db2://HOSTNAME:PORT/DATABASE:currentSchema=SCHEMA;"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim())
                        .replace("SCHEMA", getSchema().trim());
            }
        } else {
            url = "jdbc:db2://HOSTNAME:PORT/DATABASE:EXTRA_PARAMS"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
        for (String illegalParameter : illegalParameters) {
            if (url.toLowerCase().contains(illegalParameter.toLowerCase())) {
                DEException.throwException("Illegal parameter: " + illegalParameter);
            }
        }
        return url;
    }
}
