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
@Component("impala")
public class Impala extends DatasourceConfiguration {
    private String driver = "com.cloudera.impala.jdbc.Driver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList(
            // 原有非法参数
            "autoDeserialize", "queryInterceptors", "statementInterceptors", "detectCustomCollations",
            // 新增：Kerberos认证相关危险参数（漏洞利用核心参数）
            "krbJAASFile", "KrbJAASFile", "krb5.conf", "Krb5Conf",
            // 新增：JDNI/反序列化相关危险参数
            "jndi", "JNDI", "java.naming.factory.initial", "java.naming.provider.url",
            // 新增：其他JDBC危险参数
            "connectionProperties", "ConnectionProperties", "initSQL", "InitSQL"
    );
    private List<String> showTableSqls = Arrays.asList("show tables");

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            for (String illegalParameter : illegalParameters) {
                if (URLDecoder.decode(getJdbcUrl()).toLowerCase().contains(illegalParameter.toLowerCase()) || URLDecoder.decode(getExtraParams()).contains(illegalParameter.toLowerCase())) {
                    DEException.throwException("Illegal parameter: " + illegalParameter);
                }
            }

            if (!getJdbcUrl().startsWith("jdbc:impala")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            return getJdbcUrl();
        }
        String jdbcUrl = "";
        if (StringUtils.isEmpty(extraParams.trim())) {
            jdbcUrl = "jdbc:impala://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            jdbcUrl = "jdbc:impala://HOSTNAME:PORT/DATABASE;EXTRA_PARAMS"
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
