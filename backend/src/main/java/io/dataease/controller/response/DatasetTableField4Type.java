package io.dataease.controller.response;

import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DatasetTableField4Type {
    @ApiModelProperty("维度")
    List<DatasetTableFieldDTO> dimensionList;
    @ApiModelProperty("指标")
    List<DatasetTableFieldDTO> quotaList;
}
