package io.dataease.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * 同步执行器的公共请求客户端，开启鉴权时使用同一条路径及同一份请求字节签名
 */
@Component
public class SyncExecutorHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncExecutorHttpClient.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    @Value("${task.executor.auth.enabled:${DE_SYNC_AUTH_ENABLED:false}}")
    private boolean authEnabled;

    @Value("${task.executor.auth.secret:${DE_SYNC_AUTH_SECRET:}}")
    private String secret;

    public String post(String url, String body) {
        return post(url, body, new HttpClientConfig());
    }

    public String post(String url, String body, HttpClientConfig config) {
        if (body == null) throw failure(url, "同步执行器请求序列化失败", false);
        return request("POST", url, body.getBytes(StandardCharsets.UTF_8), config);
    }

    public String get(String url, HttpClientConfig config) {
        return request("GET", url, new byte[0], config);
    }

    private String request(String method, String url, byte[] body, HttpClientConfig config) {
        URI uri;
        try {
            uri = URI.create(url);
            if (uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || uri.getRawUserInfo() != null || uri.getPort() > 65535
                    || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException();
            }
            uri = URIUtils.rewriteURI(uri, URIUtils.extractHost(uri), URIUtils.NORMALIZE);
        } catch (Exception e) {
            throw failure(url, "同步执行器地址无效，请检查协议、主机和端口，不支持查询参数、片段或内嵌凭据", false);
        }
        boolean execution = "POST".equals(method) && uri.getPath().endsWith("/actuator/executed-task");
        try {
            HttpRequestBase request;
            if ("POST".equals(method)) {
                HttpPost post = new HttpPost(uri);
                post.setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_JSON));
                request = post;
            } else {
                request = new HttpGet(uri);
            }
            request.setConfig(RequestConfig.copy(config.buildRequestConfig()).setNormalizeUri(false).build());
            if (authEnabled) {
                byte[] key;
                try {
                    key = Base64.getDecoder().decode(secret == null ? "" : secret);
                    if (key.length != 32) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    throw failure(url, "主服务未配置有效的同步执行器鉴权密钥，请检查 DE_SYNC_AUTH_SECRET", false);
                }
                String timestamp = Long.toString(Instant.now().getEpochSecond());
                String nonce = UUID.randomUUID().toString();
                String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
                String canonical = String.join("\n", "v1", method, uri.getRawPath(), timestamp, nonce, digest);
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(key, "HmacSHA256"));
                request.setHeader("X-DE-Sync-Version", "v1");
                request.setHeader("X-DE-Sync-Timestamp", timestamp);
                request.setHeader("X-DE-Sync-Nonce", nonce);
                request.setHeader("X-DE-Sync-Signature", Base64.getEncoder().encodeToString(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8))));
            }
            // 保留系统 TLS 校验，不允许重定向或自动重发可能已执行的任务
            try (var client = HttpClients.custom().disableRedirectHandling().disableAutomaticRetries().build();
                 var transfer = new RemoteTransfer(MAX_RESPONSE_BYTES, config.getResponseTimeout(), request::abort);
                 var response = client.execute(request)) {
                int status = response.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    String code = "";
                    if (response.getEntity() != null) {
                        byte[] error = transfer.wrap(response.getEntity().getContent()).readNBytes(4097);
                        if (error.length <= 4096) {
                            try {
                                var json = JSON.readTree(error);
                                if (json.path("code").asInt() == status) code = json.path("message").asText();
                            } catch (Exception ignored) {
                                // 不向页面或日志反射任意远端响应
                            }
                        }
                    }
                    throw failure(url, responseError(status, code), execution && status >= 500 && !code.startsWith("SYNC_AUTH_"));
                }
                if (response.getEntity() == null) return "";
                byte[] data = transfer.wrap(response.getEntity().getContent()).readAllBytes();
                return new String(data, StandardCharsets.UTF_8);
            }
        } catch (RequestException e) {
            throw e;
        } catch (java.net.UnknownHostException e) {
            throw failure(url, "无法解析同步执行器主机，请检查地址和 DNS 配置", false);
        } catch (java.net.ConnectException | java.net.NoRouteToHostException e) {
            throw failure(url, "无法连接同步执行器，请检查服务、地址端口和网络", false);
        } catch (javax.net.ssl.SSLException e) {
            throw failure(url, "同步执行器 TLS 校验失败，请检查证书和 Java 信任库", false);
        } catch (Exception e) {
            throw failure(url, execution ? "同步执行器通信异常，任务是否已执行尚未确认，请检查实际状态，不要直接重发"
                    : "同步执行器通信异常，请检查服务状态、网络及超时配置后重试查询", execution);
        }
    }

    private static String responseError(int status, String code) {
        if (status == 401 && "SYNC_AUTH_EXPIRED".equals(code)) return "同步执行器拒绝过期请求，请检查两端系统时间";
        if (status == 401 && "SYNC_AUTH_INVALID".equals(code)) return "同步执行器验签失败，请检查两端密钥、路径及代理转发配置";
        if (status == 409 && "SYNC_AUTH_REPLAY".equals(code)) return "同步执行器拒绝重复请求，请重新查询，不要直接重发执行任务";
        if (status == 413) return "同步执行器请求过大，请检查任务或依赖文件大小及对应的鉴权请求体上限";
        if (status == 503 && "SYNC_AUTH_BUSY".equals(code)) return "同步执行器请求处理繁忙，请稍后重试查询";
        if (status == 503 && "SYNC_AUTH_CAPACITY".equals(code)) return "同步执行器防重放缓存已满，请检查请求量及缓存容量";
        if (status == 404) return "同步执行器接口不存在（HTTP 404），请检查地址和两端版本";
        return "同步执行器请求失败（HTTP " + status + "），请检查目标服务、访问限制及两端版本";
    }

    /**
     * 页面仅展示原因和诊断编号，日志中的目标地址去除凭据、路径和查询参数
     */
    public static RequestException failure(String url, String message, boolean executionUncertain) {
        String id = UUID.randomUUID().toString();
        String target = "无效地址";
        try {
            URI uri = URI.create(url);
            if (uri.getHost() != null) target = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null).toASCIIString();
        } catch (Exception ignored) {
            // 不记录可能包含凭据的原始地址
        }
        LOGGER.warn("同步执行器请求失败 diagnosticId={} target={} reason={}", id, target, message);
        return new RequestException(message + "（诊断编号：" + id + "）", executionUncertain);
    }

    /**
     * 区分确定未执行和无法确认执行结果，调用方不得自动重发结果不明的任务
     */
    public static class RequestException extends RuntimeException {
        private final boolean executionUncertain;

        public RequestException(String message, boolean executionUncertain) {
            super(message);
            this.executionUncertain = executionUncertain;
        }

        public boolean isExecutionUncertain() {
            return executionUncertain;
        }
    }
}
