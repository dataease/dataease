package io.dataease.plugins.common.constants;

public enum DatasourceCalculationMode {
    DIRECT("DIRECT"),
    SYNC("SYNC"),
    DIRECT_AND_SYNC("DIRECT_AND_SYNC");

    private String  type;
    DatasourceCalculationMode(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
}
