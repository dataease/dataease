package io.dataease.controller.request.datasource;

import com.alibaba.fastjson.JSONObject;
import io.dataease.base.domain.DatasetTableField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiDefinition {
    private String name;
    private String desc;
    private String url;
    private String method = "GET";
    private List<DatasetTableField> fields;
    private String request;
    private String dataPath;
    private List<JSONObject> datas = new ArrayList<>();
}
