package io.dataease.api.ds.vo;


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


    private int previewNum = 10;
    private int maxPreviewNum = 10;
    private int serialNumber;
    private boolean useJsonPath;
    private String jsonPath;
    private boolean reName = false;
    private String orgName;
    private boolean showApiStructure;
    private Long updateTime;
}
