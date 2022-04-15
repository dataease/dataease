package io.dataease.provider.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MaxcomputeConfig extends JdbcConfiguration {

    private String driver = "com.aliyun.odps.jdbc.OdpsDriver";
    private String url;
}
