package io.dataease.api.xpack.appearance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "外观设置参数项")
@Data
public class AppearanceItemVO implements Serializable {

    @Schema(description = "键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pkey;
    @Schema(description = "值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pval;
    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;
}
