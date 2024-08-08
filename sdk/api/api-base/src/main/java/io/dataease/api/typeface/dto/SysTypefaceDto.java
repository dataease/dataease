package io.dataease.api.typeface.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class SysTypefaceDto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 字体名称
     */
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
    private Boolean isDefault;
}

