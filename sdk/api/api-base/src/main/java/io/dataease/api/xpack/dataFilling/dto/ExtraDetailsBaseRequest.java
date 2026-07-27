package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExtraDetailsBaseRequest {

    private String value;
    private String columnId;
    private String formId;

}
