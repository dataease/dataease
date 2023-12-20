package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "当前登录人信息VO")
@Data
public class CurUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1190164294672439979L;
    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "名称")
    private String name;
    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "组织ID")
    private Long oid;
    @Schema(description = "语言")
    private String language;
}
