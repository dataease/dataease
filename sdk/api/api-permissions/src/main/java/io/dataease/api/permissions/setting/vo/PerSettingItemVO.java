package io.dataease.api.permissions.setting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "设置项VO")
@Data
@NoArgsConstructor
public class PerSettingItemVO implements Serializable {
    @Schema(description = "key")
    private String pkey;
    @Schema(description = "value")
    private String pval;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "顺序")
    private Integer sort;
}
