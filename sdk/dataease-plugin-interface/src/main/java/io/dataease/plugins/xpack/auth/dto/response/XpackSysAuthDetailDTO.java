package io.dataease.plugins.xpack.auth.dto.response;

import io.dataease.plugins.common.annotation.PluginResultMap;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-06-03
 * Description:
 */
@Data
@PluginResultMap
public class XpackSysAuthDetailDTO extends  XpackSysAuthDetail{
    @ApiModelProperty("授权源")
    private String authSource;
    @ApiModelProperty("源类型")
    private String authSourceType;
    @ApiModelProperty("授权目标")
    private String authTarget;
    @ApiModelProperty("目标类型")
    private String authTargetType;
    @ApiModelProperty("权限值")
    private Integer extPrivilegeValue;
    @ApiModelProperty("目标名称")
    private String extTargetNames;
}
