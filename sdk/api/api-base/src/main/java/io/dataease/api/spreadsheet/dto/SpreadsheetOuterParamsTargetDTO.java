package io.dataease.api.spreadsheet.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpreadsheetOuterParamsTargetDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    @NotBlank
    @Size(max = 16)
    private String targetType;

    // query 引用查询条件 ID，field 引用明细表或透视表实例 ID。
    @NotBlank
    @Size(max = 255)
    private String targetRefId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetFieldId;

    @Size(max = 16)
    private String matchMode;
}
