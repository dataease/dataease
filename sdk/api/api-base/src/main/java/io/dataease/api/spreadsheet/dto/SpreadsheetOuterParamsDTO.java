package io.dataease.api.spreadsheet.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SpreadsheetOuterParamsDTO {
    @NotNull
    @JsonSerialize(using = ToStringSerializer.class)
    private Long spreadsheetId;

    @NotNull
    private Boolean checked;

    @Valid
    @NotNull
    private List<SpreadsheetOuterParamsInfoDTO> outerParamsInfoArray;
}
