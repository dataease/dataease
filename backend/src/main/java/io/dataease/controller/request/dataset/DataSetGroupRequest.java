package io.dataease.controller.request.dataset;

import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * @Author gin
 * @Date 2021/2/22 1:30 下午
 */
@Data
public class DataSetGroupRequest extends DatasetGroup {
    @ApiModelProperty("排序")
    private String sort;
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("ID集合")
    private Set<String> ids;
    @ApiModelProperty("排除的ID")
    private String excludedId;
}
