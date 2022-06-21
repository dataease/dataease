package io.dataease.provider.datasource;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.provider.Provider;
import com.jayway.jsonpath.JsonPath;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.controller.request.datasource.ApiDefinitionRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;


import java.util.*;

@Service("apiProvider")
public class ApiProvider extends Provider {


    @Override
    public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        ApiDefinition apiDefinition = checkApiDefinition(datasourceRequest);
        String response = execHttpRequest(apiDefinition);
        return fetchResult(response, apiDefinition);
    }

    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tableDescs = new ArrayList<>();
        List<ApiDefinition> lists = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), new TypeToken<List<ApiDefinition>>() {
        }.getType());
        for (ApiDefinition apiDefinition : lists) {
            TableDesc tableDesc = new TableDesc();
            tableDesc.setName(apiDefinition.getName());
            tableDesc.setRemark(apiDefinition.getDesc());
            tableDescs.add(tableDesc);
        }
        return tableDescs;
    }

    @Override
    public List<TableField> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        List<String[]> dataList = new ArrayList<>();
        List<TableField> fieldList = new ArrayList<>();
        ApiDefinition apiDefinition = checkApiDefinition(datasourceRequest);
        String response = execHttpRequest(apiDefinition);

        fieldList = getTableFileds(apiDefinition, response);
        result.put("fieldList", fieldList);
        dataList = fetchResult(response, apiDefinition);
        result.put("dataList", dataList);
        return result;
    }


    private List<TableField> getTableFileds(ApiDefinition apiDefinition, String response) throws Exception {
        List<TableField> tableFields = new ArrayList<>();
        for (DatasetTableField field : checkApiDefinition(apiDefinition, response).getFields()) {
            TableField tableField = new TableField();
            tableField.setFieldName(field.getOriginName());
            tableField.setRemarks(field.getName());
            tableField.setFieldSize(field.getSize());
            tableField.setFieldType(field.getDeExtractType().toString());
            tableFields.add(tableField);
        }
        return tableFields;
    }

    public List<TableField> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        List<ApiDefinition> lists = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), new TypeToken<List<ApiDefinition>>() {
        }.getType());
        List<TableField> tableFields = new ArrayList<>();
        for (ApiDefinition apiDefinition : lists) {
            if (datasourceRequest.getTable().equalsIgnoreCase(apiDefinition.getName())) {
                String response = ApiProvider.execHttpRequest(apiDefinition);
                for (DatasetTableField field : checkApiDefinition(apiDefinition, response).getFields()) {
                    TableField tableField = new TableField();
                    tableField.setFieldName(field.getOriginName());
                    tableField.setRemarks(field.getName());
                    tableField.setFieldSize(field.getSize());
                    tableField.setFieldType(field.getDeExtractType().toString());
                    tableFields.add(tableField);
                }
            }
        }
        return tableFields;
    }

    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        Gson gson = new Gson();
        List<ApiDefinition> apiDefinitionList = gson.fromJson(datasourceRequest.getDatasource().getConfiguration(), new TypeToken<List<ApiDefinition>>() {
        }.getType());
        JsonObject apiItemStatuses = new JsonObject();
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            datasourceRequest.setTable(apiDefinition.getName());
            try {
                getData(datasourceRequest);
                apiItemStatuses.addProperty(apiDefinition.getName(), "Success");
            } catch (Exception ignore) {
                apiItemStatuses.addProperty(apiDefinition.getName(), "Error");
            }
        }
        return gson.toJson(apiItemStatuses);
    }

    static public String execHttpRequest(ApiDefinition apiDefinition) throws Exception {
        String response = "";
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        ApiDefinitionRequest apiDefinitionRequest = apiDefinition.getRequest();
        for (Map header : apiDefinitionRequest.getHeaders()) {
            if (header.get("name") != null && StringUtils.isNotEmpty(header.get("name").toString()) && header.get("value") != null && StringUtils.isNotEmpty(header.get("value").toString())) {
                httpClientConfig.addHeader(header.get("name").toString(), header.get("value").toString());
            }
        }

        if (apiDefinitionRequest.getAuthManager() != null
                && StringUtils.isNotBlank(apiDefinitionRequest.getAuthManager().getUsername())
                && StringUtils.isNotBlank(apiDefinitionRequest.getAuthManager().getPassword())
                && apiDefinitionRequest.getAuthManager().getVerification().equals("Basic Auth")) {
            String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((apiDefinitionRequest.getAuthManager().getUsername()
                    + ":" + apiDefinitionRequest.getAuthManager().getPassword()).getBytes());
            httpClientConfig.addHeader("Authorization", authValue);
        }

        switch (apiDefinition.getMethod()) {
            case "GET":
                response = HttpClientUtil.get(apiDefinition.getUrl(), httpClientConfig);
                break;
            case "POST":
                if (apiDefinitionRequest.getBody().get("type") == null) {
                    throw new Exception("请求类型不能为空");
                }
                String type = apiDefinitionRequest.getBody().get("type").toString();
                if (StringUtils.equalsAny(type, "JSON", "XML", "Raw")) {
                    String raw = null;
                    if (apiDefinitionRequest.getBody().get("raw") != null) {
                        raw = apiDefinitionRequest.getBody().get("raw").toString();
                        response = HttpClientUtil.post(apiDefinition.getUrl(), raw, httpClientConfig);
                    }
                }
                if (StringUtils.equalsAny(type, "Form_Data", "WWW_FORM")) {
                    if (apiDefinitionRequest.getBody().get("kvs") != null) {
                        Map<String, String> body = new HashMap<>();
                        JsonArray kvsArr = JsonParser.parseString(apiDefinitionRequest.getBody().get("kvs").toString()).getAsJsonArray();
                        for (int i = 0; i < kvsArr.size(); i++) {
                            JsonObject kv = kvsArr.get(i).getAsJsonObject();
                            if (kv.get("name") != null) {
                                body.put(kv.get("name").getAsString(), kv.get("value").getAsString());
                            }
                        }
                        response = HttpClientUtil.post(apiDefinition.getUrl(), body, httpClientConfig);
                    }
                }
                break;
            default:
                break;
        }
        return response;
    }

    static public ApiDefinition checkApiDefinition(ApiDefinition apiDefinition, String response) throws Exception {
        if (StringUtils.isEmpty(response)) {
            throw new Exception("该请求返回数据为空");
        }
        List<LinkedHashMap> datas = new ArrayList<>();
        try {
            Object object = JsonPath.read(response, apiDefinition.getDataPath());
            if (object instanceof List) {
                datas = (List<LinkedHashMap>) object;
            } else {
                datas.add((LinkedHashMap) object);
            }
        } catch (Exception e) {
            throw new Exception("jsonPath 路径错误：" + e.getMessage());
        }

        List<Map<String,String>> dataList = new ArrayList<>();
        List<DatasetTableField> fields = new ArrayList<>();
        Set<String> fieldKeys = new HashSet<>();
        //第一遍获取 field
        for (LinkedHashMap data : datas) {
            Set<String> keys = data.keySet();
            for (String key : keys) {
                if (!fieldKeys.contains(key)) {
                    fieldKeys.add(key);
                    DatasetTableField tableField = new DatasetTableField();
                    tableField.setOriginName(key);
                    tableField.setName(key);
                    tableField.setSize(65535);
                    tableField.setDeExtractType(0);
                    tableField.setDeType(0);
                    tableField.setExtField(0);
                    fields.add(tableField);
                }
            }
        }
        //第二遍获取 data
        for (LinkedHashMap data : datas) {
            Map<String,String> mapData = new HashMap<>();
            for (String key : fieldKeys) {
                mapData.put(key, Optional.ofNullable(data.get(key)).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " "));
            }
            dataList.add(mapData);
        }
        apiDefinition.setDatas(dataList);
        apiDefinition.setFields(fields);
        return apiDefinition;
    }

    private List<String[]> fetchResult(String result, ApiDefinition apiDefinition) {
        List<String[]> dataList = new LinkedList<>();
        List<LinkedHashMap> datas = new ArrayList<>();

        Object object = JsonPath.read(result, apiDefinition.getDataPath());
        if (object instanceof List) {
            datas = (List<LinkedHashMap>) object;
        } else {
            datas.add((LinkedHashMap) object);
        }
        for (LinkedHashMap data : datas) {
            String[] row = new String[apiDefinition.getFields().size()];
            int i = 0;
            for (DatasetTableField field : apiDefinition.getFields()) {
                row[i] = Optional.ofNullable(data.get(field.getOriginName())).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " ");
                i++;
            }
            dataList.add(row);
        }
        return dataList;
    }

    private ApiDefinition checkApiDefinition(DatasourceRequest datasourceRequest) throws Exception {
        List<ApiDefinition> apiDefinitionList = new ArrayList<>();
        List<ApiDefinition> apiDefinitionListTemp = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), new TypeToken<List<ApiDefinition>>() {}.getType());
        if (CollectionUtils.isNotEmpty(apiDefinitionListTemp)) {
            for (ApiDefinition apiDefinition : apiDefinitionListTemp) {
                if (apiDefinition.getName().equalsIgnoreCase(datasourceRequest.getTable())) {
                    apiDefinitionList.add(apiDefinition);
                }

            }
        }
        if (CollectionUtils.isEmpty(apiDefinitionList)) {
            throw new Exception("未找到API数据表");
        }
        if (apiDefinitionList.size() > 1) {
            throw new Exception("存在重名的API数据表");
        }
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            if (apiDefinition.getName().equalsIgnoreCase(datasourceRequest.getTable())) {
                return apiDefinition;
            }
        }
        throw new Exception("未找到API数据表");
    }

}
