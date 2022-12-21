package io.dataease.dto;

import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.constants.DatasourceCalculationMode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-18
 * Description:
 */
@Data
public class DatasourceDTO extends Datasource {

    @ApiModelProperty("权限")
    private String privileges;
    private List<ApiDefinition> apiConfiguration;
    private String apiConfigurationStr;
    private String typeDesc;
    private DatasourceCalculationMode calculationMode;
    private boolean isConfigurationEncryption = false;
}
