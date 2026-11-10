package io.dataease.ai.support;

import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.manage.AiConfigurationManage.Connection;
import jakarta.annotation.PreDestroy;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;

@Component
public class AiTransport {
    private final AiOutboundPolicy outbound;
    private final AiAdmission admission;
    private final ObjectMapper json;
    private final CloseableHttpClient http;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(40, 40, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(40), runnable -> { Thread t = new Thread(runnable, "de-ai-http"); t.setDaemon(true); return t; }, new ThreadPoolExecutor.AbortPolicy());
    private final ScheduledExecutorService deadlines = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "de-ai-deadlines"); t.setDaemon(true); return t; });
    private static final int MAX_RESPONSE = 4 * 1024 * 1024;
    public AiTransport(AiOutboundPolicy outbound, AiAdmission admission, ObjectMapper json) { this.outbound = outbound; this.admission = admission; this.json = json;
        var manager = PoolingHttpClientConnectionManagerBuilder.create().setMaxConnTotal(40).setMaxConnPerRoute(40)
                .setDnsResolver(new DnsResolver() {
                    public InetAddress[] resolve(String host) throws UnknownHostException { return outbound.resolve(host); }
                    public String resolveCanonicalHostname(String host) { return host; }
                }).setDefaultConnectionConfig(org.apache.hc.client5.http.config.ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(300)).setSocketTimeout(Timeout.ofSeconds(300))
                        .setTimeToLive(TimeValue.ofMinutes(5)).setValidateAfterInactivity(TimeValue.ofSeconds(5)).build()).build();
        http = HttpClients.custom().setConnectionManager(manager).disableAutomaticRetries().disableRedirectHandling()
                .disableCookieManagement().evictExpiredConnections().evictIdleConnections(TimeValue.ofSeconds(60)).build();
        // Spring AI 1.1.8 在空 choices 分支 WARN 完整 Prompt；精确禁用此 logger，统一返回 DE 安全错误。
        org.springframework.boot.logging.LoggingSystem.get(getClass().getClassLoader()).setLogLevel(
                "org.springframework.ai.openai.OpenAiChatModel", org.springframework.boot.logging.LogLevel.OFF); }
    public ChatResponse call(Connection connection, CoreAiSetting settings, Prompt prompt, boolean background, long deadline, Runnable recheck) {
        return execute(connection, settings, background, deadline, recheck, client -> {
            OpenAiApi api = OpenAiApi.builder().baseUrl(connection.url()).apiKey(connection.key())
                    .completionsPath(outbound.completionPath(connection.url())).restClientBuilder(client).responseErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
                        @Override public boolean hasError(ClientHttpResponse response) throws IOException { return response.getStatusCode().value() >= 300; }
                        @Override public void handleError(URI uri, HttpMethod method, ClientHttpResponse response) throws IOException { throw providerError(response); }
                    }).build();
            ChatResponse response = OpenAiChatModel.builder().openAiApi(api).retryTemplate(RetryTemplate.builder().maxAttempts(1).build())
                    .toolExecutionEligibilityPredicate((options, output) -> false).build().call(prompt);
            if (response == null || response.getResults().isEmpty() || response.getResult().getOutput().getText() == null
                    || response.getResult().getOutput().getText().isBlank()) throw AiErrors.error("response_invalid");
            for (var result : response.getResults()) if (result.getOutput().hasToolCalls() || "length".equals(result.getMetadata().getFinishReason())) throw AiErrors.error("response_invalid");
            return response;
        });
    }
    public List<String> discover(Connection connection, CoreAiSetting settings, long deadline, Runnable recheck) {
        return execute(connection, settings, false, deadline, recheck, builder -> {
            String result = builder.baseUrl(connection.url()).defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + connection.key()).build()
                    .get().uri(outbound.discoveryPath(connection.url())).retrieve().body(String.class);
            try {
                var array = json.readTree(result).get("data");
                if (array == null || !array.isArray()) throw AiErrors.error("response_invalid");
                java.util.ArrayList<String> ids = new java.util.ArrayList<>();
                for (var item : array) if (item.path("id").isTextual() && item.path("id").asText().length() <= 256) ids.add(item.path("id").asText());
                return ids.stream().distinct().limit(2000).toList();
            } catch (IOException e) { throw AiErrors.error("response_invalid"); }
        });
    }
    private interface Work<T> { T run(RestClient.Builder builder); }
    private <T> T execute(Connection c, CoreAiSetting s, boolean background, long deadline, Runnable recheck, Work<T> work) {
        long remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0) throw AiErrors.error("timeout");
        Future<T> future;
        RequestCancellation cancellation = new RequestCancellation();
        try {
            future = executor.submit(() -> {
                try {
                    recheck.run();
                    outbound.normalize(c.url());
                    long budget = deadline - System.currentTimeMillis();
                    if (budget <= 0 || Thread.currentThread().isInterrupted()) throw AiErrors.error("timeout");
                    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(http) {
                        @Override protected org.apache.hc.core5.http.ClassicHttpRequest createHttpUriRequest(HttpMethod method, URI uri) {
                            HttpUriRequestBase request = new HttpUriRequestBase(method.name(), uri);
                            request.setConfig(RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofMilliseconds(budget))
                                    .setResponseTimeout(Timeout.ofMilliseconds(budget)).setHardCancellationEnabled(true).build());
                            cancellation.attach(request);
                            return request;
                        }
                    };
                    ScheduledFuture<?> abort = deadlines.schedule(cancellation::cancel,
                            Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
                    try {
                        RestClient.Builder builder = RestClient.builder().requestFactory(factory)
                                .defaultStatusHandler(status -> status.value() >= 300, (request, response) -> { throw providerError(response); })
                                .requestInterceptor((request, bytes, execution) -> {
                            if (bytes.length > 4 * 1024 * 1024) throw AiErrors.error("input_limit");
                            recheck.run();
                            if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
                            AiAdmission.Permit permit = admission.acquire(s, background, deadline);
                            try { recheck.run(); return bounded(execution.execute(request, bytes), permit, deadline); }
                            catch (Throwable e) { permit.close(); throw e; }
                        });
                        T result = work.run(builder);
                        // 在无请求持久化上下文的传输线程复核，避免请求级实体缓存延续旧配置/资源授权。
                        recheck.run();
                        if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
                        return result;
                    } finally { abort.cancel(false); }
                } finally {
                    io.dataease.permission.util.V3UserUtil.clear();
                    org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
                    org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext();
                    org.slf4j.MDC.clear();
                }
            });
        } catch (RejectedExecutionException e) { throw AiErrors.error("concurrency"); }
        try { return future.get(remaining, TimeUnit.MILLISECONDS); }
        catch (TimeoutException e) { cancellation.cancel(); future.cancel(true); throw AiErrors.error("timeout"); }
        catch (InterruptedException e) { cancellation.cancel(); future.cancel(true); Thread.currentThread().interrupt(); throw AiErrors.error("cancelled"); }
        catch (ExecutionException e) {
            if (cancellation.cancelled() || System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
            if (e.getCause() instanceof io.dataease.exception.DEException known) throw known;
            org.slf4j.LoggerFactory.getLogger(AiTransport.class).warn("AI transport failure category: {}", e.getCause().getClass().getName());
            if (e.getCause() instanceof NoClassDefFoundError missing && missing.getMessage() != null && missing.getMessage().matches("[A-Za-z0-9_/$.-]+")) {
                org.slf4j.LoggerFactory.getLogger(AiTransport.class).warn("AI runtime dependency unavailable: {}", missing.getMessage());
            }
            if (e.getCause() instanceof LinkageError) throw AiErrors.error("configuration_unavailable");
            // 不将供应商正文、URL、认证头或异常 cause 透传到管理 API。
            throw AiErrors.error(connectionFailure(e.getCause()));
        }
    }
    // 注册与取消使用同一把短锁，覆盖超时发生在请求创建之前的竞态；只取消当前请求。
    private static final class RequestCancellation {
        private HttpUriRequestBase request;
        private boolean cancelled;
        synchronized void attach(HttpUriRequestBase request) {
            this.request = request;
            if (cancelled) request.cancel();
        }
        synchronized void cancel() {
            cancelled = true;
            if (request != null) request.cancel();
        }
        synchronized boolean cancelled() { return cancelled; }
    }
    private static String connectionFailure(Throwable failure) {
        var causes = new java.util.ArrayList<Throwable>();
        var seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Throwable, Boolean>());
        for (Throwable cause = failure; cause != null && seen.add(cause); cause = cause.getCause()) causes.add(cause);
        // 仅映射异常类型，不透传可能包含地址或凭据的异常消息。
        if (causes.stream().anyMatch(java.net.UnknownHostException.class::isInstance)) return "dns_failed";
        if (causes.stream().anyMatch(javax.net.ssl.SSLException.class::isInstance)) return "tls_failed";
        if (causes.stream().anyMatch(java.net.SocketTimeoutException.class::isInstance)) return "timeout";
        if (causes.stream().anyMatch(cause -> cause instanceof java.net.SocketException
                || cause instanceof org.springframework.web.client.ResourceAccessException)) return "connection_failed";
        return "upstream_failed";
    }
    private static RuntimeException providerError(ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        if (status == 400 || status == 422) {
            byte[] body = response.getBody().readNBytes(65537);
            if (body.length <= 65536) {
                try {
                    var root = new ObjectMapper().readTree(body);
                    var error = root == null ? com.fasterxml.jackson.databind.node.NullNode.getInstance() : root.path("error");
                    if ("unsupported_parameter".equals(error.path("code").asText()) && "max_tokens".equals(error.path("param").asText())) return AiErrors.error("max_tokens_unsupported");
                } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) { /* 未识别的正文不进入日志或异常。 */ }
            }
        }
        return AiErrors.error(switch (status) {
            case 401, 403 -> "provider_authentication";
            case 402 -> "provider_balance";
            case 429 -> "provider_rate_limit";
            case 400, 404, 405, 422 -> "provider_rejected";
            default -> "upstream_failed";
        });
    }
    private ClientHttpResponse bounded(ClientHttpResponse response, AiAdmission.Permit permit, long deadline) throws IOException {
        return new ClientHttpResponse() {
            private InputStream body;
            public HttpStatusCode getStatusCode() throws IOException { return response.getStatusCode(); }
            public String getStatusText() throws IOException { return response.getStatusText(); }
            public HttpHeaders getHeaders() { return response.getHeaders(); }
            public InputStream getBody() throws IOException {
                if (body == null) body = new FilterInputStream(response.getBody()) {
                    private long count;
                    private void check(int amount) throws IOException {
                        count += Math.max(0, amount);
                        if (count > MAX_RESPONSE) throw AiErrors.error("response_invalid");
                        if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
                        if (Thread.currentThread().isInterrupted()) throw AiErrors.error("cancelled");
                    }
                    public int read() throws IOException { int value = in.read(); check(value < 0 ? 0 : 1); return value; }
                    public int read(byte[] b, int off, int len) throws IOException { int read = in.read(b, off, Math.min(len, 8192)); check(read); return read; }
                };
                return body;
            }
            public void close() { try { response.close(); } finally { permit.close(); } }
        };
    }
    @PreDestroy public void close() { executor.shutdownNow(); deadlines.shutdownNow(); http.close(org.apache.hc.core5.io.CloseMode.IMMEDIATE); }
}
