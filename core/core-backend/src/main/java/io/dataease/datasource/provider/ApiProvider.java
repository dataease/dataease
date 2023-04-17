package io.dataease.datasource.provider;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.datasource.model.ApiDefinition;
import io.dataease.datasource.model.ApiDefinitionRequest;
import io.dataease.datasource.model.TableField;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component("apiProvider")
public class ApiProvider extends Provider {

    private static String path = "['%s']";
    private ObjectMapper objectMapper = new ObjectMapper();

    private TypeReference<List<String>> listTypeReference = new TypeReference<List<String>>() {};
    private TypeReference<List<Map<String, Object>>> listForMapTypeReference = new TypeReference<List<Map<String, Object>>>() {};

    public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        ApiDefinition apiDefinition = checkApiDefinition(datasourceRequest);
        String response = execHttpRequest(apiDefinition, 10);
        return fetchResult(response, apiDefinition);
    }

    public List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<DatasetTableDTO> tableDescs = new ArrayList<>();
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {};
        List<ApiDefinition> lists = objectMapper.readValue(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);

        for (ApiDefinition apiDefinition : lists) {
            DatasetTableDTO datasetTableDTO = new DatasetTableDTO();
            datasetTableDTO.setTableName(apiDefinition.getName());
            datasetTableDTO.setName(apiDefinition.getDesc());
        }
        return tableDescs;
    }


    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {

        Map<String, List> result = new HashMap<>();
        List<String[]> dataList = new ArrayList<>();
        List<TableField> fieldList = new ArrayList<>();
        ApiDefinition apiDefinition = checkApiDefinition(datasourceRequest);
        String response = execHttpRequest(apiDefinition, 10);

        fieldList = getTableFields(apiDefinition);
        result.put("fieldList", fieldList);
        dataList = fetchResult(response, apiDefinition);
        result.put("dataList", dataList);
        return result;
    }


    private List<TableField> getTableFields(ApiDefinition apiDefinition) throws Exception {
        return apiDefinition.getFields();
    }

    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
        List<ApiDefinition> lists = objectMapper.readValue(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);
        List<TableField> tableFields = new ArrayList<>();
        for (ApiDefinition apiDefinition : lists) {
            if (datasourceRequest.getTable().equalsIgnoreCase(apiDefinition.getName())) {
                tableFields = getTableFields(apiDefinition);
            }
        }
        return tableFields;
    }

    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
        List<ApiDefinition> apiDefinitionList = objectMapper.readValue(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);
        ObjectNode apiItemStatuses = objectMapper.createObjectNode();
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            datasourceRequest.setTable(apiDefinition.getName());
            try {
                getData(datasourceRequest);
                apiItemStatuses.put(apiDefinition.getName(), "Success");
            } catch (Exception ignore) {
                apiItemStatuses.put(apiDefinition.getName(), "Error");
            }
        }
        return apiItemStatuses.asText();
    }

    public String execHttpRequest(ApiDefinition apiDefinition, int socketTimeout) throws Exception {
        String response = "";
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.setSocketTimeout(socketTimeout * 1000);
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
                response = HttpClientUtil.get(apiDefinition.getUrl().trim(), httpClientConfig);
                break;
            case "POST":
                if (!apiDefinitionRequest.getBody().keySet().contains("type")) {
                    throw new Exception("请求类型不能为空");
                }
                String type = apiDefinitionRequest.getBody().get("type");
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
                        JsonNode rootNode = objectMapper.readTree(apiDefinitionRequest.getBody().get("kvs"));
                        for (JsonNode jsonNode : rootNode) {
                            if (jsonNode.has("name")) {
                                body.put(jsonNode.get("name").asText(), jsonNode.get("value").toString());
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


    public ApiDefinition checkApiDefinition(ApiDefinition apiDefinition, String response) throws Exception {
        if (StringUtils.isEmpty(response)) {
            throw new Exception("该请求返回数据为空");
        }
        List<Map<String, Object>> fields = new ArrayList<>();
        if (apiDefinition.isUseJsonPath()) {
            List<LinkedHashMap> currentData = new ArrayList<>();
            Object object = JsonPath.read(response, apiDefinition.getJsonPath());
            if (object instanceof List) {
                currentData = (List<LinkedHashMap>) object;
            } else {
                currentData.add((LinkedHashMap) object);
            }
            int i = 0;
            for (LinkedHashMap data : currentData) {
                if (i >= apiDefinition.getPreviewNum()) {
                    break;
                }
                if (i == 0) {
                    for (Object o : data.keySet()) {
                        Map<String, Object> field = new HashMap<>();
                        field.put("originName", o.toString());
                        field.put("name", o.toString());
                        field.put("type", "STRING");
                        field.put("checked", true);
                        field.put("size", 65535);
                        field.put("deExtractType", 0);
                        field.put("deType", 0);
                        field.put("extField", 0);
                        fields.add(field);
                    }
                }
                for (Map<String, Object> field : fields) {
                    List<Object> array = new ArrayList<>();
                    if (field.get("value") != null) {
                        TypeReference<List<Object>> listTypeReference = new TypeReference<List<Object>>() {
                        };
                        array = objectMapper.readValue(field.get("value").toString(), listTypeReference);
                        array.add(Optional.ofNullable(data.get(field.get("originName"))).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " "));
                    } else {
                        array = new ArrayList();
                        array.add(Optional.ofNullable(data.get(field.get("originName"))).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " "));
                    }
                    field.put("value", array);
                }
                i++;
            }
            apiDefinition.setJsonFields(fields);
            return apiDefinition;
        } else {

            String rootPath;
            if (response.startsWith("[")) {
                rootPath = "$[*]";
                JsonNode jsonArray = objectMapper.readTree(response);
                for (Object o : jsonArray) {
                    handleStr(apiDefinition, o.toString(), fields, rootPath);
                }
            } else {
                rootPath = "$";
                handleStr(apiDefinition, response, fields, rootPath);
            }
            for (Map<String, Object> field : fields) {
                if (field.containsKey("children")) {
                    field.put("disabled", false);
                }
                if (field.containsKey("children")) {
                    field.put("disabled", true);
                }
            }
            apiDefinition.setJsonFields(fields);
            return apiDefinition;
        }
    }


    private void handleStr(ApiDefinition apiDefinition, String jsonStr, List<Map<String, Object>> fields, String rootPath) throws Exception {
        if (jsonStr.startsWith("[")) {
            TypeReference<List<Object>> listTypeReference = new TypeReference<List<Object>>() {
            };
            List<Object> jsonArray = objectMapper.readValue(jsonStr, listTypeReference);
            for (Object o : jsonArray) {
                handleStr(apiDefinition, o.toString(), fields, rootPath);
            }
        } else {
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String value = jsonNode.get(fieldName).toString();
                if (StringUtils.isNotEmpty(value) && value.startsWith("[")) {
                    Map<String, Object> o = new HashMap<>();
                    try {
                        JsonNode jsonArray = objectMapper.readTree(value);
                        List<Map<String, Object>> childrenField = new ArrayList<>();
                        for (JsonNode node : jsonArray) {
                            handleStr(apiDefinition, node.toString(), childrenField, rootPath + "." + fieldName + "[*]");
                        }
                        o.put("children", childrenField);
                        o.put("childrenDataType", "LIST");
                    } catch (Exception e) {
                        JSONArray array = new JSONArray();
                        array.add(StringUtils.isNotEmpty(jsonNode.get(fieldName).toString()) ? jsonNode.get(fieldName).toString() : "");
                        o.put("value", array);
                    }
                    o.put("jsonPath", rootPath + "." + String.format(path, fieldName));
                    setProperty(apiDefinition, o, fieldName);
                    if (!hasItem(apiDefinition, fields, o)) {
                        fields.add(o);
                    }
                } else if (StringUtils.isNotEmpty(value) && value.startsWith("{")) {
                    try {
                        JsonNode jsonNode1 = objectMapper.readTree(value);
                        List<Map<String, Object>> children = new ArrayList<>();
                        handleStr(apiDefinition, value, children, rootPath + "." + String.format(path, fieldName));
                        Map<String, Object> o = new HashMap<>();
                        o.put("children", children);
                        o.put("childrenDataType", "OBJECT");
                        o.put("jsonPath", rootPath + "." + fieldName);
                        setProperty(apiDefinition, o, fieldName);
                        if (!hasItem(apiDefinition, fields, o)) {
                            fields.add(o);
                        }
                    } catch (Exception e) {
                        Map<String, Object> o = new HashMap<>();
                        o.put("jsonPath", rootPath + "." + String.format(path, fieldName));
                        setProperty(apiDefinition, o, fieldName);
                        JSONArray array = new JSONArray();
                        array.add(StringUtils.isNotEmpty(value) ? value : "");
                        o.put("value", array);
                        if (!hasItem(apiDefinition, fields, o)) {
                            fields.add(o);
                        }
                    }
                } else {
                    Map<String, Object> o = new HashMap<>();
                    o.put("jsonPath", rootPath + "." + String.format(path, fieldName));
                    setProperty(apiDefinition, o, fieldName);
                    JSONArray array = new JSONArray();
                    array.add(StringUtils.isNotEmpty(value) ? value : "");
                    o.put("value", array);
                    if (!hasItem(apiDefinition, fields, o)) {
                        fields.add(o);
                    }
                }

            }
        }
    }

    static private void setProperty(ApiDefinition apiDefinition, Map<String, Object> o, String s) {
        o.put("fieldName", s);
        o.put("remarks", s);
        o.put("dbFieldName", s);
        o.put("type", "STRING");
        o.put("size", 65535);
        o.put("deExtractType", 0);
        o.put("deType", 0);
        o.put("checked", false);
        if (!apiDefinition.isUseJsonPath()) {
            for (TableField field : apiDefinition.getFields()) {
                if (!ObjectUtils.isEmpty(o.get("jsonPath")) && StringUtils.isNotEmpty(field.getJsonPath()) && field.getJsonPath().equals(o.get("jsonPath").toString())) {
                    o.put("checked", true);
                    o.put("remarks", field.getRemarks());
                    o.put("dbFieldName", field.getDbFieldName());
                }
            }
        }
    }

    private boolean hasItem(ApiDefinition apiDefinition, List<Map<String, Object>> fields, Map<String, Object> item) throws Exception{
        boolean has = false;
        for (Map<String, Object> field : fields) {
            if (field.get("jsonPath").equals(item.get("jsonPath"))) {
                has = true;
                mergeField(field, item);
                mergeValue(field, apiDefinition, item);
                break;
            }
        }

        return has;
    }


    private void mergeField(Map<String, Object> field, Map<String, Object> item) throws JsonProcessingException {
        if (item.get("children") != null) {
            List<Map<String, Object>> fieldChildren = objectMapper.readValue(field.get("children").toString(), listForMapTypeReference);
            List<Map<String, Object>> itemChildren = objectMapper.readValue(item.get("children").toString(), listForMapTypeReference);

            if (fieldChildren == null) {
                fieldChildren = new ArrayList<>();
            }
            for (Map<String, Object> itemChild : itemChildren) {
                boolean hasKey = false;
                for (Map<String, Object> fieldChild : fieldChildren) {
                    if (itemChild.get("jsonPath").toString().equals(fieldChild.get("jsonPath").toString())) {
                        mergeField(fieldChild, itemChild);
                        hasKey = true;
                    }
                }
                if (!hasKey) {
                    fieldChildren.add(itemChild);
                }
            }
        }
    }

    void mergeValue(Map<String, Object> field, ApiDefinition apiDefinition, Map<String, Object> item) throws Exception {
        if (!ObjectUtils.isEmpty(field.get("value")) && !ObjectUtils.isEmpty(item.get("value"))) {
            List<String> array = objectMapper.readValue(field.get("value").toString(), listTypeReference);
            array.add(objectMapper.readValue(item.get("value").toString(), listTypeReference).get(0));
            field.put("value", array);
        }
        if (!ObjectUtils.isEmpty(field.get("children")) && !ObjectUtils.isEmpty(item.get("children"))) {
            List<Map<String, Object>> fieldChildren = objectMapper.readValue(field.get("children").toString(), listForMapTypeReference);
            List<Map<String, Object>> itemChildren = objectMapper.readValue(item.get("children").toString(), listForMapTypeReference);

            List<Map<String, Object>> fieldArrayChildren = new ArrayList<>();
            for (Map<String, Object> fieldChild : fieldChildren) {
                Map<String, Object> find = null;
                for (Map<String, Object> itemChild : itemChildren) {
                    if (fieldChild.get("jsonPath").toString().equals(itemChild.get("jsonPath").toString())) {
                        find = itemChild;
                    }
                }
                if (find != null) {
                    mergeValue(fieldChild, apiDefinition, find);
                }
                fieldArrayChildren.add(fieldChild);
            }
            field.put("children", fieldArrayChildren);
        }
    }

    private List<String[]> fetchResult(String result, ApiDefinition apiDefinition) {
        List<String[]> dataList = new LinkedList<>();
        if (apiDefinition.isUseJsonPath()) {
            List<LinkedHashMap> currentData = new ArrayList<>();
            Object object = JsonPath.read(result, apiDefinition.getJsonPath());
            if (object instanceof List) {
                currentData = (List<LinkedHashMap>) object;
            } else {
                currentData.add((LinkedHashMap) object);
            }
            for (LinkedHashMap data : currentData) {
                String[] row = new String[apiDefinition.getFields().size()];
                int i = 0;
                for (TableField field : apiDefinition.getFields()) {
                    row[i] = Optional.ofNullable(data.get(field.getFieldName())).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " ");
                    i++;
                }
                dataList.add(row);
            }
        } else {
            List<String> jsonPaths = apiDefinition.getFields().stream().map(TableField::getJsonPath).collect(Collectors.toList());
            Long maxLength = 0l;
            List<List<String>> columnDataList = new ArrayList<>();
            for (int i = 0; i < jsonPaths.size(); i++) {
                List<String> data = new ArrayList<>();
                Object object = JsonPath.read(result, jsonPaths.get(i));
                if (object instanceof List && jsonPaths.get(i).contains("[*]")) {
                    data = (List<String>) object;
                } else {
                    if (object != null) {
                        data.add(object.toString());
                    }
                }
                maxLength = maxLength > data.size() ? maxLength : data.size();
                columnDataList.add(data);
            }
            for (int i = 0; i < maxLength; i++) {
                String[] row = new String[apiDefinition.getFields().size()];
                dataList.add(row);
            }
            for (int i = 0; i < columnDataList.size(); i++) {
                for (int j = 0; j < columnDataList.get(i).size(); j++) {
                    dataList.get(j)[i] = Optional.ofNullable(String.valueOf(columnDataList.get(i).get(j))).orElse("").replaceAll("\n", " ").replaceAll("\r", " ");
                }
            }
        }
        return dataList;
    }


    private ApiDefinition checkApiDefinition(DatasourceRequest datasourceRequest) throws Exception {
        List<ApiDefinition> apiDefinitionList = new ArrayList<>();
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
        List<ApiDefinition> apiDefinitionListTemp = objectMapper.readValue(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);
        if (!CollectionUtils.isEmpty(apiDefinitionListTemp)) {
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
