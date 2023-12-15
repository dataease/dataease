package io.dataease.api.sync.datasource.vo.model;

import lombok.Getter;

/**
 * @author fit2cloud
 */

@Getter
public enum DatasourceCalculationMode {
    DIRECT("DIRECT"),
    SYNC("SYNC"),
    DIRECT_AND_SYNC("DIRECT_AND_SYNC");

    private final String  type;
    DatasourceCalculationMode(String type){
        this.type = type;
    }
}
