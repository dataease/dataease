package io.dataease.plugins.xpack.auth.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DataSetColumnPermissionsDTO extends DatasetColumnPermissions{
    private String datasetName;
    private String authTargetName;
    private List<Long> authTargetIds;

    @io.swagger.annotations.ApiModelProperty("白名单-用户（解析自whiteListUser参数）")
    private java.util.List<io.dataease.plugins.common.base.domain.SysUser> whiteListUsers;
}
