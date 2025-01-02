package io.dataease.controller.request.datafill;

import lombok.Getter;

@Getter
public class DatasourceOptionsRequest {

    private String optionTable;
    private String optionColumn;
    private String optionOrder;
    private String query;
}
