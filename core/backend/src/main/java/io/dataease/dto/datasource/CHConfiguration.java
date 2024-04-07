package io.dataease.dto.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class CHConfiguration extends JdbcConfiguration {

    private String driver = "ru.yandex.clickhouse.ClickHouseDriver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList("localFile");

    public String getJdbc() {
        if (StringUtils.isEmpty(extraParams.trim())) {
            return "jdbc:clickhouse://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            for (String illegalParameter : getIllegalParameters()) {
                if (getExtraParams().toLowerCase().contains(illegalParameter.toLowerCase()) ||
                        URLDecoder.decode(getExtraParams()).toLowerCase().contains(illegalParameter.toLowerCase())) {
                    throw new RuntimeException("Illegal parameter: " + illegalParameter);
                }
            }

            return "jdbc:clickhouse://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }

    }
}