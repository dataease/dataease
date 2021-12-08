package io.dataease.mobile.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel("首页数据实体")
public class HomeItemDTO implements Serializable {

    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("时间")
    private Long time;
}
