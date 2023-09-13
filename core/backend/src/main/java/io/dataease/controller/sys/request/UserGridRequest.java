package io.dataease.controller.sys.request;

import io.dataease.plugins.common.request.KeywordRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserGridRequest extends KeywordRequest {

    @ApiModelProperty("角色ID列表")
    private List<Long> roleIdList;
    @ApiModelProperty("组织ID列表")
    private List<Long> deptIdList;
    @ApiModelProperty("状态")
    private List<Integer> enabledList;

}
