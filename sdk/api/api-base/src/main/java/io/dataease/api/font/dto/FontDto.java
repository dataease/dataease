package io.dataease.api.font.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FontDto {
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 字体名称
     */
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件转换名称
     */
    private String fileTransName;

    /**
     * 是否默认
     */
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isDefault;
    private Long updateTime;
    private Boolean isBuiltin;
    private Double size;
    private String sizeType;
}

