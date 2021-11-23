package io.dataease.provider.datasource;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.request.datasource.es.EsReponse;
import io.dataease.controller.request.datasource.es.Requst;
import io.dataease.controller.request.datasource.es.RequstWithCursor;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.dto.datasource.EsConfiguration;
import io.dataease.dto.datasource.TableFiled;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.query.QueryProvider;
import io.dataease.provider.query.es.EsQueryProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service("es")
public class EsProvider extends DatasourceProvider {


    /**
     * 增加缓存机制 key 由 'provider_sql_' dsr.datasource.id dsr.table dsr.query共4部分组成，命中则使用缓存直接返回不再执行sql逻辑
     * @param dsr
     * @return
     * @throws Exception
     */
    /**
     * 这里使用声明式缓存不是很妥当
     * 改为chartViewService中使用编程式缓存
    @Cacheable(
            value = JdbcConstants.JDBC_PROVIDER_KEY,
            key = "'provider_sql_' + #dsr.datasource.id + '_' + #dsr.table + '_' + #dsr.query",
            condition = "#dsr.pageSize == null || #dsr.pageSize == 0L"
    )
     */
    @Override
    public List<String[]> getData(DatasourceRequest dsr) throws Exception {
        List<String[]> list = new LinkedList<>();
        try {
            EsConfiguration esConfiguration = new Gson().fromJson(dsr.getDatasource().getConfiguration(), EsConfiguration.class);
            HttpClientConfig httpClientConfig = new HttpClientConfig();
            if(StringUtils.isNotEmpty(esConfiguration.getEsUsername())){
                String auth = esConfiguration.getEsUsername() + ":" + esConfiguration.getEsPassword();
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
            }
            Requst requst = new Requst();
            requst.setQuery(dsr.getQuery());
            requst.setFetch_size(dsr.getFetchSize());
            String url = esConfiguration.getUrl().endsWith("/") ? esConfiguration.getUrl() + esConfiguration.getUri() + "?format=json" : esConfiguration.getUrl() + "/" + esConfiguration.getUri() + "?format=json";
            String  response = HttpClientUtil.post(url, new Gson().toJson(requst), httpClientConfig);
            EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);

            list.addAll(fetchResult(esReponse));
            if(dsr.isPageable()){
                Integer realSize = dsr.getPage() * dsr.getPageSize() < list.size() ? dsr.getPage() * dsr.getPageSize(): list.size();
                list = list.subList((dsr.getPage() - 1) * dsr.getPageSize(), realSize);
            }
            if(!dsr.isPreviewData()){
               while (StringUtils.isNotEmpty(esReponse.getCursor())) {
                   RequstWithCursor requstWithCursor = new RequstWithCursor();
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
    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        QueryProvider qp = ProviderFactory.getQueryProvider(datasourceRequest.getDatasource().getType());
        datasourceRequest.setQuery(qp.convertTableToSql(datasourceRequest.getTable(), datasourceRequest.getDatasource()));
        return fetchResultField(datasourceRequest);
    }

    private List<String[]> fetchResult(String response) throws Exception {
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        return fetchResult(esReponse);
    }

    private List<String[]> fetchResult(EsReponse esReponse) throws Exception {
        List<String[]> list = new LinkedList<>();
        if(esReponse.getError() != null){
            throw new Exception(esReponse.getError().getReason());
        }
        list.addAll(esReponse.getRows());
        return list;
    }

    @Override
    public List<TableFiled> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        List<TableFiled> tableFileds = new ArrayList<>();
        try {
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "?format=json");
            tableFileds = fetchResultField4Sql(response);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return tableFileds;
    }

    private List<TableFiled> fetchResultField(String response) throws Exception {
        List<TableFiled> fieldList = new ArrayList<>();
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        if(esReponse.getError() != null){
            throw new Exception(esReponse.getError().getReason());
        }

        for (String[] row : esReponse.getRows()) {
            TableFiled field = new TableFiled();
            field.setFieldName(row[0]);
            field.setRemarks(row[0]);
            field.setFieldType(row[2]);
            field.setFieldSize(EsQueryProvider.transFieldTypeSize(row[2]));
            fieldList.add(field);
        }
        return fieldList;
    }

    private List<TableFiled> fetchResultField4Sql(String response) throws Exception {
        List<TableFiled> fieldList = new ArrayList<>();
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        if(esReponse.getError() != null){
            throw new Exception(esReponse.getError().getReason());
        }

        for (EsReponse.Column column : esReponse.getColumns()) {
            TableFiled field = new TableFiled();
            field.setFieldName(column.getName());
            field.setRemarks(column.getName());
            field.setFieldType(column.getType());
            field.setFieldSize(EsQueryProvider.transFieldTypeSize(column.getType()));
            fieldList.add(field);
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
    public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception {
    }

    @Override
    public List<String> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<String> tables = new ArrayList<>();
        try {
            String response = exexQuery(datasourceRequest, "show tables", "?format=json");
            tables = fetchTables(response);
            tables = tables.stream().filter(table -> !table.startsWith(".")).collect(Collectors.toList());
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return tables;
    }

    private List<String> fetchTables(String response) throws Exception {
        List<String> tables = new ArrayList<>();
        EsReponse esReponse = new Gson().fromJson(response, EsReponse.class);
        if(esReponse.getError() != null){
            throw new Exception(esReponse.getError().getReason());
        }

        for (String[] row : esReponse.getRows()) {
           if(row.length == 3 && row[1].contains("TABLE") && row[2].equalsIgnoreCase("INDEX")){
               tables.add(row[0]);
           }
            if(row.length == 2 && row[1].contains("TABLE")){
                tables.add(row[0]);
            }
        }
        return tables;
    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        return new ArrayList<>();
    }



    @Override
    public void checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        EsConfiguration esConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), EsConfiguration.class);
        String response = exexGetQuery(datasourceRequest);

        if(JSONObject.parseObject(response).getJSONObject("error") != null){
            throw new Exception(JSONObject.parseObject(response).getJSONObject("error").getString("reason"));
        }
        String version =  JSONObject.parseObject(response).getJSONObject("version").getString("number");
        String[] versionList = version.split("\\.");
        if(Integer.valueOf(versionList[0]) < 7 && Integer.valueOf(versionList[1]) < 3){
            throw new Exception(Translator.get("i18n_es_limit"));
        }

        if(Integer.valueOf(versionList[0]) == 6 ) {
            esConfiguration.setUri("_xpack/sql");
        }
        if(Integer.valueOf(versionList[0]) == 7 ) {
            esConfiguration.setUri("_sql");
        }
        datasourceRequest.getDatasource().setConfiguration(new Gson().toJson(esConfiguration));
        getTables(datasourceRequest);
    }

