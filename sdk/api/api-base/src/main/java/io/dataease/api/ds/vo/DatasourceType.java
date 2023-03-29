package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;

@Data
public class DatasourceType {
    public String type;
    public String name;
    public boolean isPlugin = true;
    public String extraParams;
    public List<String> charset;
    public List<String> targetCharset;
    public boolean isJdbc = false;
    private String keywordPrefix = "";
    private String keywordSuffix = "";
    private String aliasPrefix = "";
    private String aliasSuffix = "";

    public DatasourceType(String type, String name, boolean isPlugin, String extraParams, boolean isJdbc) {
        this.type = type;
        this.name = name;
        this.isPlugin = isPlugin;
        this.extraParams = extraParams;
        this.isJdbc = isJdbc;
    }
}
