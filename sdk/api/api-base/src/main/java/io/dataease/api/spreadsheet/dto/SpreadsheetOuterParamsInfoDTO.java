package io.dataease.api.spreadsheet.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SpreadsheetOuterParamsInfoDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long paramsInfoId;

    @NotBlank
    @Size(max = 255)
    private String paramName;

    @NotNull
    private Boolean checked;

    @NotNull
    private Boolean required;

    @NotNull
    private Boolean enabledDefault;

    // 与仪表板一致，默认值保存为 JSON 数组字符串。
    @Size(max = 16777216)
    private String defaultValue;

    @Valid
    @NotNull
    private List<SpreadsheetOuterParamsTargetDTO> targetList;
}
