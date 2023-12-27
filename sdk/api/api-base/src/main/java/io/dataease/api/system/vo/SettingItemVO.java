package io.dataease.api.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "基础设置参数项")
@Data
@NoArgsConstructor
public class SettingItemVO implements Serializable {
    @Schema(description = "键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pkey;
    @Schema(description = "值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pval;
    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;
}
