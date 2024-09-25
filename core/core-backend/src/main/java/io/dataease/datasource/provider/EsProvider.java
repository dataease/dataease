package io.dataease.datasource.provider;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import io.dataease.dataset.utils.FieldUtils;
import io.dataease.datasource.dto.es.EsResponse;
import io.dataease.datasource.dto.es.Request;
import io.dataease.datasource.type.Es;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.i18n.Translator;

import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import io.dataease.utils.JsonUtil;
import org.apache.commons.codec.binary.Base64;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service("esProvider")
public class EsProvider extends Provider {

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        return new ArrayList<>();
    }

    @Override
    public List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) {
        List<DatasetTableDTO> tables = new ArrayList<>();
        try {
            String response = execQuery(datasourceRequest, "show tables", "?format=json");
            tables = fetchTables(response);
            tables = tables.stream().filter(table -> StringUtils.isNotEmpty(table.getTableName()) && !table.getTableName().startsWith(".")).collect(Collectors.toList());
            tables.forEach(table -> {
                table.setDatasourceId(datasourceRequest.getDatasource().getId());
            });
        } catch (Exception e) {
            e.getMessage();
            DEException.throwException(e);
        }
        return tables;
    }

    @Override
    public ConnectionObj getConnection(DatasourceDTO coreDatasource) throws Exception {
        return null;
    }

    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        Es es = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Es.class);
        String response = execGetQuery(datasourceRequest);
        if (JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("error") != null) {
            throw new Exception(JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("error").get("reason").getAsString());
        }
        String version = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("version").get("number").getAsString();
        String[] versionList = version.split("\\.");
        if (Integer.valueOf(versionList[0]) < 7 && Integer.valueOf(versionList[1]) < 3) {
            throw new Exception(Translator.get("i18n_es_limit"));
        }
        if (Integer.valueOf(versionList[0]) == 6) {
            es.setUri("_xpack/sql");
        }
        if (Integer.valueOf(versionList[0]) > 6) {
            es.setUri("_sql");
        }
        datasourceRequest.getDatasource().setConfiguration(JsonUtil.toJSONString(es).toString());
        getTables(datasourceRequest);
        return "Success";
    }

    @Override
    public Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = execQuery(datasourceRequest, datasourceRequest.getQuery(), "?format=json");
            result.put("dataList", fetchResultData(response));
            result.put("fieldList", fetchResultField4Sql(response));
        } catch (Exception e) {
            e.printStackTrace();
            DEException.throwException(e);
        }
        return result;
    }

    @Override
    public List<TableField> fetchTableField(DatasourceRequest datasourceRequest) {
        List<TableField> tableFields = new ArrayList<>();
        try {
            String response = execQuery(datasourceRequest, "select * from  " + datasourceRequest.getTable() + " limit 0", "?format=json");
            tableFields = fetchResultField4Sql(response);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return tableFields;
    }


    @Override
    public void hidePW(DatasourceDTO datasourceDTO) {
    }


    private List<String[]> fetchResultData(String response) throws Exception {
        EsResponse esResponse = new Gson().fromJson(response, EsResponse.class);
        return fetchResultData(esResponse);
    }

    private List<String[]> fetchResultData(EsResponse esResponse) throws Exception {
        List<String[]> list = new LinkedList<>();
        if (esResponse.getError() != null) {
            throw new Exception(esResponse.getError().getReason());
        }
        list.addAll(esResponse.getRows());
        return list;
    }

    private List<TableField> fetchResultField4Sql(String response) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        EsResponse esResponse = new Gson().fromJson(response, EsResponse.class);
        if (esResponse.getError() != null) {
            throw new Exception(esResponse.getError().getReason());
        }

        for (EsResponse.Column column : esResponse.getColumns()) {
            TableField field = new TableField();
            field.setOriginName(column.getName());
            field.setOriginName(column.getName());
            field.setFieldType(column.getType());
            field.setType(column.getType().toUpperCase());
            field.setFieldType(field.getType());
            int deType = FieldUtils.transType2DeType(field.getType());
            field.setDeExtractType(deType);
            field.setDeType(deType);
            fieldList.add(field);
        }
        return fieldList;
    }

    private List<DatasetTableDTO> fetchTables(String response) throws Exception {
        List<DatasetTableDTO> tables = new ArrayList<>();
        EsResponse esResponse = new Gson().fromJson(response, EsResponse.class);
        if (esResponse.getError() != null) {
            throw new Exception(esResponse.getError().getReason());
        }

        for (String[] row : esResponse.getRows()) {

            DatasetTableDTO tableDesc = new DatasetTableDTO();
            if (row.length == 3 && row[1].contains("TABLE") && row[2].equalsIgnoreCase("INDEX")) {
                tableDesc.setTableName(row[0]);
            }
            if (row.length == 2 && row[1].contains("TABLE")) {
                tableDesc.setTableName(row[0]);
            }
            if (row.length == 4 && row[2].contains("TABLE") && row[3].equalsIgnoreCase("INDEX")) {
                tableDesc.setTableName(row[1]);
            }
            tableDesc.setType("es");
            tables.add(tableDesc);
        }
        return tables;
    }


    private String execQuery(DatasourceRequest datasourceRequest, String sql, String uri) {
        Es es = null;
        if (datasourceRequest.getDatasource() == null) {
            Collection<DatasourceSchemaDTO> datasourceSchemaDTOS = datasourceRequest.getDsList().values();
            es = JsonUtil.parseObject(datasourceSchemaDTOS.stream().findFirst().get().getConfiguration(), Es.class);
        } else {
            es = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Es.class);
        }

        uri = es.getUri() + uri;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        if (StringUtils.isNotEmpty(es.getUsername()) && StringUtils.isNotEmpty(es.getPassword())) {
            String auth = es.getUsername() + ":" + es.getPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
        }
        Request request = new Request();
        request.setQuery(sql);
        request.setFetch_size(datasourceRequest.getFetchSize());
        String url = es.getUrl().endsWith("/") ? es.getUrl() + uri : es.getUrl() + "/" + uri;
        return HttpClientUtil.post(url, new Gson().toJson(request), httpClientConfig);

    }

    private String execGetQuery(DatasourceRequest datasourceRequest) {
        Es es = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Es.class);
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        if (StringUtils.isNotEmpty(es.getUsername()) && StringUtils.isNotEmpty(es.getPassword())) {
            String auth = es.getUsername() + ":" + es.getPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
        }
        return HttpClientUtil.get(es.getUrl(), httpClientConfig);
    }


}
