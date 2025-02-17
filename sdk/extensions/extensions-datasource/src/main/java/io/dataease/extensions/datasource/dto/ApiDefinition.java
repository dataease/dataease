package io.dataease.extensions.datasource.dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ApiDefinition {
    private String name;
    private String deTableName;
    private String desc;
    private String url;
    private String method = "GET";
    private List<TableField> fields;
    private List<Map<String, Object>> jsonFields =new ArrayList<>();
    private ApiDefinitionRequest request;
    private String status;
    private List<Map<String, Object>> data = new ArrayList<>();
    private Integer apiQueryTimeout = 10;
    private int previewNum = 100;
    private int serialNumber;
    private boolean useJsonPath;
    private String jsonPath;
    private boolean reName = false;
    private String orgName;
    private boolean showApiStructure;
    private Long updateTime;
    private String type = "table";
    private  String token;
    private  String appToken;
    private  String tableId;
    private  String viewId;
}
