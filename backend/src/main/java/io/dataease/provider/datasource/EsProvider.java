package io.dataease.provider.datasource;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.request.datasource.es.EsReponse;
import io.dataease.controller.request.datasource.es.Request;
import io.dataease.controller.request.datasource.es.RequestWithCursor;
import io.dataease.dto.datasource.EsConfiguration;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.constants.datasource.EsSqlLConstants;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.provider.query.es.EsQueryProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service("esProviders")
public class EsProvider extends Provider {


    /**
     * 增加缓存机制 key 由 'provider_sql_' dsr.datasource.id dsr.table dsr.query共4部分组成，命中则使用缓存直接返回不再执行sql逻辑
     * @param dsr
     * @return
     * @throws Exception
     */
    /**
     * 这里使用声明式缓存不是很妥当
     * 改为chartViewService中使用编程式缓存
     *
     * @Cacheable( value = JdbcConstants.JDBC_PROVIDER_KEY,
     * key = "'provider_sql_' + #dsr.datasource.id + '_' + #dsr.table + '_' + #dsr.query",
     * condition = "#dsr.pageSize == null || #dsr.pageSize == 0L"
     * )
     */
    @Override
    public List<String[]> getData(DatasourceRequest dsr) throws Exception {
        List<String[]> list = new LinkedList<>();
        try {
            EsConfiguration esConfiguration = new Gson().fromJson(dsr.getDatasource().getConfiguration(), EsConfiguration.class);
            HttpClientConfig httpClientConfig = new HttpClientConfig();
            if (StringUtils.isNotEmpty(esConfiguration.getEsUsername())) {
                String auth = esConfiguration.getEsUsername() + ":" + esConfiguration.getEsPassword();
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
            }
            Request request = new Request();
            request.setQuery(dsr.getQuery());
            request.setFetch_size(dsr.getFetchSize());
            String url = esConfiguration.getUrl().endsWith("/") ? esConfiguration.getUrl() + esConfiguration.getUri() + "?format=json" : esConfiguration.getUrl() + "/" + esConfiguration.getUri() + "?format=json";
            String response = HttpClientUtil.post(url, new Gson().toJson(request), httpClientConfig);
            EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);

            list.addAll(fetchResult(esReponse));
            if (dsr.isPageable()) {
                Integer realSize = dsr.getPage() * dsr.getPageSize() < list.size() ? dsr.getPage() * dsr.getPageSize() : list.size();
                list = list.subList((dsr.getPage() - 1) * dsr.getPageSize(), realSize);
            }
            if (!dsr.isPreviewData()) {
                while (StringUtils.isNotEmpty(esReponse.getCursor())) {
                    RequestWithCursor requstWithCursor = new RequestWithCursor();
                    requstWithCursor.setQuery(dsr.getQuery());
                    requstWithCursor.setFetch_size(dsr.getFetchSize());
                    requstWithCursor.setCursor(esReponse.getCursor());
                    response = HttpClientUtil.post(url, new Gson().toJson(requstWithCursor), httpClientConfig);
                    esReponse = new Gson().fromJson(response, EsReponse.class);
                    list.addAll(fetchResult(esReponse));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DataEaseException.throwException(e);
        }
        return list;
    }

    @Override
    public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();
        try {
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "?format=json");
            list = fetchResult(response);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return list;
    }

