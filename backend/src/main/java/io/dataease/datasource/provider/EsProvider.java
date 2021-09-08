package io.dataease.datasource.provider;

import com.google.gson.Gson;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.datasource.dto.*;
import io.dataease.datasource.dto.es.EsReponse;
import io.dataease.datasource.dto.es.Requst;
import io.dataease.datasource.dto.es.RequstWithCursor;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DataEaseException;
import io.dataease.provider.es.EsQueryProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service("es")
public class EsProvider extends DatasourceProvider {

    private static Map<String, ComboPooledDataSource> jdbcConnection = new HashMap<>();
    private static int initPoolSize = 5;
    private static int maxConnections = 200;

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
            EsConfigDTO esConfigDTO = new Gson().fromJson(dsr.getDatasource().getConfiguration(), EsConfigDTO.class);

            HttpClientConfig httpClientConfig = new HttpClientConfig();
            String auth = esConfigDTO.getUsername() + ":" + esConfigDTO.getPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
            Requst requst = new Requst();
            requst.setQuery(dsr.getQuery());
            requst.setFetch_size(dsr.getFetchSize());
            String url = esConfigDTO.getUrl().endsWith("/") ? esConfigDTO.getUrl() + "_sql?format=json" : esConfigDTO.getUrl() + "/" + "_sql?format=json";
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
            DataEaseException.throwException(e);
        }
        return list;
    }

    @Override
    public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> list = new LinkedList<>();
        try {
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "_sql?format=json");
            list = fetchResult(response);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return list;
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
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "_sql?format=json");
            tableFileds = fetchResultField(response);
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

    @Override
    public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        try {
            String response = exexQuery(datasourceRequest, datasourceRequest.getQuery(), "_sql?format=json");
            result.put("dataList", fetchResult(response));
            result.put("fieldList", fetchResultField(response));
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
            String response = exexQuery(datasourceRequest, "show tables", "_sql?format=json");
            tables = fetchTables(response);
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
           if(row.length == 3 && row[1].equalsIgnoreCase("TABLE") && row[2].equalsIgnoreCase("INDEX")){
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
    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        List<TableFiled> tableFileds = new ArrayList<>();
        try {
            String response = exexQuery(datasourceRequest, "desc " + datasourceRequest.getTable(), "_sql?format=json");
            tableFileds = fetchResultField(response);
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return tableFileds;
    }

    @Override
    public void checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        getTables(datasourceRequest);
    }

    private String exexQuery(DatasourceRequest datasourceRequest, String sql, String uri){
        EsConfigDTO esConfigDTO = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), EsConfigDTO.class);

        HttpClientConfig httpClientConfig = new HttpClientConfig();
        String auth = esConfigDTO.getUsername() + ":" + esConfigDTO.getPassword();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        httpClientConfig.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));

        Requst requst = new Requst();
        requst.setQuery(sql);
        requst.setFetch_size(datasourceRequest.getFetchSize());
        String url = esConfigDTO.getUrl().endsWith("/") ? esConfigDTO.getUrl() + uri : esConfigDTO.getUrl() + "/" + uri;
        String  response = HttpClientUtil.post(url, new Gson().toJson(requst), httpClientConfig);
        return response;
    }

}
