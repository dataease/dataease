package io.dataease.datasource.model;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ApiDefinition {
    private String name;
    private String desc;
    private String url;
    private String method = "GET";
    private List<TableField> fields;
    private ApiDefinitionRequest request;
    private String status;
    private List<Map<String, Object>> data = new ArrayList<>();

    private List<Map<String, Object>> jsonFields =new ArrayList<>();
    private int previewNum = 10;
    private int maxPreviewNum = 10;
    private int serialNumber;
    private boolean useJsonPath;
    private String jsonPath;
    private boolean showApiStructure;
    private boolean reName = false;
    private String orgName;

}
