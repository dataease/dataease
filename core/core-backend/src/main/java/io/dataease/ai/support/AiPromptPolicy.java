package io.dataease.ai.support;

import org.springframework.ai.chat.prompt.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.content.MediaContent;
import org.springframework.ai.openai.OpenAiChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.Introspector;
import java.util.*;

/** 从允许字段新建选项，不复制任意 provider 扩展。 */
public final class AiPromptPolicy {
    private static final Set<String> ALLOWED = Set.of("model", "temperature", "topP", "frequencyPenalty", "presencePenalty", "maxTokens",
            "maxCompletionTokens", "stopSequences", "stop", "responseFormat", "outputSchema", "internalToolExecutionEnabled");
    private AiPromptPolicy() { }
    public static Prompt constrain(Prompt prompt, String model, int maximum, int inputLimit, ObjectMapper json) {
        return constrain(prompt, model, maximum, inputLimit, json, "MAX_TOKENS");
    }
    public static Prompt constrain(Prompt prompt, String model, int maximum, int inputLimit, ObjectMapper json, String parameter) {
        if (!Set.of("MAX_TOKENS", "MAX_COMPLETION_TOKENS").contains(parameter == null ? "" : parameter)) throw AiErrors.error("model_unavailable");
        if (prompt == null || prompt.getInstructions().isEmpty()) throw AiErrors.error("invalid");
        List<Message> messages = new ArrayList<>();
        for (Message message : prompt.getInstructions()) {
            if (message.getMessageType() == MessageType.TOOL || (message instanceof MediaContent media && !media.getMedia().isEmpty())
                    || (message instanceof AssistantMessage assistant && assistant.hasToolCalls())) throw AiErrors.error("unsupported");
            String text = message.getText();
            if (text == null) throw AiErrors.error("unsupported");
            messages.add(switch (message.getMessageType()) {
                case USER -> new UserMessage(text);
                case SYSTEM -> new SystemMessage(text);
                case ASSISTANT -> new AssistantMessage(text);
                default -> throw AiErrors.error("unsupported");
            });
        }
        OpenAiChatOptions safe = new OpenAiChatOptions();
        safe.setModel(model); safe.setN(1); safe.setInternalToolExecutionEnabled(false);
        ChatOptions options = prompt.getOptions();
        Integer requested = maximum > 0 ? maximum : null;
        if (options != null) {
            if (!(options.getClass() == DefaultChatOptions.class || options.getClass() == OpenAiChatOptions.class)) throw AiErrors.error("unsupported");
            try {
                for (var property : Introspector.getBeanInfo(options.getClass(), Object.class).getPropertyDescriptors()) {
                    if (property.getReadMethod() == null || ALLOWED.contains(property.getName())) continue;
                    Object value = property.getReadMethod().invoke(options);
                    if ("streamUsage".equals(property.getName()) && Boolean.FALSE.equals(value)) continue;
                    if (value != null && !(value instanceof Collection<?> collection && collection.isEmpty())
                            && !(value instanceof Map<?, ?> map && map.isEmpty())) throw AiErrors.error("unsupported");
                }
            } catch (ReflectiveOperationException | java.beans.IntrospectionException e) { throw AiErrors.error("unsupported"); }
            if (options.getModel() != null && !model.equals(options.getModel())) throw AiErrors.error("unsupported");
            if (options.getMaxTokens() != null) requested = options.getMaxTokens();
            safe.setTemperature(options.getTemperature()); safe.setTopP(options.getTopP());
            safe.setFrequencyPenalty(options.getFrequencyPenalty()); safe.setPresencePenalty(options.getPresencePenalty());
            safe.setStopSequences(options.getStopSequences() == null ? null : List.copyOf(options.getStopSequences()));
            if (options instanceof OpenAiChatOptions provider) {
                if (Boolean.TRUE.equals(provider.getInternalToolExecutionEnabled())) throw AiErrors.error("unsupported");
                if (provider.getMaxCompletionTokens() != null) {
                    if (options.getMaxTokens() != null) throw AiErrors.error("invalid");
                    if (!"MAX_COMPLETION_TOKENS".equals(parameter)) throw AiErrors.error("unsupported");
                    requested = provider.getMaxCompletionTokens();
                }
                // 深复制结构参数，防止调用方并发修改。
                try { if (provider.getResponseFormat() != null) safe.setResponseFormat(json.readValue(json.writeValueAsBytes(provider.getResponseFormat()), org.springframework.ai.openai.api.ResponseFormat.class)); }
                catch (Exception e) { throw AiErrors.error("invalid"); }
                if (provider.getResponseFormat() != null && provider.getOutputSchema() != null) safe.setOutputSchema(provider.getOutputSchema());
            }
        }
        if (requested != null && (requested < 1 || maximum > 0 && requested > maximum)) throw AiErrors.error("output_limit");
        if ("MAX_COMPLETION_TOKENS".equals(parameter)) safe.setMaxCompletionTokens(requested); else safe.setMaxTokens(requested);
        checkFinite(safe.getTemperature(), 0, 2); checkFinite(safe.getTopP(), 0, 1);
        checkFinite(safe.getFrequencyPenalty(), -2, 2); checkFinite(safe.getPresencePenalty(), -2, 2);
        long characters = messages.stream().map(Message::getText).mapToLong(t -> t.codePointCount(0, t.length())).sum();
        if (safe.getResponseFormat() != null && safe.getOutputSchema() != null) {
            String schema = safe.getOutputSchema(); characters += schema.codePointCount(0, schema.length());
        }
        if (safe.getStopSequences() != null) for (String stop : safe.getStopSequences()) characters += stop.codePointCount(0, stop.length());
        if (inputLimit > 0 && characters > inputLimit) throw AiErrors.error("input_limit");
        return new Prompt(List.copyOf(messages), safe);
    }
    private static void checkFinite(Double value, double min, double max) {
        if (value != null && (!Double.isFinite(value) || value < min || value > max)) throw AiErrors.error("invalid");
    }
}
