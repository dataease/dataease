package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.manage.AiConfigurationManage;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.SettingsChange;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

/** Real provider wire requests against an owned server, with no external model or credentials. */
public final class ModelLimitsRegression {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void run(AiConfigurationManage configuration, AiAdmission admission) throws Exception {
        SettingsDefaultsRegression.run();
        var json = new ObjectMapper();
        var fresh = new CoreAiSetting();
        check(fresh.getMaxInputChars() == 0 && fresh.getMaxOutputTokens() == 0, "new installations must follow the model");
        try (var validators = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            for (int output : new int[]{0, 65536, 1048576})
                check(validators.getValidator().validate(new SettingsChange(1L, false, null, false, 60, 1048576, output, 5, 2, null)).isEmpty(), "0 / 64K / 1M settings accepted");
        }
        var before = configuration.setting();
        configuration.saveSettings(new SettingsChange(before.getRowVersion(), before.getEnabled(), before.getDefaultModelId(), before.getAllowSamples(), 60, 0, 0, 5, 2, null));
        var settings = configuration.setting();
        check(settings.getMaxOutputTokens() == 0 && settings.getMaxInputChars() == 0, "model defaults persist through normal settings publication");
        var defaultPrompt = AiPromptPolicy.constrain(new Prompt("Explain SQL"), "synthetic", 0, 0, json);
        var options = (OpenAiChatOptions) defaultPrompt.getOptions();
        check(options.getMaxTokens() == null && options.getMaxCompletionTokens() == null, "no output parameter in model-default prompt");
        AiPromptPolicy.constrain(new Prompt("a".repeat(300000)), "synthetic", 0, 0, json);
        var explicit = AiPromptPolicy.constrain(new Prompt("SQL", OpenAiChatOptions.builder().maxTokens(65536).build()), "synthetic", 0, 0, json);
        check(explicit.getOptions().getMaxTokens() == 65536, "native SDK caller can explicitly request 64K");
        var budget = new AiToolProtocol.Budget(65536);
        for (int i = 0; i < 5; i++) check(budget.allocate() == 65536, "custom limit applies to each request without dividing by five");
        try { budget.allocate(); throw new AssertionError("step count not bounded"); } catch (AiErrors.Failure expected) { }
        var wire = new AtomicReference<com.fasterxml.jackson.databind.JsonNode>();
        var temporaryFailures = new AtomicInteger();
        var wireRequests = new java.util.concurrent.CopyOnWriteArrayList<com.fasterxml.jackson.databind.JsonNode>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            var request = json.readTree(exchange.getRequestBody()); wire.set(request); wireRequests.add(request);
            if (temporaryFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                exchange.sendResponseHeaders(503, -1); exchange.close(); return;
            }
            boolean stream = request.path("stream").asBoolean();
            String body = stream
                    ? "data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"OK\"},\"finish_reason\":\"stop\"}]}\n\ndata: {\"choices\":[],\"usage\":{\"completion_tokens\":9000}}\n\ndata: [DONE]\n\n"
                    : "{\"id\":\"synthetic\",\"object\":\"chat.completion\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"OK\"},\"finish_reason\":\"stop\"}]}";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", stream ? "text/event-stream" : "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try { exchange.getResponseBody().write(bytes); } finally { exchange.close(); }
        });
        server.start();
        var transport = new AiTransport(new AiOutboundPolicy(), admission, json);
        var connection = new AiConfigurationManage.Connection(1L, 1, "OPENAI_COMPATIBLE", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "synthetic", "synthetic");
        try {
            transport.call(connection, settings, defaultPrompt, false, System.currentTimeMillis() + 5000, () -> { });
            check(!wire.get().has("max_tokens") && !wire.get().has("max_completion_tokens"), "native wire must omit model-default output parameter");
            transport.call(connection, settings, explicit, false, System.currentTimeMillis() + 5000, () -> { });
            check(wire.get().path("max_tokens").asInt() == 65536, "native explicit 64K reaches the provider intact");
            for (boolean tools : new boolean[]{false, true}) {
                wireRequests.clear(); temporaryFailures.set(tools ? 1 : 0);
                var done = new CountDownLatch(1); var failure = new AtomicReference<Throwable>(); var text = new StringBuilder();
                java.util.function.Consumer<Throwable> failed = error -> { failure.set(error); done.countDown(); };
                var recovery = new AiRecovery(settings);
                if (tools) {
                    var unrestricted = new AiToolProtocol.Budget(0);
                    var payload = json.createObjectNode().put("model", "synthetic").put("stream", true);
                    try (var stream = transport.toolStream()) {
                        stream.start(connection, settings, payload, System.currentTimeMillis() + 5000, unrestricted.allocate(), unrestricted, () -> { }, () -> { }, text::append, result -> done.countDown(), failed, recovery);
                        check(done.await(5, TimeUnit.SECONDS), "tool stream completes");
                    }
                } else {
                    try (var stream = transport.stream()) {
                        stream.start(connection, settings, defaultPrompt, System.currentTimeMillis() + 5000, () -> { }, () -> { }, text::append, done::countDown, failed, recovery);
                        check(done.await(5, TimeUnit.SECONDS), "plain stream completes");
                    }
                }
                if (failure.get() != null) throw new AssertionError("model-default stream must accept provider usage", failure.get());
                check("OK".contentEquals(text), "incremental output preserved");
                check(!wire.get().has("max_tokens") && !wire.get().has("max_completion_tokens"), "stream wire must omit output parameter");
                check(wireRequests.size() == (tools ? 2 : 1) && wireRequests.stream().noneMatch(request -> request.has("max_tokens") || request.has("max_completion_tokens")), "technical retries must retain model-default parameters");
            }
            System.out.println("PASS model-default settings, 64K/1M validation, native/plain/tool wire omission, provider usage and independent per-request limits");
        } finally { transport.close(); server.stop(0); }
    }
}
