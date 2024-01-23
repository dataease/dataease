package io.dataease.datasource.provider;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.ApiDefinition;
import io.dataease.api.ds.vo.ApiDefinitionRequest;
import io.dataease.api.ds.vo.TableField;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DEException;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import io.dataease.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class ApiUtils {

    private static String path = "['%s']";
    public static ObjectMapper objectMapper = CommonBeanFactory.getBean(ObjectMapper.class);

    private static TypeReference<List<Object>> listTypeReference = new TypeReference<List<Object>>() {
    };
    private static TypeReference<List<Map<String, Object>>> listForMapTypeReference = new TypeReference<List<Map<String, Object>>>() {
    };

    public static List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) throws DEException {
        List<DatasetTableDTO> tableDescs = new ArrayList<>();
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
        List<ApiDefinition> apiDefinitionList = JsonUtil.parseList(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            DatasetTableDTO datasetTableDTO = new DatasetTableDTO();
            datasetTableDTO.setTableName(apiDefinition.getDeTableName());
            datasetTableDTO.setName(apiDefinition.getName());
            datasetTableDTO.setDatasourceId(datasourceRequest.getDatasource().getId());
            tableDescs.add(datasetTableDTO);
        }
        return tableDescs;
    }


    public static Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException {
        Map<String, Object> result = new HashMap<>();
        List<String[]> dataList = new ArrayList<>();
        List<TableField> fieldList = new ArrayList<>();
        ApiDefinition apiDefinition = checkApiDefinition(datasourceRequest);
        if (apiDefinition == null) {
            DEException.throwException("未找到");
        }
        String response = execHttpRequest(apiDefinition, 10);
        fieldList = getTableFields(apiDefinition);
        result.put("fieldList", fieldList);
        dataList = fetchResult(response, apiDefinition);
        result.put("dataList", dataList);
        return result;
    }


    private static List<TableField> getTableFields(ApiDefinition apiDefinition) throws DEException {
        return apiDefinition.getFields();
    }

    public static List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws DEException {
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };

        List<TableField> tableFields = new ArrayList<>();
        try {
            List<ApiDefinition> lists = objectMapper.readValue(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);
            for (ApiDefinition apiDefinition : lists) {
                if (datasourceRequest.getTable().equalsIgnoreCase(apiDefinition.getDeTableName())) {
                    tableFields = getTableFields(apiDefinition);
                }
            }
        } catch (Exception e) {

        }
        return tableFields;
    }

    public static String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
        List<ApiDefinition> apiDefinitionList = JsonUtil.parseList(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);
        List<ObjectNode> status = new ArrayList();
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            datasourceRequest.setTable(apiDefinition.getName());
            ObjectNode apiItemStatuses = objectMapper.createObjectNode();
            try {
                getData(datasourceRequest);
                apiItemStatuses.put("name", apiDefinition.getName());
                apiItemStatuses.put("status", "Success");
            } catch (Exception ignore) {
                apiItemStatuses.put("name", apiDefinition.getName());
                apiItemStatuses.put("status", "Error");
            }
            status.add(apiItemStatuses);
        }
        return JsonUtil.toJSONString(status).toString();
    }

    private static List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception {
        ApiDefinition apiDefinition = checkApiDefinition(datasourceRequest);
        if (apiDefinition == null) {
            DEException.throwException("未找到");
        }
        String response = execHttpRequest(apiDefinition, 10);
        return fetchResult(response, apiDefinition);
    }


    public static String execHttpRequest(ApiDefinition apiDefinition, int socketTimeout) {
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

        List<String> params = new ArrayList<>();
        for (Map<String, String> argument : apiDefinition.getRequest().getArguments()) {
            if (StringUtils.isNotEmpty(argument.get("name")) && StringUtils.isNotEmpty(argument.get("value"))) {
                params.add(argument.get("name") + "=" + URLEncoder.encode(argument.get("value")));
            }
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(params)) {
            apiDefinition.setUrl(apiDefinition.getUrl() + "?" + StringUtils.join(params, "&"));
        }

        switch (apiDefinition.getMethod()) {
            case "GET":

                response = HttpClientUtil.get(apiDefinition.getUrl().trim(), httpClientConfig);
                break;
            case "POST":
                if (!apiDefinitionRequest.getBody().keySet().contains("type")) {
                    DEException.throwException("请求类型不能为空");
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
                        TypeReference<List<JsonNode>> listTypeReference = new TypeReference<List<JsonNode>>() {
                        };
                        List<JsonNode> rootNode = null;
                        try {
                            rootNode = objectMapper.readValue(JsonUtil.toJSONString(apiDefinition.getRequest().getBody().get("kvs")).toString(), listTypeReference);
                        } catch (Exception e) {
                            e.printStackTrace();
                            DEException.throwException(e);
                        }
                        for (JsonNode jsonNode : rootNode) {
                            if (jsonNode.has("name")) {
                                body.put(jsonNode.get("name").asText(), jsonNode.get("value").asText());
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


    public static ApiDefinition checkApiDefinition(ApiDefinition apiDefinition, String response) throws DEException {
        if (StringUtils.isEmpty(response)) {
            DEException.throwException("该请求返回数据为空");
        }
        List<Map<String, Object>> fields = new ArrayList<>();
        if (apiDefinition.isShowApiStructure() || !apiDefinition.isUseJsonPath()) {
            String rootPath;
            if (response.startsWith("[")) {
                rootPath = "$[*]";
                JsonNode jsonArray = null;
                try {
                    jsonArray = objectMapper.readTree(response);
                } catch (Exception e) {
                    DEException.throwException(e);
                }
                for (Object o : jsonArray) {
                    handleStr(apiDefinition, o.toString(), fields, rootPath);
                }
            } else {
                rootPath = "$";
                handleStr(apiDefinition, response, fields, rootPath);
            }
            apiDefinition.setJsonFields(fields);
            return apiDefinition;
        } else {
            List<LinkedHashMap> currentData = new ArrayList<>();
            try {
                Object object = JsonPath.read(response, apiDefinition.getJsonPath());
                if (object instanceof List) {
                    currentData = (List<LinkedHashMap>) object;
                } else {
                    currentData.add((LinkedHashMap) object);
                }
            } catch (Exception e) {
                DEException.throwException(e);
            }
            int i = 0;
            try {
                LinkedHashMap data  =  currentData.get(0);
            }catch (Exception e){
                DEException.throwException("数据不符合规范, " + e.getMessage());
            }
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
                    JSONArray array = new JSONArray();
                    if (field.get("value") != null) {
                        try {
                            TypeReference<JSONArray> listTypeReference = new TypeReference<JSONArray>() {
                            };
                            array = objectMapper.readValue(field.get("value").toString(), listTypeReference);
                        } catch (Exception e) {
                            e.printStackTrace();
                            DEException.throwException(e);
                        }
                        array.add(Optional.ofNullable(data.get(field.get("originName"))).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " "));
                    } else {
                        array.add(Optional.ofNullable(data.get(field.get("originName"))).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " "));
                    }
                    field.put("value", array);
                }
                i++;
            }
            apiDefinition.setJsonFields(fields);
            return apiDefinition;
        }
    }


    private static void handleStr(ApiDefinition apiDefinition, String jsonStr, List<Map<String, Object>> fields, String rootPath) throws DEException {
        if (jsonStr.startsWith("[")) {
            TypeReference<List<Object>> listTypeReference = new TypeReference<List<Object>>() {
            };
            List<Object> jsonArray = null;

            try {
                jsonArray = objectMapper.readValue(jsonStr, listTypeReference);
            } catch (Exception e) {
                DEException.throwException(e);
            }
            for (Object o : jsonArray) {
                handleStr(apiDefinition, o.toString(), fields, rootPath);
            }
        } else {
            JsonNode jsonNode = null;
            try {
                jsonNode = objectMapper.readTree(jsonStr);
            } catch (Exception e) {
                DEException.throwException(e);
            }
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String value = jsonNode.get(fieldName).toString();
                if (StringUtils.isNotEmpty(value) && !value.startsWith("[") && !value.startsWith("{")) {
                    value = jsonNode.get(fieldName).asText();
                }
                if (StringUtils.isNotEmpty(value) && value.startsWith("[")) {
                    Map<String, Object> o = new HashMap<>();
                    try {
                        JsonNode jsonArray = objectMapper.readTree(value);
                        List<Map<String, Object>> childrenField = new ArrayList<>();
                        for (JsonNode node : jsonArray) {
                            if (StringUtils.isNotEmpty(node.toString()) && !node.toString().startsWith("[") && !node.toString().startsWith("{")) {
                                throw new Exception(node + "is not json type");
                            }
                        }
                        for (JsonNode node : jsonArray) {
                            handleStr(apiDefinition, node.toString(), childrenField, rootPath + "." + String.format(path, fieldName) + "[*]");
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

    private static void setProperty(ApiDefinition apiDefinition, Map<String, Object> o, String s) {
        o.put("originName", s);
        o.put("name", s);
        o.put("type", "STRING");
        o.put("size", 65535);
        o.put("deExtractType", 0);
        o.put("deType", 0);
        o.put("checked", false);
        if (!apiDefinition.isUseJsonPath()) {
            for (TableField field : apiDefinition.getFields()) {
                if (!ObjectUtils.isEmpty(o.get("jsonPath")) && StringUtils.isNotEmpty(field.getJsonPath()) && field.getJsonPath().equals(o.get("jsonPath").toString())) {
                    o.put("checked", true);
                    o.put("name", field.getName());
                    o.put("deExtractType", field.getDeExtractType());
                }
            }
        }
    }

    private static boolean hasItem(ApiDefinition apiDefinition, List<Map<String, Object>> fields, Map<String, Object> item) throws DEException {
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


    private static void mergeField(Map<String, Object> field, Map<String, Object> item) throws DEException {
        if (item.get("children") != null) {
            List<Map<String, Object>> fieldChildren = null;
            List<Map<String, Object>> itemChildren = null;
            try {
                fieldChildren = objectMapper.readValue(JsonUtil.toJSONString(field.get("children")).toString(), listForMapTypeReference);
                itemChildren = objectMapper.readValue(JsonUtil.toJSONString(item.get("children")).toString(), listForMapTypeReference);
            } catch (Exception e) {
                DEException.throwException(e);
            }
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

    private static void mergeValue(Map<String, Object> field, ApiDefinition apiDefinition, Map<String, Object> item) throws DEException {
        TypeReference<JSONArray> listTypeReference = new TypeReference<JSONArray>() {
        };
        try {
            if (!ObjectUtils.isEmpty(field.get("value")) && !ObjectUtils.isEmpty(item.get("value"))) {
                JSONArray array = objectMapper.readValue(JsonUtil.toJSONString(field.get("value")).toString(), listTypeReference);
                array.add(objectMapper.readValue(JsonUtil.toJSONString(item.get("value")).toString(), listTypeReference).get(0));
                field.put("value", array);
            }
            if (!ObjectUtils.isEmpty(field.get("children")) && !ObjectUtils.isEmpty(item.get("children"))) {
                List<Map<String, Object>> fieldChildren = objectMapper.readValue(JsonUtil.toJSONString(field.get("children")).toString(), listForMapTypeReference);
                List<Map<String, Object>> itemChildren = objectMapper.readValue(JsonUtil.toJSONString(item.get("children")).toString(), listForMapTypeReference);
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
        } catch (Exception e) {
            e.printStackTrace();
            DEException.throwException(e);
        }

    }

    private static List<String[]> fetchResult(String result, ApiDefinition apiDefinition) {
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
                    row[i] = Optional.ofNullable(data.get(field.getName())).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " ");
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


    private static ApiDefinition checkApiDefinition(DatasourceRequest datasourceRequest) throws DEException {
        List<ApiDefinition> apiDefinitionList = new ArrayList<>();
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
        List<ApiDefinition> apiDefinitionListTemp = null;
        try {
            apiDefinitionListTemp = objectMapper.readValue(datasourceRequest.getDatasource().getConfiguration(), listTypeReference);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        if (!CollectionUtils.isEmpty(apiDefinitionListTemp)) {
            for (ApiDefinition apiDefinition : apiDefinitionListTemp) {
                if (apiDefinition.getDeTableName().equalsIgnoreCase(datasourceRequest.getTable()) || apiDefinition.getName().equalsIgnoreCase(datasourceRequest.getTable())) {
                    apiDefinitionList.add(apiDefinition);
                }

            }
        }
        if (CollectionUtils.isEmpty(apiDefinitionList)) {
            DEException.throwException("未找到API数据表");
        }
        if (apiDefinitionList.size() > 1) {
            DEException.throwException("存在重名的API数据表");
        }
        ApiDefinition find = null;
        for (ApiDefinition apiDefinition : apiDefinitionList) {
            if (apiDefinition.getName().equalsIgnoreCase(datasourceRequest.getTable()) || apiDefinition.getDeTableName().equalsIgnoreCase(datasourceRequest.getTable())) {
                find = apiDefinition;
            }
        }
        return find;
    }

}
