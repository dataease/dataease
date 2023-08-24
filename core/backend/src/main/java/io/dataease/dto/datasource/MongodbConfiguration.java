package io.dataease.dto.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MongodbConfiguration extends JdbcConfiguration {

    private String driver = "mongodb.jdbc.MongoDriver";
    private String connectionType;
    private String extraParams = "rebuildschema=true";

    public String getJdbc(String dsId) {
        if(StringUtils.isEmpty(extraParams.trim()) && StringUtils.isEmpty(dsId)){
            return "jdbc:mongodb://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }else {
            this.extraParams = StringUtils.isEmpty(dsId) ? getExtraParams().trim() : getExtraParams().trim() + "&schema=" + dsId +".xml";
            return "jdbc:mongodb://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }

    }
}
