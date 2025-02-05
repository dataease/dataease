package io.dataease.dto.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class MysqlConfiguration extends JdbcConfiguration {

    private String driver = "com.mysql.jdbc.Driver";
    private String extraParams = "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull";
    private List<String> illegalParameters = Arrays.asList("autoDeserialize", "queryInterceptors", "statementInterceptors", "detectCustomCollations", "allowloadlocalinfile", "allowUrlInLocalInfile", "allowLoadLocalInfileInPath");

    public String getJdbc() {
        String jdbcUrl = "";
        if (StringUtils.isEmpty(extraParams.trim())) {
            jdbcUrl = "jdbc:mysql://HOSTNAME:PORT/DATABASE".replace("HOSTNAME", getHost().trim()).replace("PORT", getPort().toString().trim()).replace("DATABASE", getDataBase().trim());
        } else {
            jdbcUrl = "jdbc:mysql://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS".replace("HOSTNAME", getHost().trim()).replace("PORT", getPort().toString().trim()).replace("DATABASE", getDataBase().trim()).replace("EXTRA_PARAMS", getExtraParams().trim());
        }
        for (String illegalParameter : getIllegalParameters()) {
            if (jdbcUrl.toLowerCase().contains(illegalParameter.toLowerCase()) || URLDecoder.decode(jdbcUrl).contains(illegalParameter.toLowerCase())) {
                throw new RuntimeException("Illegal parameter: " + illegalParameter);
            }
        }
        return jdbcUrl;
    }

    public List<String> getIllegalParameters() {
        List<String> newIllegalParameters = new ArrayList<>();
        newIllegalParameters.addAll(illegalParameters);
        newIllegalParameters.addAll(Arrays.asList("maxAllowedPacket", "allowloadlocalinfile", "allowUrlInLocalInfile", "allowLoadLocalInfileInPath"));
        return newIllegalParameters;
    }

}
