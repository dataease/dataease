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
    private List<String> illegalParameters = Arrays.asList("INIT", "RUNSCRIPT");

    public String getJdbc() {
        for (String illegalParameter : illegalParameters) {
            if (jdbc.toUpperCase().contains(illegalParameter)) {
                DEException.throwException("Has illegal parameter: " + jdbc);
            }
        }

        return jdbc;
    }
}
