package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExtraDetailsBaseRequest {

    private String value;
    private String columnId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;
}
