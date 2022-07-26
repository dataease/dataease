package io.dataease.controller.request.datasource;

import com.alibaba.fastjson.JSONObject;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
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
    private List<DatasetTableFieldDTO> fields;
    private ApiDefinitionRequest request;
    private String dataPath;
    private String status;
    private List<Map<String,String>> datas = new ArrayList<>();
    private List<JSONObject> jsonFields = new ArrayList<>();
    private int previewNum = 10;
    private int maxPreviewNum = 10;
    private int serialNumber;

}
