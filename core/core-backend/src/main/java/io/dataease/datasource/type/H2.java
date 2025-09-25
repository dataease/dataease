package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Component("h2")
public class H2 extends DatasourceConfiguration {
    private String driver = "org.h2.Driver";

    public String getJdbc() {
        for (String illegalParameter : getH2IllegalParameters()) {
            if (jdbc.toUpperCase().replace("\\", "").contains(illegalParameter)) {
                DEException.throwException("Has illegal parameter: " + jdbc);
            }
        }
        if (StringUtils.isNotEmpty(jdbc) && !jdbc.startsWith("jdbc:h2")) {
            DEException.throwException("Illegal jdbcUrl: " + jdbc);
        }
        return jdbc;
    }

    private List<String> getH2IllegalParameters() {
        return Arrays.asList("INIT", "RUNSCRIPT");
    }

}
