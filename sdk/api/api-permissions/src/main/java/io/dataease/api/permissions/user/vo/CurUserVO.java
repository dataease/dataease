package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CurUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1190164294672439979L;
    @JsonSerialize(using= ToStringSerializer.class)
    @ApiModelProperty("用户ID")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("组织ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long oid;
    @ApiModelProperty("语言")
    private String language;
}
