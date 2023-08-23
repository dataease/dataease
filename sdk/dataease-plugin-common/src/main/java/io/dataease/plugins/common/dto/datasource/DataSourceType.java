package io.dataease.plugins.common.dto.datasource;

import io.dataease.plugins.common.constants.DatabaseClassification;
import io.dataease.plugins.common.constants.DatasourceCalculationMode;
import lombok.Data;

import java.util.List;

@Data
public class DataSourceType {
    public String type;
    public String name;
    public boolean isPlugin = true;
    public DatasourceCalculationMode calculationMode = DatasourceCalculationMode.DIRECT;
    public DatabaseClassification databaseClassification = DatabaseClassification.OTHER;
    public String extraParams;
    public List<String> charset;
    public List<String> targetCharset;
    public boolean isJdbc = false;
    private String keywordPrefix = "";
    private String keywordSuffix = "";
    private String aliasPrefix = "";
    private String aliasSuffix = "";

    public DataSourceType(String type, String name, boolean isPlugin, String extraParams, DatasourceCalculationMode calculationMode, boolean isJdbc) {
        this.type = type;
        this.name = name;
        this.isPlugin = isPlugin;
        this.extraParams = extraParams;
        this.calculationMode = calculationMode;
        this.isJdbc = isJdbc;
    }
}
