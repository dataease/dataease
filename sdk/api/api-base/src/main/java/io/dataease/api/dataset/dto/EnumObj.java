package io.dataease.api.dataset.dto;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class EnumObj {
    private DatasetTableFieldDTO field;
    private DatasetGroupInfoDTO dataset;
}
