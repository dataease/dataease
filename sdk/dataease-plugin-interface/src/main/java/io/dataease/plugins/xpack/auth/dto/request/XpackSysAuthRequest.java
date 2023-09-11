package io.dataease.plugins.xpack.auth.dto.request;

import io.dataease.plugins.xpack.auth.dto.response.XpackSysAuth;
import io.dataease.plugins.xpack.auth.dto.response.XpackSysAuthDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class XpackSysAuthRequest extends XpackSysAuth {

    @ApiModelProperty("授权资源")
    private List<String> authSources;
    @ApiModelProperty("授权目标")
    private List<String> authTargets;
    @ApiModelProperty("授权明细")
    private XpackSysAuthDetail authDetail;

    // 权限查询方向 source:查询对应target 拥有的 source授权；target: 查询对应source 拥有的 target授权
    @ApiModelProperty("授权查询方向")
    private String direction;
}
