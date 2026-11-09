package io.dataease.ai.manage;

import io.dataease.api.ai.runtime.*;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.CoreAiOrgPolicyRepository;
import io.dataease.ai.support.*;
import io.dataease.permission.util.V3UserUtil;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.prompt.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.*;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;

/** 原生 Spring AI 公共模型边界；无管理权限要求，无任意选模入口。 */
@Component
public class AiRuntimeManage implements AiContextFactory, AiModelProvider {
    private record Context(String executionId, String feature, Long actorId, Long organizationId, boolean background,
            long deadline, RequestAttributes request, String proxyBinding, String taskId, String taskVersion, AiDataScope scope, AiDeploymentMode.State deployment) implements AiExecutionContext {
        public Long proxyOrganizationId() { return proxyBinding != null && !proxyBinding.endsWith(":none") ? organizationId : null; }
    }
    private final Map<String, AiFeature> features;
    private final AiConfigurationManage configuration;
    private final CoreAiOrgPolicyRepository policies;
    private final AiSecrets secrets;
    private final AiTransport transport;
    private final ObjectMapper json;
    private final AiAccess access;
    public AiRuntimeManage(ObjectProvider<AiFeature> features, AiConfigurationManage configuration,
                           CoreAiOrgPolicyRepository policies, AiSecrets secrets, AiTransport transport, ObjectMapper json, AiAccess access) {
        this.features = features.stream().collect(Collectors.toUnmodifiableMap(AiFeature::code, f -> f));
        this.configuration = configuration; this.policies = policies; this.secrets = secrets; this.transport = transport; this.json = json; this.access = access;
    }
    @Override public AiExecutionContext current(String feature, AiDataScope scope) {
        Objects.requireNonNull(scope);
        feature(feature);
        if (V3UserUtil.getUid() == null || V3UserUtil.getLink() != null || RequestContextHolder.getRequestAttributes() == null) throw AiErrors.error("forbidden");
        AiAccess.Identity identity = access.capture();
        access.verify(identity, false);
        Long actor = identity.actor();
        Long org = identity.organization();
        return new Context(UUID.randomUUID().toString(), feature, actor, org, false,
                System.currentTimeMillis() + configuration.setting().getRequestTimeoutSeconds() * 1000L,
                RequestContextHolder.getRequestAttributes(), binding(), null, null, scope, identity.deployment());
    }
    @Override public AiExecutionContext task(String feature, String taskId, AiDataScope scope) {
        Objects.requireNonNull(scope);
        if (taskId == null || taskId.isBlank()) throw AiErrors.error("invalid");
        AiFeature.TaskIdentity identity = feature(feature).taskIdentity(taskId);
        if (identity == null || identity.actorId() == null || identity.identityVersion() == null) throw AiErrors.error("forbidden");
        AiDeploymentMode.State deployment = access.mode();
        access.verify(new AiAccess.Identity(identity.actorId(), identity.organizationId(), identity.proxyOrganizationId(), deployment), false);
        return new Context(UUID.randomUUID().toString(), feature, identity.actorId(), identity.organizationId(), true,
                System.currentTimeMillis() + configuration.setting().getRequestTimeoutSeconds() * 1000L,
                null, identity.actorId() + ":" + (identity.proxyOrganizationId() == null ? "none" : identity.proxyOrganizationId()), taskId, identity.identityVersion(), scope, deployment);
    }
    @Override public ChatModel current(AiExecutionContext value) {
        if (!(value instanceof Context context)) throw AiErrors.error("forbidden");
        caller(context);
        return new ChatModel() {
            @Override public ChatResponse call(Prompt prompt) {
                caller(context);
                if (TransactionSynchronizationManager.isActualTransactionActive()) throw AiErrors.error("transaction_active");
                CoreAiSetting s = configuration.setting();
                CoreAiModel model = resolve(context, s);
                long deadline = Math.min(context.deadline(), System.currentTimeMillis() + s.getRequestTimeoutSeconds() * 1000L);
                Prompt finalPrompt = AiPromptPolicy.constrain(prompt, model.getModelIdentifier(), s.getMaxOutputTokens(), s.getMaxInputChars(), json, model.getOutputTokenParameter());
                if (context.scope().inputFingerprint() != null && !AiPromptFingerprint.of(finalPrompt).equals(context.scope().inputFingerprint())) throw AiErrors.error("scope_changed");
                inspect(context, finalPrompt);
                var connection = new AiConfigurationManage.Connection(model.getId(), model.getConfigRevision(), model.getProvider(),
                        model.getBaseUrl(), secrets.decrypt(model.getKeyEnvelope(), model.getId()), model.getModelIdentifier());
                return transport.call(connection, s, finalPrompt, context.background(), deadline, () -> {
                    inspect(context, finalPrompt);
                    CoreAiSetting latest = configuration.setting();
                    CoreAiModel current = resolve(context, latest);
                    if (!Objects.equals(latest.getRevision(), s.getRevision()) || !Objects.equals(current.getId(), model.getId())
                            || !Objects.equals(current.getConfigRevision(), model.getConfigRevision())
                            || !Objects.equals(current.getCredentialRevision(), model.getCredentialRevision())) throw AiErrors.error("conflict");
                });
            }
            @Override public Flux<ChatResponse> stream(Prompt prompt) { return Flux.error(AiErrors.error("unsupported")); }
            @Override public ChatOptions getDefaultOptions() { return ChatOptions.builder().build(); }
        };
    }
    private void inspect(Context context, Prompt prompt) {
        java.util.List<org.springframework.ai.chat.messages.Message> messages = prompt.getInstructions().stream().map(message ->
                switch (message.getMessageType()) {
                    case USER -> (org.springframework.ai.chat.messages.Message) new org.springframework.ai.chat.messages.UserMessage(message.getText());
                    case SYSTEM -> new org.springframework.ai.chat.messages.SystemMessage(message.getText());
                    case ASSISTANT -> new org.springframework.ai.chat.messages.AssistantMessage(message.getText());
                    default -> throw AiErrors.error("unsupported");
                }).toList();
        try {
            var detached = json.readValue(json.writeValueAsBytes(prompt.getOptions()), org.springframework.ai.openai.OpenAiChatOptions.class);
            feature(context.feature()).authorizeInput(context, new Prompt(messages, detached));
        } catch (java.io.IOException e) { throw AiErrors.error("invalid"); }
    }
    private String binding() { return "" + V3UserUtil.getUid() + ":" + (V3UserUtil.getProxy().isProxy() ? V3UserUtil.getProxy().getProxyOid() : "none"); }
    private void caller(Context c) {
        if (c.background()) return;
        if (RequestContextHolder.getRequestAttributes() != c.request() || V3UserUtil.getLink() != null
                || !Objects.equals(V3UserUtil.getUid(), c.actorId()) || !Objects.equals(binding(), c.proxyBinding())) throw AiErrors.error("forbidden");
    }
    private AiFeature feature(String code) {
        AiFeature f = features.get(code); if (f == null) throw AiErrors.error("forbidden"); return f;
    }
    private CoreAiModel resolve(Context c, CoreAiSetting s) {
        if (System.currentTimeMillis() >= c.deadline()) throw AiErrors.error("timeout");
        AiFeature feature = feature(c.feature());
        if (c.background()) {
            AiFeature.TaskIdentity now = feature.taskIdentity(c.taskId());
            if (now == null || !Objects.equals(now.actorId(), c.actorId()) || !Objects.equals(now.organizationId(), c.organizationId())
                    || !Objects.equals(now.identityVersion(), c.taskVersion()) || !Objects.equals(now.proxyOrganizationId(), c.proxyOrganizationId())) throw AiErrors.error("forbidden");
        }
        Long proxy = c.proxyOrganizationId();
        access.verify(new AiAccess.Identity(c.actorId(), c.organizationId(), proxy, c.deployment()), false);
        feature.authorize(c);
        if (!Boolean.TRUE.equals(s.getEnabled())) throw AiErrors.error("disabled");
        if (c.scope().samples()) {
            if (c.background() || !Boolean.TRUE.equals(s.getAllowSamples())) throw AiErrors.error("samples_denied");
            feature.checkSampleConfirmation(c);
        }
        Long selected = s.getDefaultModelId();
        if (c.organizationId() != null) {
            CoreAiOrgPolicy policy = policies.findById(c.organizationId()).orElse(null);
            if (policy != null) {
                if ("DISABLED".equals(policy.getMode())) throw AiErrors.error("organization_disabled");
                if ("SPECIFIED".equals(policy.getMode())) selected = policy.getModelId();
            }
        }
        return configuration.available(selected);
    }
}
