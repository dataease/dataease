package io.dataease.utils;

import io.dataease.exception.DEException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.*;

import static io.dataease.result.ResultCode.SYSTEM_INNER_ERROR;

public class HttpClientUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final String HTTPS = "https";

    /**
     * 根据url构建HttpClient（区分http和https）
     *
     * @param url 请求地址
     * @return CloseableHttpClient实例
     */
    private static CloseableHttpClient buildHttpClient(String url) {
        if (StringUtils.isEmpty(url)) {
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: url 不能为空！");
        }
        try {
            if (url.startsWith(HTTPS)) {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, (X509Certificate[] x509Certificates, String s) -> true);
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"TLSv1.1", "TLSv1.2", "SSLv3"}, null, NoopHostnameVerifier.INSTANCE);
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", new PlainConnectionSocketFactory())
                        .register("https", socketFactory).build();
                HttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
                CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).build();
                return httpClient;
            } else {
                // http
                return HttpClientBuilder.create().build();
            }
        } catch (Exception e) {
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        }
    }

    public static boolean validateUrl(String url, HttpClientConfig config) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = buildHttpClient(url);
            HttpGet httpGet = new HttpGet(url);
            if (config == null) {
                config = new HttpClientConfig();
            }
            httpGet.setConfig(config.buildRequestConfig());

            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpGet.addHeader(key, header.get(key));
            }
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() >= 400) {
                String msg = EntityUtils.toString(response.getEntity(), config.getCharset());
                if (StringUtils.isEmpty(msg)) {
                    msg = "StatusCode: " + response.getStatusLine().getStatusCode();
                }
                throw new Exception(msg);
            }
            return true;
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                logger.error("HttpClient关闭连接失败", e);
            }
        }
    }

    /**
     * Get http请求
     *
     * @param url    请求地址
     * @param config 配置项，如果null则使用默认配置
     * @return 响应结果字符串
     */
    public static String get(String url, HttpClientConfig config) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = buildHttpClient(url);
            HttpGet httpGet = new HttpGet(url);

            if (config == null) {
                config = new HttpClientConfig();
            }
            httpGet.setConfig(config.buildRequestConfig());

            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpGet.addHeader(key, header.get(key));
            }
            HttpResponse response = httpClient.execute(httpGet);
            return getResponseStr(response, config);
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                logger.error("HttpClient关闭连接失败", e);
            }
        }
    }

    public static String patch(String url, String json, HttpClientConfig config) {
        CloseableHttpClient httpClient = buildHttpClient(url);
        HttpPatch httpPatch = new HttpPatch(url);
        config = config == null ? new HttpClientConfig() : config;
        try {
            httpPatch.setConfig(config.buildRequestConfig());
            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpPatch.addHeader(key, header.get(key));
            }
            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setText(json);
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
            HttpEntity requestEntity = entityBuilder.build();
            httpPatch.setEntity(requestEntity);
            HttpResponse response = httpClient.execute(httpPatch);
            return getResponseStr(response, config);
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                logger.error("HttpClient关闭连接失败", e);
            }
        }
    }

    /**
     * Post请求，请求内容必须为JSON格式的字符串
     *
     * @param url    请求地址
     * @param config 配置项，如果null则使用默认配置
     * @param json   JSON格式的字符串
     * @return 响应结果字符串
     */
    public static String post(String url, String json, HttpClientConfig config) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = buildHttpClient(url);
            HttpPost httpPost = new HttpPost(url);
            if (config == null) {
                config = new HttpClientConfig();
            }
            httpPost.setConfig(config.buildRequestConfig());
            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpPost.addHeader(key, header.get(key));
            }
            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setText(json);
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
            HttpEntity requestEntity = entityBuilder.build();
            httpPost.setEntity(requestEntity);

            HttpResponse response = httpClient.execute(httpPost);
            return getResponseStr(response, config);
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                logger.error("HttpClient关闭连接失败", e);
            }
        }
    }

    public static HttpResponse postWithHeaders(String url, String json, HttpClientConfig config) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = buildHttpClient(url);
            HttpPost httpPost = new HttpPost(url);
            if (config == null) {
                config = new HttpClientConfig();
            }
            httpPost.setConfig(config.buildRequestConfig());
            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpPost.addHeader(key, header.get(key));
            }
            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setText(json);
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
            HttpEntity requestEntity = entityBuilder.build();
            httpPost.setEntity(requestEntity);

            HttpResponse response = httpClient.execute(httpPost);
            return response;
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                logger.error("HttpClient关闭连接失败", e);
            }
        }
    }

    public static String put(String url, String json, HttpClientConfig config) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = buildHttpClient(url);
            HttpPut httpPut = new HttpPut(url);
            if (config == null) {
                config = new HttpClientConfig();
            }
            httpPut.setConfig(config.buildRequestConfig());
            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpPut.addHeader(key, header.get(key));
            }
            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setText(json);
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
            HttpEntity requestEntity = entityBuilder.build();
            httpPut.setEntity(requestEntity);

            HttpResponse response = httpClient.execute(httpPut);
            return getResponseStr(response, config);
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                logger.error("HttpClient关闭连接失败", e);
            }
        }
    }

    /**
     * Post请求，请求内容必须为JSON格式的字符串
     *
     * @param url  请求地址
     * @param json JSON格式的字符串
     * @return 响应结果字符串
     */
    public static String post(String url, String json) {
        return HttpClientUtil.post(url, json, null);
    }

    /**
     * Post请求，请求内容必须为键值对参数
     *
     * @param url    请求地址
     * @param config 配置项，如果null则使用默认配置
     * @param body   请求内容键值对参数
     * @return 响应结果字符串
     */
    public static String post(String url, Map<String, String> body, HttpClientConfig config) {
        try (CloseableHttpClient httpClient = buildHttpClient(url)) {
            HttpPost httpPost = new HttpPost(url);
            if (config == null) {
                config = new HttpClientConfig();
            }
            httpPost.setConfig(config.buildRequestConfig());
            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpPost.addHeader(key, header.get(key));
            }
            if (body != null && body.size() > 0) {
                List<NameValuePair> nvps = new ArrayList<>();
                for (String key : body.keySet()) {
                    nvps.add(new BasicNameValuePair(key, body.get(key)));
                }
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nvps, config.getCharset()));
                } catch (Exception e) {
                    logger.error("HttpClient转换编码错误", e);
                    throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient转换编码错误: " + e.getMessage());
                }
            }

            HttpResponse response = httpClient.execute(httpPost);
            return getResponseStr(response, config);
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        }
    }

    private static String getResponseStr(HttpResponse response, HttpClientConfig config) throws Exception {
        if (response.getStatusLine().getStatusCode() >= 400) {
            String msg = EntityUtils.toString(response.getEntity(), config.getCharset());
            if (StringUtils.isEmpty(msg)) {
                msg = "StatusCode: " + response.getStatusLine().getStatusCode();
            }
            throw new Exception(msg);
        }
        return EntityUtils.toString(response.getEntity(), config.getCharset());
    }

    public static byte[] downloadBytes(String url) {
        HttpClientConfig config = new HttpClientConfig();
        return HttpClientUtil.downFromRemote(url, config);
    }

    public static byte[] downFromRemote(String url, HttpClientConfig config) {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient httpClient = buildHttpClient(url);

        try {
            httpGet.setConfig(config.buildRequestConfig());
            Map<String, String> header = config.getHeader();
            Iterator var5 = header.keySet().iterator();

            while (var5.hasNext()) {
                String key = (String) var5.next();
                httpGet.addHeader(key, (String) header.get(key));
            }

            HttpResponse response = httpClient.execute(httpGet);
            InputStream inputStream = response.getEntity().getContent();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] var10 = outputStream.toByteArray();
            return var10;
        } catch (Exception var19) {
            logger.error("HttpClient查询失败", var19);
            throw new RuntimeException("HttpClient查询失败: " + var19.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception var18) {
                logger.error("HttpClient关闭连接失败", var18);
            }

        }
    }

    public static String postFile(String fileServer, byte[] bytes, String fileName, Map<String, String> param, HttpClientConfig config) {
        CloseableHttpClient httpClient = buildHttpClient(fileServer);
        HttpPost postRequest = new HttpPost(fileServer);
        if (config == null) {
            config = new HttpClientConfig();
        }

        postRequest.setConfig(config.buildRequestConfig());
        Map<String, String> header = config.getHeader();
        if (MapUtils.isNotEmpty(header)) {
            Iterator var8 = header.keySet().iterator();

            while (var8.hasNext()) {
                String key = (String) var8.next();
                postRequest.addHeader(key, (String) header.get(key));
            }
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(StandardCharsets.UTF_8);
        builder.addBinaryBody("image", bytes, ContentType.DEFAULT_BINARY, fileName);
        if (param != null) {
            Iterator var13 = param.entrySet().iterator();
            while (var13.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry) var13.next();
                builder.addTextBody((String) entry.getKey(), (String) entry.getValue());
            }
        }
        try {
            postRequest.setEntity((HttpEntity) builder.build());
            return getResponseStr(httpClient.execute(postRequest), config);
        } catch (Exception var11) {
            logger.error("HttpClient查询失败", var11);
            throw new RuntimeException("HttpClient查询失败: " + var11.getMessage());
        }
    }

    public static String upload(String url, byte[] bytes, String name, Map<String, String> paramMap, Map<String, Object> headMap) {
        HttpClientConfig config = new HttpClientConfig();
        addHead(config, headMap);
        return HttpClientUtil.postFile(url, bytes, name, paramMap, config);
    }

    private static void addHead(HttpClientConfig config, Map<String, Object> headMap) {
        if (MapUtils.isEmpty(headMap)) return;
        for (Map.Entry<String, Object> entry : headMap.entrySet()) {
            config.addHeader(entry.getKey(), entry.getValue().toString());
        }
    }

    public static String delete(String url, HttpClientConfig config) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = buildHttpClient(url);
            HttpDelete httpDelete = new HttpDelete(url);

            if (config == null) {
                config = new HttpClientConfig();
            }
            httpDelete.setConfig(config.buildRequestConfig());

            Map<String, String> header = config.getHeader();
            for (String key : header.keySet()) {
                httpDelete.addHeader(key, header.get(key));
            }
            HttpResponse response = httpClient.execute(httpDelete);
            return getResponseStr(response, config);
        } catch (Exception e) {
            logger.error("HttpClient查询失败", e);
            throw new DEException(SYSTEM_INNER_ERROR.code(), "HttpClient查询失败: " + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                logger.error("HttpClient关闭连接失败", e);
            }
        }
    }
}
