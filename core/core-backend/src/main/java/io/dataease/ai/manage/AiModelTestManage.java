package io.dataease.ai.manage;

import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.ai.support.*;
import org.springframework.ai.chat.prompt.*;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Component
public class AiModelTestManage {
    private final AiConfigurationManage configuration;
    private final AiTransport transport;
    private final AiAccess access;
    private final ObjectMapper json;
    public AiModelTestManage(AiConfigurationManage configuration, AiTransport transport, AiAccess access, ObjectMapper json) {
        this.configuration = configuration; this.transport = transport; this.access = access; this.json = json;
    }
    public List<String> discover(Candidate candidate) {
        var connection = configuration.candidate(candidate, false);
        var settings = configuration.setting();
        return transport.discover(connection, settings, System.currentTimeMillis() + settings.getRequestTimeoutSeconds() * 1000L, configuration.maintenanceCheck(connection, candidate));
    }
    public TestResult test(Candidate candidate, boolean savedSummary) {
        var connection = configuration.candidate(candidate, true);
        var settings = configuration.setting(); long started = System.currentTimeMillis();
        long deadline = started + settings.getRequestTimeoutSeconds() * 1000L;
        AiAccess.Identity identity = access.configurationIdentity();
        Runnable recheck = configuration.maintenanceCheck(connection, candidate, identity);
        boolean basic = false, structured = false;
        String parameter = "MAX_TOKENS";
        // Capability negotiation uses a bounded synthetic probe even when runtime requests follow the model.
        int probeTokens = settings.getMaxOutputTokens() > 0 ? settings.getMaxOutputTokens() : 4096;
        try {
            OpenAiChatOptions options = OpenAiChatOptions.builder().model(connection.identifier()).maxTokens(probeTokens).internalToolExecutionEnabled(false).build();
            try {
                transport.call(connection, settings, new Prompt("Reply with the word OK.", options), false, deadline, recheck);
            } catch (AiErrors.Failure failure) {
                if (!"max_tokens_unsupported".equals(failure.safeCode())) throw failure;
                // 仅明确参数不支持时有限协商一次，非通用重试；每次实际 HTTP 仍独立准入和鉴权。
                parameter = "MAX_COMPLETION_TOKENS";
                options.setMaxTokens(null); options.setMaxCompletionTokens(probeTokens);
                transport.call(connection, settings, new Prompt("Reply with the word OK.", options), false, deadline, recheck);
            }
            basic = true;
            // OpenAI 兼容不等于支持原生 JSON Schema；使用 JSON 模式并由 DE 严格校验结构。
            options.setResponseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build());
            var result = transport.call(connection, settings, new Prompt("Return only this JSON object, with exactly one boolean field and no extra text: {\"ok\":true}", options), false, deadline, recheck);
            com.fasterxml.jackson.databind.JsonNode parsed;
            try {
                parsed = json.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                        .readTree(result.getResult().getOutput().getText());
            } catch (com.fasterxml.jackson.core.JsonProcessingException invalidJson) { throw AiErrors.error("response_invalid"); }
            if (!parsed.isObject() || parsed.size() != 1 || !parsed.path("ok").isBoolean() || !parsed.path("ok").asBoolean()) throw AiErrors.error("response_invalid");
            structured = true;
            recheck.run();
            // 行内测试只更新摘要；可能更新连接版本，不签发编辑保存凭证。
            String proof = savedSummary ? null : configuration.testProof(connection, parameter, identity);
            if (savedSummary) configuration.testSummary(connection, true, started, null, parameter);
            return new TestResult(true, true, null, proof);
        } catch (Exception e) {
            String code = AiErrors.code(e);
            if ("forbidden".equals(code) || "conflict".equals(code)) throw AiErrors.error(code);
            if (savedSummary) configuration.testSummary(connection, false, started, code, parameter);
            return new TestResult(basic, structured, code, null);
        }
    }
}
