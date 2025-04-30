package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = true)
@Data
@Component("h2")
public class H2 extends DatasourceConfiguration {
    private String driver = "org.h2.Driver";

    public String getJdbc() {
        if (StringUtils.containsAnyIgnoreCase(jdbc, "INIT", "RUNSCRIPT")) {
            DEException.throwException("Has illegal parameter: " + jdbc);
        }
        return jdbc;
    }
}