    private String exexQuery(DatasourceRequest datasourceRequest, String sql, String uri){
        EsConfiguration esConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), EsConfiguration.class);
        uri = esConfiguration.getUri()+uri;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        if(StringUtils.isNotEmpty(esConfiguration.getEsUsername()) && StringUtils.isNotEmpty(esConfiguration.getEsPassword())){
            String auth = esConfiguration.getEsUsername() + ":" + esConfiguration.getEsPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
        }

        Requst requst = new Requst();
        requst.setQuery(sql);
        requst.setFetch_size(datasourceRequest.getFetchSize());
        String url = esConfiguration.getUrl().endsWith("/") ? esConfiguration.getUrl() + uri : esConfiguration.getUrl() + "/" + uri;
        String  response = HttpClientUtil.post(url, new Gson().toJson(requst), httpClientConfig);
        return response;
    }

    private String exexGetQuery(DatasourceRequest datasourceRequest){
        EsConfiguration esConfiguration = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), EsConfiguration.class);
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        if(StringUtils.isNotEmpty(esConfiguration.getEsUsername()) && StringUtils.isNotEmpty(esConfiguration.getEsPassword())){
            String auth = esConfiguration.getEsUsername() + ":" + esConfiguration.getEsPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
        }

        String  response = HttpClientUtil.get(esConfiguration.getUrl(), httpClientConfig);
        return response;
    }

}
