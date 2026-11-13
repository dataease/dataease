package io.dataease.ai.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dataease.api.ai.runtime.AiPromptFingerprint;
import org.springframework.ai.chat.prompt.Prompt;
import java.util.*;

/** Conversation-only protocol. No callbacks, provider extensions or private reasoning replay. */
public final class AiToolProtocol {
    private AiToolProtocol() { }
    public static final int MAX_MODEL_STEPS = 5, MAX_ARGUMENTS = 8192, MAX_RESULTS = 32768;
    private static final ObjectMapper JSON = new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    static {
        JSON.getFactory().setStreamReadConstraints(com.fasterxml.jackson.core.StreamReadConstraints.builder()
                .maxNestingDepth(12).maxStringLength(65536).maxNumberLength(20).build());
    }
    public enum RetrySafety { READ_ONLY, IDEMPOTENT, UNKNOWN }
    public record Definition(String name, String description, Map<String, String> parameters, RetrySafety retrySafety) {
        public Definition(String name, String description, Map<String, String> parameters) { this(name, description, parameters, RetrySafety.UNKNOWN); }
        public Definition { parameters = Map.copyOf(parameters); }
    }
    public record Call(String id, String name, String arguments) { }
    public record Message(String role, String content, List<Call> calls, String callId) {
        public Message { calls = calls == null ? List.of() : List.copyOf(calls); }
        public static Message text(String role, String text) { return new Message(role, text, List.of(), null); }
        public static Message assistant(Step step) { return new Message("assistant", step.text(), step.calls(), null); }
        public static Message result(Call call, String result) { return new Message("tool", result, List.of(), call.id()); }
    }
    public record Input(List<Message> messages, List<Definition> tools) {
        public Input { messages = List.copyOf(messages); tools = List.copyOf(tools); }
    }
    public record Step(String text, List<Call> calls) { public Step { calls = List.copyOf(calls); } }
    public static ObjectNode payload(Input input) {
        var root = JSON.createObjectNode(); var messages = root.putArray("messages");
        for (var message : input.messages()) {
            var node = messages.addObject(); node.put("role", message.role()); node.put("content", message.content());
            if (message.callId() != null) node.put("tool_call_id", message.callId());
            if (!message.calls().isEmpty()) {
                var calls = node.putArray("tool_calls");
                for (var call : message.calls()) {
                    var item = calls.addObject(); item.put("id", call.id()); item.put("type", "function");
                    var function = item.putObject("function"); function.put("name", call.name()); function.put("arguments", call.arguments());
                }
            }
        }
        if (!input.tools().isEmpty()) {
            var tools = root.putArray("tools");
            for (var tool : input.tools()) {
                var item = tools.addObject(); item.put("type", "function"); var function = item.putObject("function");
                function.put("name", tool.name()); function.put("description", tool.description());
                var params = function.putObject("parameters"); params.put("type", "object"); params.put("additionalProperties", false);
                var properties = params.putObject("properties"); var required = params.putArray("required");
                tool.parameters().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    properties.putObject(entry.getKey()).put("type", "string").put("description", entry.getValue()); required.add(entry.getKey());
                });
            }
            root.put("parallel_tool_calls", false);
        }
        return root;
    }
    public static String fingerprint(Input input) { return AiPromptFingerprint.of(new Prompt(payload(input).toString())); }
    /** One complete, shallow JSON object with the registered exact string keys. */
    public static Map<String, String> arguments(Definition definition, Call call) {
        if (call.arguments() == null || call.arguments().length() > MAX_ARGUMENTS) throw AiErrors.error("invalid");
        try {
            JsonNode node = JSON.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(call.arguments());
            if (node == null || !node.isObject() || node.size() != definition.parameters().size()) throw AiErrors.error("invalid");
            var result = new LinkedHashMap<String, String>();
            for (String name : definition.parameters().keySet()) {
                var value = node.get(name);
                if (value == null || !value.isTextual() || value.asText().isBlank()) throw AiErrors.error("invalid");
                result.put(name, value.asText());
            }
            return Map.copyOf(result);
        } catch (java.io.IOException invalid) { throw AiErrors.error("invalid"); }
    }
    /** Only malformed arguments of a known tool can request model correction; never expose the exception. */
    public static String correction(Call call, AiRecovery recovery, Set<String> invalid) {
        String canonical = call.name() + ":" + Objects.toString(call.arguments(), "").strip();
        try { canonical = call.name() + ":" + JSON.writer().with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).writeValueAsString(JSON.convertValue(JSON.readTree(call.arguments()), Object.class)); }
        catch (Exception ignored) { }
        if (!invalid.add(canonical) || !recovery.correct(canonical)) throw AiErrors.error("invalid");
        return result(Map.of("status", "INVALID", "code", "arguments", "message", "Use exactly the registered argument names with nonempty string values. No tool was executed."));
    }
    public static boolean invalidResult(String result) {
        try { return "INVALID".equals(JSON.readTree(result).path("status").asText()); }
        catch (Exception ignored) { return false; }
    }
    public static String result(Object value) {
        try {
            String result = JSON.writeValueAsString(value);
            if (result.length() > MAX_RESULTS || result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_RESULTS) throw AiErrors.error("output_limit");
            return result;
        } catch (java.io.IOException invalid) { throw AiErrors.error("response_invalid"); }
    }
    /** Shared across all model steps; independent of provider-reported usage and packet sizes. */
    public static final class Budget {
        private long raw, generated, results;
        private int steps, frames;
        public final int output;
        public Budget(int output) { this.output = output; }
        public synchronized int allocate() {
            if (steps >= MAX_MODEL_STEPS || output < 0) throw AiErrors.error("output_limit");
            steps++; return output;
        }
        public synchronized void frame() { if (++frames > 20000) throw AiErrors.error("response_invalid"); }
        public synchronized void raw(int bytes) { raw += bytes; if (raw > 4 * 1024 * 1024) throw AiErrors.error("response_invalid"); }
        public synchronized void generated(int chars) { generated += chars; if (generated > 262144L) throw AiErrors.error("output_limit"); }
        public synchronized void result(String result) { results += result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length; if (results > MAX_RESULTS) throw AiErrors.error("output_limit"); }
    }
}
