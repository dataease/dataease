package io.dataease.api.xpack.settings.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Schema(description = "状态VO")
@Data
public class XpackAuthenticationStatusVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3394065091528285702L;

    @Schema(description = "名称")
    private String name;
    @Schema(description = "状态")
    private boolean enable;
}
