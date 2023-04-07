package io.dataease.dataset.dto;

import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class DatasourceSchemaDTO extends CoreDatasource {
    private String schemaAlias;
}