    @Override
    public List<TableField> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        datasourceRequest.setQuery("desc " + String.format(EsSqlLConstants.KEYWORD_TABLE, datasourceRequest.getTable()));
        List<TableField> tableFields = new ArrayList<>();
        try {
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "?format=json");
            tableFields = fetchResultField4Table(response);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return tableFields;
    }


    private List<String[]> fetchResult(String response) throws Exception {
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        return fetchResult(esReponse);
    }

    private List<String[]> fetchResult(EsReponse esReponse) throws Exception {
        List<String[]> list = new LinkedList<>();
        if (esReponse.getError() != null) {
            throw new Exception(esReponse.getError().getReason());
        }
        list.addAll(esReponse.getRows());
        return list;
    }

    @Override
    public List<TableField> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> tableFields = new ArrayList<>();
        try {
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "?format=json");
            tableFields = fetchResultField4Sql(response);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return tableFields;
    }

    private List<TableField> fetchResultField(String response) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        if (esReponse.getError() != null) {
            throw new Exception(esReponse.getError().getReason());
        }

        for (String[] row : esReponse.getRows()) {
            TableField field = new TableField();
            field.setFieldName(row[0]);
            field.setRemarks(row[0]);
            field.setFieldType(row[2]);
            field.setFieldSize(EsQueryProvider.transFieldTypeSize(row[2]));
            fieldList.add(field);
        }
        return fieldList;
    }

    private List<TableField> fetchResultField4Sql(String response) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        if (esReponse.getError() != null) {
            throw new Exception(esReponse.getError().getReason());
        }

        for (EsReponse.Column column : esReponse.getColumns()) {
            TableField field = new TableField();
            field.setFieldName(column.getName());
            field.setRemarks(column.getName());
            field.setFieldType(column.getType());
            field.setFieldSize(EsQueryProvider.transFieldTypeSize(column.getType()));
            fieldList.add(field);
        }
        return fieldList;
    }

    private List<TableField> fetchResultField4Table(String response) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        if (esReponse.getError() != null) {
            throw new Exception(esReponse.getError().getReason());
        }

        for (String[] row : esReponse.getRows()) {
            if(!row[1].equalsIgnoreCase("STRUCT")){
                TableField field = new TableField();
                field.setFieldName(row[0]);
                field.setRemarks(row[0]);
                field.setFieldType(row[2]);
                field.setFieldSize(EsQueryProvider.transFieldTypeSize(row[2]));
                fieldList.add(field);
            }
        }
        return fieldList;
    }

    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        try {
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "?format=json");
            result.put("dataList", fetchResult(response));
            result.put("fieldList", fetchResultField4Sql(response));
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return result;
    }

    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tables = new ArrayList<>();
        try {
            String response = exexQuery(datasourceRequest, "show tables", "?format=json");
            tables = fetchTables(response);
            tables = tables.stream().filter(table -> !table.getName().startsWith(".")).collect(Collectors.toList());
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return tables;
    }

    private List<TableDesc> fetchTables(String response) throws Exception {
        List<TableDesc> tables = new ArrayList<>();
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        if (esReponse.getError() != null) {
            throw new Exception(esReponse.getError().getReason());
        }

        for (String[] row : esReponse.getRows()) {
            if (row.length == 3 && row[1].contains("TABLE") && row[2].equalsIgnoreCase("INDEX")) {
                TableDesc tableDesc = new TableDesc();
                tableDesc.setName(row[0]);
                tables.add(tableDesc);
            }
            if (row.length == 2 && row[1].contains("TABLE")) {
                TableDesc tableDesc = new TableDesc();
                tableDesc.setName(row[0]);
                tables.add(tableDesc);
            }
            if (row.length == 4 && row[2].contains("TABLE") && row[3].equalsIgnoreCase("INDEX")) {
                TableDesc tableDesc = new TableDesc();
                tableDesc.setName(row[1]);
                tables.add(tableDesc);
            }
        }
        return tables;
    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        return new ArrayList<>();
    }


    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        Gson gson = new Gson();
        EsConfiguration esConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), EsConfiguration.class);
        String response = exexGetQuery(datasourceRequest);

        if (JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("error") != null) {
            throw new Exception(JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("error").get("reason").getAsString());
        }
        String version = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("version").get("number").getAsString();
        String[] versionList = version.split("\\.");
        if (Integer.valueOf(versionList[0]) < 7 && Integer.valueOf(versionList[1]) < 3) {
            throw new Exception(Translator.get("i18n_es_limit"));
        }

        if (Integer.valueOf(versionList[0]) == 6) {
            esConfiguration.setUri("_xpack/sql");
        }
        if (Integer.valueOf(versionList[0]) > 6) {
            esConfiguration.setUri("_sql");
        }
        datasourceRequest.getDatasource().setConfiguration(new Gson().toJson(esConfiguration));
        getTables(datasourceRequest);
        return "Success";
    }

    private String exexQuery(DatasourceRequest datasourceRequest, String sql, String uri) {
        EsConfiguration esConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), EsConfiguration.class);
        uri = esConfiguration.getUri() + uri;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        if (StringUtils.isNotEmpty(esConfiguration.getEsUsername()) && StringUtils.isNotEmpty(esConfiguration.getEsPassword())) {
            String auth = esConfiguration.getEsUsername() + ":" + esConfiguration.getEsPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
        }

        Request request = new Request();
        request.setQuery(sql);
        request.setFetch_size(datasourceRequest.getFetchSize());
        String url = esConfiguration.getUrl().endsWith("/") ? esConfiguration.getUrl() + uri : esConfiguration.getUrl() + "/" + uri;
        String response = HttpClientUtil.post(url, new Gson().toJson(request), httpClientConfig);
        return response;
    }

    private String exexGetQuery(DatasourceRequest datasourceRequest) {
        EsConfiguration esConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), EsConfiguration.class);
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        if (StringUtils.isNotEmpty(esConfiguration.getEsUsername()) && StringUtils.isNotEmpty(esConfiguration.getEsPassword())) {
            String auth = esConfiguration.getEsUsername() + ":" + esConfiguration.getEsPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
        }

        String response = HttpClientUtil.get(esConfiguration.getUrl(), httpClientConfig);
        return response;
    }

}
