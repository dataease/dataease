package io.dataease.plugins.common.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class KeywordRequest implements Serializable {
    @ApiModelProperty("关键字")
    private String keyword;

    @ApiModelProperty("排序描述")
    private List<String> orders;
}
