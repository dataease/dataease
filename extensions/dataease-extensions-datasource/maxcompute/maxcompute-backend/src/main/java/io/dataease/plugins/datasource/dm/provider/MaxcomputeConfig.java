package io.dataease.plugins.datasource.dm.provider;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MaxcomputeConfig extends JdbcConfiguration {

    private String driver = "com.aliyun.odps.jdbc.OdpsDriver";
    private String projectName;
    private String access_id;
    private String access_key;
    private String end_point;
    private String extraParams;


    public String getJdbc() {
        if(StringUtils.isEmpty(getExtraParams())){
            return "jdbc:odps:END_POINT?project=PROJECT_NAME"
                    .replace("END_POINT", getEnd_point().trim())
                    .replace("PROJECT_NAME", getProjectName().trim());
        }else {
            return "jdbc:odps:END_POINT?project=PROJECT_NAME&EXTRA_PARAMS"
                    .replace("END_POINT", getEnd_point().trim())
                    .replace("PROJECT_NAME", getProjectName().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
    }
}
