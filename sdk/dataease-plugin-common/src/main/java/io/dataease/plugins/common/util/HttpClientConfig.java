package io.dataease.plugins.common.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;

import java.util.HashMap;
import java.util.Map;

public class HttpClientConfig {

    // 字符集
    private String charset = "UTF-8";

    // 请求头
    private Map<String, String> header = new HashMap<>();

    // 设置连接超时时间，单位毫秒
    private int connectTimeout = 30000;
    // 设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的
    private int connectionRequestTimeout = 30000;
    // 请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用
    private int socketTimeout = 60000;

    public RequestConfig buildRequestConfig() {
        Builder builder = RequestConfig.custom();
        builder.setConnectTimeout(connectTimeout);
        builder.setConnectionRequestTimeout(connectionRequestTimeout);
        builder.setSocketTimeout(socketTimeout);
        return builder.build();
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void addHeader(String key, String value) {
        header.put(key, value);
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int cocketTimeout) {
        this.socketTimeout = cocketTimeout;
    }

}
