package io.dataease.provider.datasource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.controller.request.datasource.ApiDefinitionRequest;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.dto.datasource.TableDesc;
import io.dataease.dto.datasource.TableField;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service("api")
public class ApiProvider extends DatasourceProvider{
    @Override
    public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        ApiDefinition apiDefinition = checkApiDefinition(datasourceRequest);
        String response = execHttpRequest(apiDefinition);
        return fetchResult(response, apiDefinition);
    }

    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tableDescs = new ArrayList<>();
        List<ApiDefinition> lists = JSONObject.parseArray(datasourceRequest.getDatasource().getConfiguration(), ApiDefinition.class);
        for (ApiDefinition apiDefinition : lists) {
            TableDesc tableDesc = new TableDesc();
            tableDesc.setName(apiDefinition.getName());
            tableDesc.setRemark(apiDefinition.getDesc());
            tableDescs.add(tableDesc);
        }
        return tableDescs;
    }

    @Override
    public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    @Override
    public List<TableField> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        List<String[]> dataList = new ArrayList<>();
        List<TableField> fieldList = new ArrayList<>();;
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

    @Override
    public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception {

    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    @Override
    public List<TableField> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        List<ApiDefinition> lists = JSONObject.parseArray(datasourceRequest.getDatasource().getConfiguration(), ApiDefinition.class);
        List<TableField> tableFields = new ArrayList<>();
        for (ApiDefinition apiDefinition : lists) {
            if(datasourceRequest.getTable().equalsIgnoreCase(apiDefinition.getName())){
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

    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        List<ApiDefinition> apiDefinitionList = JSONObject.parseArray(datasourceRequest.getDatasource().getConfiguration(), ApiDefinition.class);
        JSONObject apiItemStatuses = new JSONObject();
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            datasourceRequest.setTable(apiDefinition.getName());
           try {
               getData(datasourceRequest);
               apiItemStatuses.put(apiDefinition.getName(), "Success");
           }catch (Exception ignore){
               apiItemStatuses.put(apiDefinition.getName(), "Error");
           }
        }
        return JSONObject.toJSONString(apiItemStatuses);
    }

    static public String execHttpRequest(ApiDefinition apiDefinition) throws Exception{
        String response = "";
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        ApiDefinitionRequest apiDefinitionRequest = JSONObject.parseObject(apiDefinition.getRequest(), ApiDefinitionRequest.class);
        //headers
        for (JSONObject header : apiDefinitionRequest.getHeaders()) {
            if(StringUtils.isNotEmpty(header.getString("name")) && StringUtils.isNotEmpty(header.getString("value"))){
                httpClientConfig.addHeader(header.getString("name"), header.getString("value"));
            }
        }

        if(apiDefinitionRequest.getAuthManager() != null
                && StringUtils.isNotBlank(apiDefinitionRequest.getAuthManager().getUsername())
                && StringUtils.isNotBlank(apiDefinitionRequest.getAuthManager().getPassword())
                && apiDefinitionRequest.getAuthManager().getVerification().equals("Basic Auth")){
            String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((apiDefinitionRequest.getAuthManager().getUsername()
                    + ":" + apiDefinitionRequest.getAuthManager().getPassword()).getBytes());
            httpClientConfig.addHeader("Authorization", authValue);
        }

        switch (apiDefinition.getMethod()){
            case "GET":
                response = HttpClientUtil.get(apiDefinition.getUrl(), httpClientConfig);
                break;
            case "POST":
                if (!apiDefinitionRequest.getBody().containsKey("type")) {
                   throw new Exception("请求类型不能为空");
                }
                String type = apiDefinitionRequest.getBody().getString("type");
                if (StringUtils.equalsAny(type, "JSON", "XML", "Raw")) {
                    String raw = null;
                    if (apiDefinitionRequest.getBody().containsKey("raw")) {
                        raw = apiDefinitionRequest.getBody().getString("raw");
                        response = HttpClientUtil.post(apiDefinition.getUrl(), raw, httpClientConfig);
                    }
                }
                if (StringUtils.equalsAny(type, "Form_Data", "WWW_FORM")) {
                    if (apiDefinitionRequest.getBody().containsKey("kvs")) {
                        Map<String, String> body = new HashMap<>();
                        JSONArray kvsArr = apiDefinitionRequest.getBody().getJSONArray("kvs");
                        for (int i = 0; i < kvsArr.size(); i++) {
                            JSONObject kv = kvsArr.getJSONObject(i);
                            if (kv.containsKey("name")) {
                                body.put(kv.getString("name"), kv.getString("value"));
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

    static public ApiDefinition checkApiDefinition(ApiDefinition apiDefinition, String response)throws Exception{
        if(StringUtils.isEmpty(response)){
            throw new Exception("该请求返回数据为空");
        }
        List<LinkedHashMap> datas = new ArrayList<>();
        try {
            datas = JsonPath.read(response,apiDefinition.getDataPath());
        }catch (Exception e){
            throw new Exception("jsonPath 路径错误：" + e.getMessage());
        }

        List<JSONObject> dataList = new ArrayList<>();
        List<DatasetTableField> fields = new ArrayList<>();
        Set<String> fieldKeys = new HashSet<>();
        //第一遍获取 field
        for (LinkedHashMap data : datas) {
            Set<String> keys = data.keySet();
            for (String key : keys) {
                if(!fieldKeys.contains(key)){
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
            JSONObject jsonObject = new JSONObject();
            for (String key : fieldKeys) {
                jsonObject.put(key, Optional.ofNullable(data.get(key)).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " "));
            }
            dataList.add(jsonObject);
        }
        apiDefinition.setDatas(dataList);
        apiDefinition.setFields(fields);
        return apiDefinition;
    }

    private List<String[]> fetchResult(String result, ApiDefinition apiDefinition){
        List<String[]> dataList = new LinkedList<>();
        List<LinkedHashMap> datas = JsonPath.read(result, apiDefinition.getDataPath());
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

    private ApiDefinition checkApiDefinition(DatasourceRequest datasourceRequest)throws Exception{
        List<ApiDefinition> apiDefinitionList = JSONObject.parseArray(datasourceRequest.getDatasource().getConfiguration(), ApiDefinition.class).stream().filter(item -> item.getName().equalsIgnoreCase(datasourceRequest.getTable())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(apiDefinitionList)){
            throw new Exception("未找到API数据表");
        }
        if(apiDefinitionList.size() > 1 ){
            throw new Exception("存在重名的API数据表");
        }
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            if (apiDefinition.getName().equalsIgnoreCase(datasourceRequest.getTable())){
                return apiDefinition;
            }
        }
        throw new Exception("未找到API数据表");
    }

}
