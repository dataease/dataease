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
            for (var result : response.getResults()) if (result.getOutput().hasToolCalls() || "length".equalsIgnoreCase(result.getMetadata().getFinishReason())) throw AiErrors.error("response_invalid");
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
                    return perform(c, s, background, deadline, recheck, cancellation, work);
                } finally {
                    clearWorker();
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
    private <T> T perform(Connection c, CoreAiSetting s, boolean background, long deadline, Runnable recheck,
                          RequestCancellation cancellation, Work<T> work) {
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
    }
    /** One controlled interactive upstream stream. No default WebClient, retry or model-side tool execution. */
    public final class Stream implements AutoCloseable {
        private final RequestCancellation cancellation = new RequestCancellation();
        private Future<?> future;
        private boolean started, closed;
        public synchronized void start(Connection c, CoreAiSetting s, Prompt prompt, long deadline, Runnable recheck, Runnable progress,
                                       java.util.function.Consumer<String> delta, Runnable completed,
                                       java.util.function.Consumer<Throwable> failed) {
            if (started || closed) throw AiErrors.error("cancelled");
            started = true;
            Runnable guard = () -> {
                if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
                if (cancellation.cancelled() || Thread.currentThread().isInterrupted()) throw AiErrors.error("cancelled");
                progress.run();
            };
            try {
                future = executor.submit(() -> {
                    try {
                        perform(c, s, false, deadline, recheck, cancellation, builder -> {
                            var options = (org.springframework.ai.openai.OpenAiChatOptions) prompt.getOptions();
                            var payload = json.createObjectNode();
                            payload.put("model", options.getModel()); payload.put("stream", true); payload.put("n", 1);
                            if (options.getMaxTokens() != null) payload.put("max_tokens", options.getMaxTokens());
                            if (options.getMaxCompletionTokens() != null) payload.put("max_completion_tokens", options.getMaxCompletionTokens());
                            if (options.getTemperature() != null) payload.put("temperature", options.getTemperature());
                            if (options.getTopP() != null) payload.put("top_p", options.getTopP());
                            if (options.getFrequencyPenalty() != null) payload.put("frequency_penalty", options.getFrequencyPenalty());
                            if (options.getPresencePenalty() != null) payload.put("presence_penalty", options.getPresencePenalty());
                            if (options.getStopSequences() != null) payload.set("stop", json.valueToTree(options.getStopSequences()));
                            if (options.getResponseFormat() != null) payload.set("response_format", json.valueToTree(options.getResponseFormat()));
                            var messages = payload.putArray("messages");
                            for (var message : prompt.getInstructions()) {
                                var item = messages.addObject(); item.put("role", message.getMessageType().getValue()); item.put("content", message.getText());
                            }
                            return builder.baseUrl(c.url()).defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + c.key()).build()
                                    .post().uri(outbound.completionPath(c.url())).contentType(MediaType.APPLICATION_JSON).body(payload)
                                    .exchange((request, response) -> {
                                        if (response.getStatusCode().value() >= 300) throw providerError(response);
                                        MediaType type = response.getHeaders().getContentType();
                                        if (type == null || !MediaType.TEXT_EVENT_STREAM.isCompatibleWith(type)) throw AiErrors.error("unsupported");
                                        readEvents(response.getBody(), options.getMaxTokens() != null ? options.getMaxTokens() : options.getMaxCompletionTokens(), guard, delta);
                                        return Boolean.TRUE;
                                    });
                        });
                        if (!cancellation.cancelled()) completed.run();
                    } catch (Throwable failure) {
                        if (!cancellation.cancelled()) failed.accept(safeFailure(failure, deadline));
                    } finally { clearWorker(); }
                });
            } catch (RejectedExecutionException failure) { throw AiErrors.error("concurrency"); }
        }
        @Override public synchronized void close() {
            closed = true; cancellation.cancel(); if (future != null) { future.cancel(true); executor.purge(); }
        }
    }
    /** One model step, scheduled directly: its completion may enqueue the next step, never await a child worker. */
    public final class ToolStream implements AutoCloseable {
        private final RequestCancellation cancellation = new RequestCancellation();
        private Future<?> future;
        private boolean closed;
        public synchronized void start(Connection connection, CoreAiSetting settings, com.fasterxml.jackson.databind.node.ObjectNode payload,
                                       long deadline, int allocation, AiToolProtocol.Budget budget, Runnable recheck, Runnable progress,
                                       java.util.function.Consumer<String> delta, java.util.function.Consumer<AiToolProtocol.Step> completed,
                                       java.util.function.Consumer<Throwable> failed) {
            if (closed || future != null) throw AiErrors.error("cancelled");
            Runnable guard = () -> {
                if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
                if (cancellation.cancelled() || Thread.currentThread().isInterrupted()) throw AiErrors.error("cancelled");
                progress.run();
            };
            try {
                future = executor.submit(() -> {
                    try {
                        var result = perform(connection, settings, false, deadline, recheck, cancellation, builder ->
                            builder.baseUrl(connection.url()).defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + connection.key()).build()
                                .post().uri(outbound.completionPath(connection.url())).contentType(MediaType.APPLICATION_JSON).body(payload)
                                .exchange((request, response) -> {
                                    if (response.getStatusCode().value() >= 300) throw providerError(response);
                                    var type = response.getHeaders().getContentType();
                                    if (type == null || !MediaType.TEXT_EVENT_STREAM.isCompatibleWith(type)) throw AiErrors.error("unsupported");
                                    return readToolEvents(response.getBody(), allocation, budget, guard, delta);
                                }));
                        guard.run(); if (!cancellation.cancelled()) completed.accept(result);
                    } catch (Throwable failure) { if (!cancellation.cancelled()) failed.accept(safeFailure(failure, deadline)); }
                    finally { clearWorker(); }
                });
            } catch (RejectedExecutionException failure) { throw AiErrors.error("concurrency"); }
        }
        @Override public synchronized void close() {
            closed = true; cancellation.cancel(); if (future != null) { future.cancel(true); executor.purge(); }
        }
    }
    public ToolStream toolStream() { return new ToolStream(); }
    private AiToolProtocol.Step readToolEvents(InputStream input, int allocation, AiToolProtocol.Budget budget, Runnable guard,
                                               java.util.function.Consumer<String> delta) throws IOException {
        var bounded = new FilterInputStream(input) {
            @Override public int read() throws IOException { int value = super.read(); if (value != -1) budget.raw(1); return value; }
            @Override public int read(byte[] bytes, int offset, int length) throws IOException { int count = in.read(bytes, offset, length); if (count > 0) budget.raw(count); return count; }
        };
        var decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
        var reader = new InputStreamReader(bounded, decoder);
        var line = new StringBuilder(); var data = new StringBuilder(); var text = new StringBuilder(); var arguments = new StringBuilder();
        String id = null, name = null, callType = null, finish = null;
        Long usage = null; int frames = 0; long generated = 0; boolean previousCr = false, sawTool = false;
        for (int value; (value = reader.read()) != -1;) {
            guard.run(); char ch = (char) value;
            if (previousCr && ch == '\n') { previousCr = false; continue; } previousCr = ch == '\r';
            if (ch != '\n' && ch != '\r') { if (line.length() >= 65536) throw AiErrors.error("response_invalid"); line.append(ch); continue; }
            String field = line.toString(); line.setLength(0);
            if (!field.isEmpty()) {
                if (field.startsWith("data:")) {
                    String part = field.substring(5); if (part.startsWith(" ")) part = part.substring(1);
                    if (data.length() + part.length() > 65536) throw AiErrors.error("response_invalid"); data.append(part).append('\n');
                }
                continue;
            }
            if (data.isEmpty()) continue;
            if (++frames > 20000) throw AiErrors.error("response_invalid"); budget.frame();
            String event = data.substring(0, data.length() - 1); data.setLength(0); guard.run();
            if ("[DONE]".equals(event)) {
                if ("stop".equals(finish) && !sawTool && !text.toString().isBlank()) return new AiToolProtocol.Step(text.toString(), List.of());
                if ("tool_calls".equals(finish) && id != null && name != null && "function".equals(callType) && !arguments.isEmpty())
                    return new AiToolProtocol.Step(text.toString(), List.of(new AiToolProtocol.Call(id, name, arguments.toString())));
                throw AiErrors.error("response_invalid");
            }
            com.fasterxml.jackson.databind.JsonNode root;
            try (var parser = json.getFactory().createParser(event)) {
                parser.enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
                root = json.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(parser);
            } catch (com.fasterxml.jackson.core.JsonProcessingException invalid) { throw AiErrors.error("response_invalid"); }
            if (root == null || !root.isObject() || root.has("error") || !root.path("choices").isArray()) throw AiErrors.error("response_invalid");
            if (root.hasNonNull("usage")) {
                var count = root.path("usage").path("completion_tokens");
                if (!root.path("usage").isObject() || !count.isIntegralNumber() || !count.canConvertToLong() || count.asLong() < 0) throw AiErrors.error("response_invalid");
                if (count.asLong() > allocation) throw AiErrors.error("output_limit");
                if (usage != null && usage != count.asLong()) throw AiErrors.error("response_invalid"); usage = count.asLong();
            }
            var choices = root.path("choices"); if (choices.isEmpty() && root.path("usage").isObject()) continue;
            if (choices.size() != 1 || !choices.get(0).path("index").isInt() || choices.get(0).path("index").asInt() != 0 || finish != null) throw AiErrors.error("response_invalid");
            var choice = choices.get(0); var chunk = choice.path("delta"); if (!chunk.isObject()) throw AiErrors.error("response_invalid");
            var names = chunk.fieldNames();
            while (names.hasNext()) { String key = names.next(); if (!java.util.Set.of("role", "content", "reasoning", "reasoning_content", "tool_calls").contains(key) && !chunk.path(key).isNull()) throw AiErrors.error("unsupported"); }
            if (chunk.hasNonNull("role") && !"assistant".equals(chunk.path("role").asText())) throw AiErrors.error("response_invalid");
            for (String key : List.of("content", "reasoning", "reasoning_content")) {
                if (!chunk.hasNonNull(key)) continue;
                if (!chunk.path(key).isTextual()) throw AiErrors.error("response_invalid");
                String part = chunk.path(key).asText(); generated += part.length(); budget.generated(part.length());
                if (generated > Math.min(262144L, allocation * 16L)) throw AiErrors.error("output_limit");
                if ("content".equals(key) && !part.isEmpty()) { text.append(part); delta.accept(part); }
            }
            if (chunk.hasNonNull("tool_calls")) {
                sawTool = true;
                var calls = chunk.path("tool_calls");
                // Deliberate first capability: one serial tool per model step. Never mis-correlate a batch.
                if (!calls.isArray() || calls.size() != 1 || !calls.get(0).path("index").isInt() || calls.get(0).path("index").asInt() != 0) throw AiErrors.error("unsupported");
                var call = calls.get(0);
                for (String generatedField : List.of("id", "type")) if (call.path(generatedField).isTextual()) budget.generated(call.path(generatedField).asText().length());
                if (call.path("function").path("name").isTextual()) budget.generated(call.path("function").path("name").asText().length());
                rejectUnknown(call, java.util.Set.of("index", "id", "type", "function"));
                if (call.hasNonNull("id")) id = stable(id, call.path("id"), "[A-Za-z0-9_-]{1,128}");
                if (call.hasNonNull("type")) callType = stable(callType, call.path("type"), "function");
                if (call.hasNonNull("function")) {
                    var function = call.path("function"); if (!function.isObject()) throw AiErrors.error("response_invalid");
                    rejectUnknown(function, java.util.Set.of("name", "arguments"));
                    if (function.hasNonNull("name")) name = stable(name, function.path("name"), "[A-Za-z0-9_-]{1,64}");
                    if (function.hasNonNull("arguments")) {
                        var part = function.path("arguments"); if (!part.isTextual() || arguments.length() + part.asText().length() > AiToolProtocol.MAX_ARGUMENTS) throw AiErrors.error("response_invalid");
                        arguments.append(part.asText()); generated += part.asText().length(); budget.generated(part.asText().length());
                        if (generated > allocation * 16L) throw AiErrors.error("output_limit");
                    }
                }
            }
            if (choice.hasNonNull("finish_reason")) {
                finish = choice.path("finish_reason").asText();
                if (!java.util.Set.of("stop", "tool_calls").contains(finish)) throw AiErrors.error("response_invalid");
            }
        }
        throw AiErrors.error("response_invalid");
    }
    private void rejectUnknown(com.fasterxml.jackson.databind.JsonNode node, java.util.Set<String> allowed) {
        var names = node.fieldNames(); while (names.hasNext()) if (!allowed.contains(names.next())) throw AiErrors.error("unsupported");
    }
    private String stable(String previous, com.fasterxml.jackson.databind.JsonNode value, String pattern) {
        if (!value.isTextual() || !value.asText().matches(pattern) || previous != null && !previous.equals(value.asText())) throw AiErrors.error("response_invalid");
        return value.asText();
    }
    public Stream stream() { return new Stream(); }
    private RuntimeException safeFailure(Throwable failure, long deadline) {
        if (System.currentTimeMillis() >= deadline) return AiErrors.error("timeout");
        if (failure instanceof io.dataease.exception.DEException known) return known;
        return AiErrors.error(connectionFailure(failure));
    }
    private static void clearWorker() {
        io.dataease.permission.util.V3UserUtil.clear();
        io.dataease.utils.CommunityUtils.clear();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext();
        org.slf4j.MDC.clear();
    }
    /** Bounded SSE framing, incrementally decoded UTF-8 including split multibyte characters. */
    private void readEvents(InputStream input, int maxTokens, Runnable guard, java.util.function.Consumer<String> delta) throws IOException {
        var decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT).onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
        var reader = new InputStreamReader(input, decoder);
        StringBuilder line = new StringBuilder(), data = new StringBuilder();
        boolean finished = false, text = false, previousCr = false;
        int frames = 0;
        long characters = 0;
        for (int value; (value = reader.read()) != -1;) {
            guard.run();
            char ch = (char) value;
            if (previousCr && ch == '\n') { previousCr = false; continue; }
            previousCr = ch == '\r';
            if (ch != '\n' && ch != '\r') {
                if (line.length() >= 65536) throw AiErrors.error("response_invalid");
                line.append(ch); continue;
            }
            String field = line.toString(); line.setLength(0);
            if (!field.isEmpty()) {
                if (field.startsWith("data:")) {
                    String part = field.substring(5); if (part.startsWith(" ")) part = part.substring(1);
                    if (data.length() + part.length() > 65536) throw AiErrors.error("response_invalid");
                    data.append(part).append('\n');
                }
                continue;
            }
            if (data.isEmpty()) continue;
            if (++frames > 20000) throw AiErrors.error("response_invalid");
            String event = data.substring(0, data.length() - 1); data.setLength(0);
            guard.run();
            if ("[DONE]".equals(event)) {
                if (!finished || !text) throw AiErrors.error("response_invalid");
                return;
            }
            com.fasterxml.jackson.databind.JsonNode root;
            try { root = json.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(event); }
            catch (com.fasterxml.jackson.core.JsonProcessingException invalid) { throw AiErrors.error("response_invalid"); }
            if (root == null || !root.isObject() || root.has("error") || !root.path("choices").isArray()) throw AiErrors.error("response_invalid");
            if (root.path("usage").path("completion_tokens").canConvertToLong() && root.path("usage").path("completion_tokens").asLong() > maxTokens) throw AiErrors.error("output_limit");
            var choices = root.path("choices");
            if (choices.isEmpty() && root.path("usage").isObject()) continue;
            if (choices.size() != 1 || !choices.get(0).path("index").isInt() || choices.get(0).path("index").asInt() != 0 || finished)
                throw AiErrors.error("response_invalid");
            var choice = choices.get(0); var chunk = choice.path("delta");
            if (!chunk.isObject()) throw AiErrors.error("response_invalid");
            var names = chunk.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                if (!java.util.Set.of("role", "content", "reasoning_content", "reasoning").contains(name) && !chunk.path(name).isNull()) throw AiErrors.error("unsupported");
            }
            if (chunk.hasNonNull("reasoning_content") && !chunk.path("reasoning_content").isTextual()
                    || chunk.hasNonNull("reasoning") && !chunk.path("reasoning").isTextual()) throw AiErrors.error("response_invalid");
            if (chunk.hasNonNull("role") && !"assistant".equals(chunk.path("role").asText())) throw AiErrors.error("response_invalid");
            if (chunk.hasNonNull("content")) {
                if (!chunk.path("content").isTextual()) throw AiErrors.error("response_invalid");
                String content = chunk.path("content").asText();
                if (!content.isEmpty()) {
                    characters += content.length();
                    if (characters > Math.min(262144L, maxTokens * 16L)) throw AiErrors.error("output_limit");
                    guard.run(); delta.accept(content); text |= !content.isBlank();
                }
            }
            if (choice.hasNonNull("finish_reason")) {
                if (!"stop".equals(choice.path("finish_reason").asText())) throw AiErrors.error("response_invalid");
                finished = true;
            }
        }
        throw AiErrors.error("response_invalid");
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
