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
            long deadline, RequestAttributes request, String proxyBinding, String taskId, String taskVersion, AiDataScope scope, AiDeploymentMode.State deployment, String usageRevision) implements AiExecutionContext {
        public Long proxyOrganizationId() { return proxyBinding != null && !proxyBinding.endsWith(":none") ? organizationId : null; }
    }
    private final Map<String, AiFeature> features;
    private final AiConfigurationManage configuration;
    private final CoreAiOrgPolicyRepository policies;
    private final AiSecrets secrets;
    private final AiTransport transport;
    private final ObjectMapper json;
    private final AiAccess access;
    private final AiAuditManage audit;
    public AiRuntimeManage(ObjectProvider<AiFeature> features, AiConfigurationManage configuration,
                           CoreAiOrgPolicyRepository policies, AiSecrets secrets, AiTransport transport, ObjectMapper json, AiAccess access) {
        this(features, configuration, policies, secrets, transport, json, access, null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    public AiRuntimeManage(ObjectProvider<AiFeature> features, AiConfigurationManage configuration,
                           CoreAiOrgPolicyRepository policies, AiSecrets secrets, AiTransport transport, ObjectMapper json, AiAccess access, AiAuditManage audit) {
        this.audit = audit;
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
                RequestContextHolder.getRequestAttributes(), binding(), null, null, scope, identity.deployment(), usageRevision(identity));
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
                null, identity.actorId() + ":" + (identity.proxyOrganizationId() == null ? "none" : identity.proxyOrganizationId()), taskId, identity.identityVersion(), scope, deployment, usageRevision(new AiAccess.Identity(identity.actorId(), identity.organizationId(), identity.proxyOrganizationId(), deployment)));
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
                CoreAiSetting admissionSettings = json.convertValue(s, CoreAiSetting.class);
                Runnable recheck = () -> {
                    inspect(context, finalPrompt);
                    CoreAiSetting latest = configuration.setting();
                    CoreAiModel current = resolve(context, latest);
                    if (!securityPolicyEquivalent(s, latest) || !Objects.equals(current.getId(), model.getId())
                            || !Objects.equals(current.getConfigRevision(), model.getConfigRevision())
                            || !Objects.equals(current.getCredentialRevision(), model.getCredentialRevision())) throw AiErrors.error("conflict");
                    updateAdmission(admissionSettings, latest);
                };
                var capture = audit == null ? null : auditSession(context, new ModelSnapshot(model.getId(), model.getConfigRevision(), model.getCredentialRevision(), model.getModelIdentifier()), s.getRevision(), null, s.getAuditRetentionDays());
                try {
                    ChatResponse response = capture == null
                            ? transport.call(connection, admissionSettings, finalPrompt, context.background(), deadline, recheck)
                            : transport.call(connection, admissionSettings, finalPrompt, context.background(), deadline, recheck, capture);
                    caller(context); recheck.run();
                    if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
                    if (capture != null) capture.end("COMPLETED");
                    return response;
                } catch (RuntimeException failure) { if (capture != null) capture.end("FAILED"); throw failure; }
            }
            @Override public Flux<ChatResponse> stream(Prompt prompt) { return Flux.error(AiErrors.error("unsupported")); }
            @Override public ChatOptions getDefaultOptions() { return ChatOptions.builder().build(); }
        };
    }
    private AiAuditManage.Session auditSession(Context context, ModelSnapshot model, Long settingsRevision, AiRecovery recovery, int retentionDays) {
        return audit.session(context, model, settingsRevision, recovery, feature(context.feature()).auditReference(context), retentionDays);
    }
    /** Created only in the authenticated request; the worker receives no Servlet state or request-bound model. */
    public record ModelSnapshot(Long id, Long revision, Long credentialRevision, String identifier) { }
    public interface StreamHandle extends AutoCloseable {
        ModelSnapshot model();
        AiRecovery recovery();
        String executionId();
        long deadline();
        void verifyDelivery();
        default void finishAudit(String status) { if (recovery().audit() != null) recovery().audit().end(status); }
        @Override void close();
    }
    public final class Streaming implements StreamHandle {
        private final Context context;
        private Prompt prompt;
        private AiConfigurationManage.Connection connection;
        private final CoreAiSetting settings;
        private Runnable verify;
        private final Runnable delivery;
        private final AiTransport.Stream stream;
        private final ModelSnapshot model;
        private final AiRecovery recovery;
        private Streaming(Context context, Prompt prompt, AiConfigurationManage.Connection connection, CoreAiSetting settings, Runnable verify, Runnable delivery, ModelSnapshot model) {
            this.stream = transport.stream(context.background()); this.model = model; this.recovery = new AiRecovery(settings);
            if (audit != null) recovery.audit(auditSession(context, model, settings.getRevision(), recovery, settings.getAuditRetentionDays()));
            this.context = context; this.prompt = prompt; this.connection = connection; this.settings = settings; this.verify = verify; this.delivery = delivery;
        }
        public String executionId() { return context.executionId(); }
        public ModelSnapshot model() { return model; }
        public AiRecovery recovery() { return recovery; }
        public long deadline() { return context.deadline(); }
        public void verifyDelivery() { delivery.run(); }
        public synchronized void start(Runnable progress, java.util.function.Consumer<String> delta, Runnable completed, java.util.function.Consumer<Throwable> failed) {
            if (connection == null) throw AiErrors.error("cancelled");
            try { stream.start(connection, settings, prompt, context.deadline(), verify, progress, delta, completed, failed, recovery); }
            finally { connection = null; prompt = null; verify = null; }
        }
        @Override public synchronized void close() { stream.close(); finishAudit("CANCELLED"); connection = null; prompt = null; verify = null; }
    }
    public Streaming stream(String feature, AiDataScope scope, Prompt input) {
        return stream((Context) current(feature, scope), input);
    }
    public Streaming streamTask(String feature, String taskId, AiDataScope scope, Prompt input) {
        return stream((Context) task(feature, taskId, scope), input);
    }
    public Streaming streamTask(String feature, String taskId, AiDataScope scope, Prompt input, long deadline) {
        return stream(boundedTask(feature, taskId, scope, deadline), input);
    }
    private Context boundedTask(String feature, String taskId, AiDataScope scope, long deadline) {
        Context c = (Context) task(feature, taskId, scope);
        return new Context(c.executionId(), c.feature(), c.actorId(), c.organizationId(), c.background(), Math.min(c.deadline(), deadline),
                c.request(), c.proxyBinding(), c.taskId(), c.taskVersion(), c.scope(), c.deployment(), c.usageRevision());
    }
    private Streaming stream(Context request, Prompt input) {
        String feature = request.feature(); AiDataScope scope = request.scope();
        caller(request);
        if (TransactionSynchronizationManager.isActualTransactionActive()) throw AiErrors.error("transaction_active");
        if (!feature(feature).supportsStreaming() || scope.samples()) throw AiErrors.error("unsupported");
        CoreAiSetting original = configuration.setting();
        CoreAiModel model = resolve(request, original);
        Prompt prompt = AiPromptPolicy.constrain(input, model.getModelIdentifier(), original.getMaxOutputTokens(), original.getMaxInputChars(), json, model.getOutputTokenParameter());
        if (scope.inputFingerprint() != null && !AiPromptFingerprint.of(prompt).equals(scope.inputFingerprint())) throw AiErrors.error("scope_changed");
        inspect(request, prompt);
        // Detached immutable authorization coordinates; never copy RequestAttributes into a worker.
        Context detached = new Context(request.executionId(), request.feature(), request.actorId(), request.organizationId(), request.background(),
                request.deadline(), null, request.proxyBinding(), request.taskId(), request.taskVersion(), request.scope(), request.deployment(), request.usageRevision());
        var settings = json.convertValue(original, CoreAiSetting.class);
        var modelId = model.getId();
        var configRevision = model.getConfigRevision(); var credentialRevision = model.getCredentialRevision();
        java.util.function.Consumer<Boolean> policy = enforceDeadline -> {
            var latest = configuration.setting(); var selected = resolve(detached, latest, enforceDeadline);
            if (!securityPolicyEquivalent(original, latest) || !Objects.equals(modelId, selected.getId())
                    || !Objects.equals(configRevision, selected.getConfigRevision()) || !Objects.equals(credentialRevision, selected.getCredentialRevision()))
                throw AiErrors.error("conflict");
            updateAdmission(settings, latest);
        };
        Runnable verify = () -> { inspect(detached, prompt); policy.accept(true); };
        Runnable delivery = () -> { feature(feature).authorize(detached); policy.accept(false); };
        var connection = new AiConfigurationManage.Connection(model.getId(), model.getConfigRevision(), model.getProvider(), model.getBaseUrl(),
                secrets.decrypt(model.getKeyEnvelope(), model.getId()), model.getModelIdentifier());
        // Only detached setting values enter admission; no JPA/request-bound entity crosses threads.
        return new Streaming(detached, prompt, connection, settings, verify, delivery, new ModelSnapshot(modelId, configRevision, credentialRevision, model.getModelIdentifier()));
    }
    /** Capacity/recovery/retention changes are next-request policy, not revocation of a trusted attempt. */
    private static boolean securityPolicyEquivalent(CoreAiSetting before, CoreAiSetting after) {
        return Objects.equals(before.getEnabled(), after.getEnabled())
                && Objects.equals(before.getDefaultModelId(), after.getDefaultModelId())
                && Objects.equals(before.getAllowSamples(), after.getAllowSamples());
    }
    private static void updateAdmission(CoreAiSetting snapshot, CoreAiSetting latest) {
        snapshot.setRevision(latest.getRevision());
        snapshot.setInteractiveConcurrency(latest.getInteractiveConcurrency());
        snapshot.setBackgroundConcurrency(latest.getBackgroundConcurrency());
    }
    /** Same execution/model snapshot across the entire bounded dialogue. No request or managed entity crosses threads. */
    public final class ToolStreaming implements StreamHandle {
        private final Context context;
        private final CoreAiSetting settings;
        private final Long modelId, configRevision, credentialRevision, settingRevision;
        private final AiToolProtocol.Budget budget;
        private final AiRecovery recovery;
        private final String modelIdentifier;
        private AiTransport.ToolStream active;
        private volatile boolean closed;
        private ToolStreaming(Context context, CoreAiSetting settings, CoreAiModel model) {
            this.context = context; this.settings = settings; this.modelIdentifier = model.getModelIdentifier();
            modelId = model.getId(); configRevision = model.getConfigRevision(); credentialRevision = model.getCredentialRevision(); settingRevision = settings.getRevision();
            budget = new AiToolProtocol.Budget(settings.getMaxOutputTokens()); recovery = new AiRecovery(settings);
            if (audit != null) recovery.audit(auditSession(context, model(), settingRevision, recovery, settings.getAuditRetentionDays()));
        }
        public String executionId() { return context.executionId(); }
        public long deadline() { return context.deadline(); }
        private CoreAiModel verify(boolean deadline) {
            var latest = configuration.setting(); var current = resolve(context, latest, deadline);
            if (!securityPolicyEquivalent(settings, latest) || !Objects.equals(modelId, current.getId())
                    || !Objects.equals(configRevision, current.getConfigRevision()) || !Objects.equals(credentialRevision, current.getCredentialRevision())) throw AiErrors.error("conflict");
            updateAdmission(settings, latest); return current;
        }
        public AiRecovery recovery() { return recovery; }
        public ModelSnapshot model() { return new ModelSnapshot(modelId, configRevision, credentialRevision, modelIdentifier); }
        public void verifyDelivery() { verify(false); }
        public void verifyStep() { verify(true); }
        public void result(String result) { budget.result(result); }
        public void send(AiToolProtocol.Input input, Runnable progress, java.util.function.Consumer<String> delta,
                                      java.util.function.Consumer<AiToolProtocol.Step> completed, java.util.function.Consumer<Throwable> failed) {
            if (closed) throw AiErrors.error("cancelled"); progress.run();
            var model = verify(true);
            var feature = (AiToolConversationFeature) feature(context.feature()); feature.authorizeToolInput(context, input);
            int allocation = budget.allocate();
            var payload = AiToolProtocol.payload(input);
            payload.put("model", model.getModelIdentifier()); payload.put("stream", true); payload.put("n", 1);
            payload.put("MAX_COMPLETION_TOKENS".equals(model.getOutputTokenParameter()) ? "max_completion_tokens" : "max_tokens", allocation);
            String serialized = payload.toString();
            // Conservative complete JSON count includes definitions, roles, ids, history, arguments, results and escaping.
            if (serialized.codePointCount(0, serialized.length()) > settings.getMaxInputChars()
                    || serialized.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 4 * 1024 * 1024) throw AiErrors.error("input_limit");
            String fingerprint = AiToolProtocol.fingerprint(input);
            Runnable recheck = () -> {
                progress.run(); verify(true); feature.authorizeToolInput(context, input);
                if (!fingerprint.equals(AiToolProtocol.fingerprint(input))) throw AiErrors.error("scope_changed");
            };
            var connection = new AiConfigurationManage.Connection(model.getId(), model.getConfigRevision(), model.getProvider(), model.getBaseUrl(),
                    secrets.decrypt(model.getKeyEnvelope(), model.getId()), model.getModelIdentifier());
            synchronized (this) {
                if (closed) throw AiErrors.error("cancelled"); progress.run();
                active = transport.toolStream(context.background());
                active.start(connection, settings, payload, context.deadline(), allocation, budget, recheck, progress, delta, completed, failed, recovery);
            }
        }
        public synchronized void close() { closed = true; if (active != null) active.close(); active = null; finishAudit("CANCELLED"); }
    }
    public ToolStreaming streamTools(String feature, AiDataScope scope, AiToolProtocol.Input input) {
        return streamTools((Context) current(feature, scope), input);
    }
    public ToolStreaming streamTaskTools(String feature, String taskId, AiDataScope scope, AiToolProtocol.Input input) {
        return streamTools((Context) task(feature, taskId, scope), input);
    }
    public ToolStreaming streamTaskTools(String feature, String taskId, AiDataScope scope, AiToolProtocol.Input input, long deadline) {
        return streamTools(boundedTask(feature, taskId, scope, deadline), input);
    }
    private ToolStreaming streamTools(Context request, AiToolProtocol.Input input) {
        String feature = request.feature(); AiDataScope scope = request.scope(); caller(request);
        if (TransactionSynchronizationManager.isActualTransactionActive()) throw AiErrors.error("transaction_active");
        if (!(feature(feature) instanceof AiToolConversationFeature toolFeature) || !toolFeature.supportsStreaming() || scope.samples()) throw AiErrors.error("unsupported");
        if (scope.inputFingerprint() == null || !scope.inputFingerprint().equals(AiToolProtocol.fingerprint(input))) throw AiErrors.error("scope_changed");
        var original = configuration.setting(); var model = resolve(request, original); toolFeature.authorizeToolInput(request, input);
        Context detached = new Context(request.executionId(), request.feature(), request.actorId(), request.organizationId(), request.background(),
                request.deadline(), null, request.proxyBinding(), request.taskId(), request.taskVersion(), request.scope(), request.deployment(), request.usageRevision());
        return new ToolStreaming(detached, json.convertValue(original, CoreAiSetting.class), model);
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
    private CoreAiModel resolve(Context c, CoreAiSetting s) { return resolve(c, s, true); }
    private CoreAiModel resolve(Context c, CoreAiSetting s, boolean enforceDeadline) {
        if (enforceDeadline && System.currentTimeMillis() >= c.deadline()) throw AiErrors.error("timeout");
        if (!Objects.equals(c.usageRevision(), usageRevision(c.organizationId(), s))) throw AiErrors.error("scope_changed");
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
        return resolveModel(c.organizationId(), s);
    }
    private String usageRevision(Long organization, CoreAiSetting settings) {
        Long epoch = settings.getUsageEpoch();
        Long organizationEpoch = organization == null ? 0L : policies.findById(organization).map(CoreAiOrgPolicy::getUsageEpoch).orElse(0L);
        if (epoch == null || organizationEpoch == null) throw AiErrors.error("scope_changed");
        return epoch + ":" + organizationEpoch;
    }
    public String usageRevision(AiAccess.Identity owner) { verifyUsage(owner); return usageRevision(owner.organization(), configuration.setting()); }
    public void verifyUsage(AiAccess.Identity owner) {
        access.verify(owner, false);
        if (!Boolean.TRUE.equals(configuration.setting().getEnabled())) throw AiErrors.error("disabled");
        if (owner.organization() != null && policies.findById(owner.organization()).map(p -> "DISABLED".equals(p.getMode())).orElse(false))
            throw AiErrors.error("organization_disabled");
    }
    public void verifyTaskExecution(AiAccess.Identity owner, Long modelId, Long revision, Long credentialRevision) {
        verifyUsage(owner);
        if (modelId != null) {
            var model = configuration.available(modelId);
            if (!Objects.equals(revision, model.getConfigRevision()) || !Objects.equals(credentialRevision, model.getCredentialRevision())) throw AiErrors.error("conflict");
        }
    }
    public void verifyTaskAdmission(AiAccess.Identity owner) {
        verifyUsage(owner); resolveModel(owner.organization(), configuration.setting());
    }
    private CoreAiModel resolveModel(Long organizationId, CoreAiSetting s) {
        if (!Boolean.TRUE.equals(s.getEnabled())) throw AiErrors.error("disabled");
        Long selected = s.getDefaultModelId();
        if (organizationId != null) {
            CoreAiOrgPolicy policy = policies.findById(organizationId).orElse(null);
            if (policy != null) {
                if ("DISABLED".equals(policy.getMode())) throw AiErrors.error("organization_disabled");
                if ("SPECIFIED".equals(policy.getMode())) selected = policy.getModelId();
            }
        }
        return configuration.available(selected);
    }
}
