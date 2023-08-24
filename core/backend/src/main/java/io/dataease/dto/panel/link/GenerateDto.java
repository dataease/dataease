package io.dataease.dto.panel.link;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GenerateDto {

    @ApiModelProperty("是否有效")
    private boolean valid;
    @ApiModelProperty("是否启用密码")
    private boolean enablePwd;
    @ApiModelProperty("链接地址")
    private String uri;
    @ApiModelProperty("密码")
    private String pwd;
    @ApiModelProperty("有效期")
    private Long overTime;
}
