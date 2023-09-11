package io.dataease.plugins.common.dto.datasource;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Data;

@Data
public class DeSortField extends DatasetTableField {

    private String orderDirection;
}
