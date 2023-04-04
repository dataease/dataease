package io.dataease.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class KeywordRequest implements Serializable {

    @ApiModelProperty("关键字")
    private String keyword;
}
