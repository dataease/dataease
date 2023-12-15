package io.dataease.api.sync.datasource.vo.model.config;

import io.dataease.api.sync.datasource.vo.model.SyncDatasourceConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author fit2cloud
 */
@Getter
@Setter
@Component("syncDoris")
public class Doris extends SyncDatasourceConfiguration {
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String extraParams = "";

    public String getJdbc() {

        if(StringUtils.isEmpty(extraParams.trim())){
            return "jdbc:mysql://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }else {
            return "jdbc:mysql://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
    }
    public String getSeaTunnelSink(String configTemplateContent,String tableName) {
        return configTemplateContent
                .replace("${sink.db.username}", getUsername())
                .replace("${sink.db.password}", getPassword())
                .replace("${sink.db.host}", getHost())
                //.replace("${sink.db.port}", String.valueOf(dorisConfiguration.getPort()))
                .replace("${sink.db.identifier}", getDataBase() + "." + tableName);
    }
}
