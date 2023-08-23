package io.dataease.plugins.xpack.auth.dto.response;

import io.dataease.plugins.common.annotation.PluginResultMap;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@PluginResultMap
public class XpackSysAuthDetail implements Serializable {
    @ApiModelProperty("Id")
    private String id;
    @ApiModelProperty("授权ID")
    private String authId;
    @ApiModelProperty("名称")
    private String privilegeName;
    @ApiModelProperty("类型")
    private Integer privilegeType;
    @ApiModelProperty("值")
    private Integer privilegeValue;
    @ApiModelProperty("扩展")
    private String privilegeExtend;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("创建人")
    private String createUser;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;

}
