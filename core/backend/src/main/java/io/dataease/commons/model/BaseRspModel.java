package io.dataease.commons.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
public class BaseRspModel implements Serializable {

    @ApiModelProperty("成功标志")
    private Boolean success = true;

    @ApiModelProperty("请求ID")
    private String requestId;

    @ApiModelProperty("返回业务信息")
    private Object responseInfo;


}
