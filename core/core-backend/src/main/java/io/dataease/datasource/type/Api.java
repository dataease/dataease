package io.dataease.datasource.type;

import io.dataease.api.ds.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("api")
public class Api extends DatasourceConfiguration {
    private String type = DatasourceType.API.getType();
    private String name = DatasourceType.API.getName();
    private DatasourceCatalog catalog = DatasourceCatalog.API;
    private String catalogDesc = DatasourceCatalog.API.getDesc();
}
