package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DatasourceOptionsRequest {

    private String optionTable;
    private String optionColumn;
    private String optionOrder;
}
