package io.dataease.api.xpack.settings.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "认证列表VO")
@Data
public class XpackAuthenticationVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1744424881280545811L;

    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "开启")
    private boolean enable;
    @Schema(description = "有效")
    private boolean valid;
}
