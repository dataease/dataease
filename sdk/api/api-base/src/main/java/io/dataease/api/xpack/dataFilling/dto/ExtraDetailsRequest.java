package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.datafilling.dto.ExtraColumnItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ExtraDetailsRequest {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long optionDatasource;
    private String optionTable;
    private String optionColumn;
    private List<ExtraColumnItem> extraColumns;
    private String value;

}
