package io.dataease.datasource.type;

import io.dataease.api.ds.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("excel")
public class Excel extends DatasourceConfiguration {
    private String type = DatasourceType.Excel.getType();
    private String name = DatasourceType.Excel.getName();
    private DatasourceCatalog catalog = DatasourceCatalog.LOCALFILE;
    private String catalogDesc = DatasourceCatalog.LOCALFILE.getDesc();
}
