package io.dataease.dto;

import com.google.gson.JsonArray;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.constants.DatasourceCalculationMode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-05-18
 * Description:
 */
@Data
public class DatasourceDTO extends Datasource {

    @ApiModelProperty("权限")
    private String privileges;
    private JsonArray apiConfiguration;
    private String typeDesc;
    private DatasourceCalculationMode calculationMode;
}
