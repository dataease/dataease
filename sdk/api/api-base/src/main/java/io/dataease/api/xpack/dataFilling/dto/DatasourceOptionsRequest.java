package io.dataease.api.xpack.dataFilling.dto;

import lombok.Getter;

@Getter
public class DatasourceOptionsRequest {

    private String optionTable;
    private String optionColumn;
    private String optionOrder;
}
