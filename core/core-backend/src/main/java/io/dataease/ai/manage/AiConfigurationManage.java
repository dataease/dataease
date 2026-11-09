package io.dataease.ai.manage;

import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.*;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.text.Normalizer;
import java.util.*;

@Component
public class AiConfigurationManage implements io.dataease.api.permissions.org.api.AiOrganizationLifecycle {
    @Resource private CoreAiSettingRepository settings;
    @Resource private CoreAiModelRepository models;
    @Resource private CoreAiOrgPolicyRepository policies;
    @Resource private EntityManager em;
    @Resource private jakarta.validation.Validator validator;
    private final TransactionTemplate transactions;
    public AiConfigurationManage(org.springframework.transaction.PlatformTransactionManager manager) {
        transactions = new TransactionTemplate(manager);
        transactions.setTimeout(30);
    }
    @Resource private AiAccess access;
    @Resource private AiSecrets secrets;
    @Resource private AiOutboundPolicy outbound;
    @Resource private AiAdmission admission;

    public CoreAiSetting setting() {
        return settings.findById(1L).orElseThrow(() -> AiErrors.error("configuration_unavailable"));
    }
    public Settings querySettings() {
        AiAccess.Identity identity = access.configurationIdentity();
        CoreAiSetting s = setting();
        AiAdmission.State state = admission.state(s);
        var result = new Settings(s.getEnabled(), s.getDefaultModelId(), s.getAllowSamples(), s.getRequestTimeoutSeconds(),
                s.getMaxInputChars(), s.getMaxOutputTokens(), s.getInteractiveConcurrency(), s.getBackgroundConcurrency(),
                s.getRevision(), s.getRowVersion(), s.getRevision(), state.revision(), state.name(), identity.deployment().organizations());
        access.verify(identity, true);
        return result;
    }
    public Settings saveSettings(SettingsChange c) {
        access.configuration(); validate(c);
        if (c.backgroundConcurrency() > c.interactiveConcurrency()) throw AiErrors.error("invalid");
        requireOwnTransaction();
        String owner = admission.beginUpdate(setting());
        try {
            write(tx -> {
                CoreAiSetting s = coordinate();
                admission.assertUpdateOwner(owner, s);
                // coordinate 已递增行版本；客户端对照的是递增前版本。
                if (!Objects.equals(c.expectedVersion() + 1, s.getRowVersion())) throw AiErrors.error("conflict");
                if ((!Objects.equals(c.defaultModelId(), s.getDefaultModelId())) || (s.getEnabled() && !c.enabled())) {
                    verifyImpact(c.impactToken(), "SETTINGS", "" + c.defaultModelId() + ":" + c.enabled(), s.getRowVersion() - 1);
                }
                if (c.defaultModelId() != null && !Objects.equals(c.defaultModelId(), s.getDefaultModelId())) available(c.defaultModelId());
                s.setEnabled(c.enabled()); s.setDefaultModelId(c.defaultModelId()); s.setAllowSamples(c.allowSamples());
                s.setRequestTimeoutSeconds(c.requestTimeoutSeconds()); s.setMaxInputChars(c.maxInputChars());
                s.setMaxOutputTokens(c.maxOutputTokens()); s.setInteractiveConcurrency(c.interactiveConcurrency());
                s.setBackgroundConcurrency(c.backgroundConcurrency()); s.setRevision(s.getRevision() + 1);
                touch(s); em.flush();
            });
        } catch (RuntimeException failure) {
            try { admission.finishUpdate(owner, setting(), false); }
            catch (RuntimeException publicationFailure) { /* 恢复器保留阻断状态，不覆盖原业务失败。 */ }
            throw failure;
        }
        try { admission.finishUpdate(owner, setting(), true); }
        catch (RuntimeException publicationFailure) {
            org.slf4j.LoggerFactory.getLogger(AiConfigurationManage.class).warn("AI settings persisted; runtime publication pending");
        }
        return querySettings();
    }
    /** 所有引用及模型写入先 CAS 同一配置行；只用于短配置事务。 */
    private CoreAiSetting coordinate() {
        CoreAiSetting initial = setting();
        int count = em.createQuery("update CoreAiSetting s set s.rowVersion = s.rowVersion + 1 where s.id = 1 and s.rowVersion = :v")
                .setParameter("v", initial.getRowVersion()).executeUpdate();
        if (count != 1) throw AiErrors.error("conflict");
        em.refresh(initial);
        return initial;
    }
    public Draft draft() {
        AiAccess.Identity identity = access.configurationIdentity();
        long id = IDUtils.snowID();
        var draft = new Draft(id, secrets.sign("draft", Map.of("actor", AiAccess.binding(identity), "id", id), 600000));
        access.verify(identity, true);
        return draft;
    }
    public CoreAiModel live(Long id) {
        CoreAiModel model = id == null ? null : models.findById(id).orElse(null);
        if (model == null || model.getDeletedAt() != null) throw AiErrors.error("model_unavailable");
        return model;
    }
    public CoreAiModel available(Long id) {
        CoreAiModel m = live(id);
        if (!Boolean.TRUE.equals(m.getEnabled()) || !Objects.equals(m.getConfigRevision(), m.getValidatedRevision())) throw AiErrors.error("model_unavailable");
        return m;
    }
    public Page<Model> list(String search, int page, int size) {
        AiAccess.Identity identity = access.configurationIdentity();
        if (page < 1 || size < 1 || size > 50) throw AiErrors.error("invalid");
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        var result = models.findAll((r, q, cb) -> term.isEmpty() ? cb.isNull(r.get("deletedAt"))
                : cb.and(cb.isNull(r.get("deletedAt")), cb.or(cb.like(cb.lower(r.get("name")), "%" + escape(term) + "%", '\\'),
                cb.like(cb.lower(r.get("modelIdentifier")), "%" + escape(term) + "%", '\\'))),
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        CoreAiSetting setting = setting();
        Map<Long, Long> counts = new HashMap<>();
        em.createQuery("select p.modelId, count(p) from CoreAiOrgPolicy p where p.modelId is not null group by p.modelId", Object[].class)
                .getResultList().forEach(row -> counts.put((Long) row[0], (Long) row[1]));
        Map<Long, List<String>> names = referenceNames(result.getContent().stream().map(CoreAiModel::getId).toList());
        var pageResult = new Page<>(result.getContent().stream().map(m -> view(m, setting, counts.getOrDefault(m.getId(), 0L), names.getOrDefault(m.getId(), List.of()), identity.deployment().organizations())).toList(), result.getTotalElements());
        access.verify(identity, true);
        return pageResult;
    }
    private String escape(String s) { return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); }
    public Model detail(Long id) {
        AiAccess.Identity identity = access.configurationIdentity();
        CoreAiModel m = live(id);
        var result = view(m, setting(), references(id), referenceNames(List.of(id)).getOrDefault(id, List.of()), identity.deployment().organizations());
        access.verify(identity, true);
        return result;
    }
    private long references(Long id) {
        return policies.count((r, q, cb) -> cb.equal(r.get("modelId"), id));
    }
    private Map<Long, List<String>> referenceNames(List<Long> modelIds) {
        if (modelIds.isEmpty()) return Map.of();
        var directory = access.organizations(); if (directory == null) return Map.of();
        Map<Long, String> organizationNames = new HashMap<>(); directory.forEach(o -> organizationNames.put(o.id(), o.name()));
        Map<Long, List<String>> result = new HashMap<>();
        em.createQuery("select p.modelId, p.orgId from CoreAiOrgPolicy p where p.modelId in :ids", Object[].class)
                .setParameter("ids", modelIds).getResultList().forEach(row -> {
                    String name = organizationNames.get((Long) row[1]);
                    if (name != null) result.computeIfAbsent((Long) row[0], ignored -> new ArrayList<>()).add(name);
                });
        result.values().forEach(names -> names.sort(String::compareTo)); return result;
    }
    private Model view(CoreAiModel m, CoreAiSetting s, long organizationReferences, List<String> organizationNames, boolean organizations) {
        return new Model(m.getId(), m.getName(), m.getProvider(), m.getBaseUrl(), m.getModelIdentifier(),
                m.getKeyEnvelope() != null, Boolean.TRUE.equals(m.getEnabled()), Objects.equals(m.getConfigRevision(), m.getValidatedRevision()),
                m.getConfigRevision(), m.getRowVersion(), m.getProcessingLocation(), m.getProcessingNote(), m.getOutputTokenParameter(), m.getLastTestStatus(),
                m.getLastTestTime(), m.getLastTestErrorCode(), organizations ? organizationReferences : 0L, Objects.equals(m.getId(), s.getDefaultModelId()), organizations ? organizationNames : List.of(), organizations ? 0L : organizationReferences);
    }
    public record Connection(Long id, long baseline, String provider, String url, String key, String identifier) {
        @Override public String toString() { return "AI connection [redacted]"; }
    }
    public Connection candidate(Candidate c, boolean identifierRequired) {
        access.configuration(); validate(c);
        if (c.getId() == null || !"OPENAI_COMPATIBLE".equals(c.getProvider())) throw AiErrors.error("invalid");
        CoreAiModel old = null;
        if (c.getDraftHandle() != null) {
            Map<String, Object> proof = secrets.verify(c.getDraftHandle(), "draft");
            binding(proof, c.getId());
            if (models.existsById(c.getId())) throw AiErrors.error("conflict");
        } else {
            old = live(c.getId());
            if (!Objects.equals(old.getRowVersion(), c.getExpectedVersion())
                    || !Objects.equals(old.getConfigRevision(), c.getExpectedConfigRevision())) throw AiErrors.error("conflict");
        }
        String key = c.getApiKey();
        if (key == null || key.isEmpty()) {
            if (old == null) throw AiErrors.error("invalid");
            key = secrets.decrypt(old.getKeyEnvelope(), old.getId());
        }
        if (key.isBlank() || key.length() > 4096 || key.chars().anyMatch(ch -> ch < 0x21 || ch > 0x7e)) throw AiErrors.error("invalid");
        String url = outbound.normalize(c.getBaseUrl());
        String identifier = c.getModelIdentifier() == null ? "" : c.getModelIdentifier().trim();
        if (identifierRequired && identifier.isEmpty()) throw AiErrors.error("invalid");
        return new Connection(c.getId(), old == null ? 0 : old.getConfigRevision(), c.getProvider(), url, key, identifier);
    }
    private void binding(Map<String, Object> proof, Long id) {
        if (!access.identity().equals(proof.get("actor")) || ((Number) proof.get("id")).longValue() != id) throw AiErrors.error("proof_invalid");
    }
    public Runnable maintenanceCheck(Connection c, Candidate candidate) {
        return maintenanceCheck(c, candidate, access.configurationIdentity());
    }
    public Runnable maintenanceCheck(Connection c, Candidate candidate, AiAccess.Identity identity) {
        String handle = candidate.getDraftHandle();
        Long expected = candidate.getExpectedVersion();
        return () -> {
            access.verify(identity, true);
            if (handle != null) {
                var proof = secrets.verify(handle, "draft");
                if (!Objects.equals(proof.get("actor"), AiAccess.binding(identity))
                        || ((Number) proof.get("id")).longValue() != c.id() || models.existsById(c.id())) throw AiErrors.error("conflict");
            } else {
                CoreAiModel current = live(c.id());
                if (!Objects.equals(current.getRowVersion(), expected) || current.getConfigRevision() != c.baseline()) throw AiErrors.error("conflict");
            }
        };
    }
    public String testProof(Connection c) { return testProof(c, "MAX_TOKENS"); }
    public String testProof(Connection c, String parameter) { return testProof(c, parameter, access.configurationIdentity()); }
    public String testProof(Connection c, String parameter, AiAccess.Identity identity) {
        access.verify(identity, true);
        validateParameter(parameter);
        String kid = secrets.activeKey();
        return secrets.sign("test", Map.of("actor", AiAccess.binding(identity), "id", c.id(), "baseline", c.baseline(),
                "fingerprintKey", kid, "connection", fingerprint(c, kid, parameter), "contract", 3, "outputParameter", parameter), 600000);
    }
    private String fingerprint(Connection c, String kid, String parameter) { return secrets.fingerprint(kid, List.of(c.provider(), c.url(), c.key(), c.identifier(), parameter)); }
    private String verifyTest(Candidate candidate, Connection c) {
        var p = secrets.verify(candidate.getTestProof(), "test"); binding(p, c.id());
        String parameter = (String) p.get("outputParameter"); validateParameter(parameter);
        if (((Number) p.get("baseline")).longValue() != c.baseline() || ((Number) p.get("contract")).intValue() != 3
                || !Objects.equals(p.get("connection"), fingerprint(c, (String) p.get("fingerprintKey"), parameter))) throw AiErrors.error("proof_invalid");
        return parameter;
    }
    private void validateParameter(String value) { if (!Set.of("MAX_TOKENS", "MAX_COMPLETION_TOKENS").contains(value == null ? "" : value)) throw AiErrors.error("proof_invalid"); }
    public Model save(Candidate c, boolean create) {
        requireOwnTransaction();
        Connection connection = candidate(c, true);
        if (c.getName() == null || c.getName().trim().isEmpty()) throw AiErrors.error("invalid");
        String name = c.getName().trim();
        String nameKey = "name:" + Normalizer.normalize(name, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        if (name.length() > 128 || nameKey.length() > 256) throw AiErrors.error("invalid");
        if (create != (c.getDraftHandle() != null)) throw AiErrors.error("invalid");
        write(tx -> {
            coordinate();
            CoreAiModel m = create ? new CoreAiModel() : live(c.getId());
            if (!create && !Objects.equals(m.getRowVersion(), c.getExpectedVersion())) throw AiErrors.error("conflict");
            boolean changed = create || !Objects.equals(m.getProvider(), connection.provider()) || !Objects.equals(m.getBaseUrl(), connection.url())
                    || !Objects.equals(m.getModelIdentifier(), connection.identifier()) || !Objects.equals(secrets.decrypt(m.getKeyEnvelope(), m.getId()), connection.key());
            String parameter = changed || c.getTestProof() != null ? verifyTest(c, connection) : m.getOutputTokenParameter();
            changed = changed || !Objects.equals(parameter, m.getOutputTokenParameter());
            m.setId(c.getId()); m.setName(name); m.setNameKey(nameKey); m.setProvider(connection.provider());
            if (changed) {
                boolean keyChanged = create || !Objects.equals(secrets.decrypt(m.getKeyEnvelope(), m.getId()), connection.key());
                m.setOutputTokenParameter(parameter);
                m.setKeyEnvelope(secrets.encrypt(connection.key(), m.getId()));
                if (!create) { m.setConfigRevision(m.getConfigRevision() + 1); if (keyChanged) m.setCredentialRevision(m.getCredentialRevision() + 1); }
                m.setBaseUrl(connection.url()); m.setModelIdentifier(connection.identifier());
                m.setValidatedRevision(m.getConfigRevision()); m.setLastTestStatus("PASSED"); m.setLastTestTime(System.currentTimeMillis()); m.setLastTestErrorCode(null);
            }
            if (!changed && !m.getKeyEnvelope().startsWith(secrets.activeKey() + ".")) m.setKeyEnvelope(secrets.encrypt(connection.key(), m.getId()));
            m.setProcessingLocation(c.getProcessingLocation()); m.setProcessingNote(c.getProcessingNote()); touch(m);
            if (create) { m.setCreateBy(V3UserUtil.getUid()); em.persist(m); }
            em.flush();
        });
        return detail(c.getId());
    }
    public void testSummary(Connection c, boolean success, long started, String errorCode, String parameter) {
        write(tx -> {
            coordinate();
            CoreAiModel model = models.findById(c.id()).orElse(null);
            if (model == null || model.getDeletedAt() != null || model.getConfigRevision() != c.baseline()
                    || model.getLastTestTime() != null && model.getLastTestTime() > started) return;
            if (success) {
                validateParameter(parameter);
                if (!Objects.equals(model.getOutputTokenParameter(), parameter)) {
                    model.setOutputTokenParameter(parameter);
                    model.setConfigRevision(model.getConfigRevision() + 1);
                    model.setValidatedRevision(model.getConfigRevision());
                }
            }
            model.setLastTestStatus(success ? "PASSED" : "FAILED"); model.setLastTestTime(started);
            model.setLastTestErrorCode(success ? null : errorCode); em.flush();
        });
    }
    public Impact impact(String action, String target) {
        AiAccess.Identity identity = access.configurationIdentity();
        CoreAiSetting s = setting();
        Long id = "SETTINGS".equals(action) ? s.getDefaultModelId() : Long.valueOf(target);
        if (id != null) live(id);
        boolean settingAction = "SETTINGS".equals(action);
        boolean closing = settingAction && Boolean.TRUE.equals(s.getEnabled()) && target.endsWith(":false");
        var directory = access.organizations();
        List<String> names = organizationPage("", directory).items().stream().filter(o -> settingAction
                ? (closing ? !"DISABLED".equals(o.mode()) : "INHERIT".equals(o.mode()))
                : Objects.equals(o.modelId(), id) || (Objects.equals(s.getDefaultModelId(), id) && "INHERIT".equals(o.mode())))
                .map(Org::name).toList();
        String fingerprintKey = secrets.activeKey();
        String token = secrets.sign("impact", Map.of("actor", AiAccess.binding(identity), "action", action, "target", target,
                "version", s.getRowVersion(), "directoryKey", fingerprintKey, "directory", directoryFingerprint(fingerprintKey, directory)), 300000);
        access.verify(identity, true);
        return new Impact(token, settingAction || id != null && Objects.equals(id, s.getDefaultModelId()), names.size(), names);
    }
    private void verifyImpact(String token, String action, String target, long version) {
        var p = secrets.verify(token, "impact");
        if (!access.identity().equals(p.get("actor")) || !action.equals(p.get("action")) || !target.equals(p.get("target"))
                || ((Number) p.get("version")).longValue() != version
                || !Objects.equals(p.get("directory"), directoryFingerprint((String) p.get("directoryKey")))) throw AiErrors.error("conflict");
    }
    public void action(Long id, ModelAction a, boolean delete) {
        access.configuration(); validate(a);
        write(tx -> {
            CoreAiSetting s = coordinate(); CoreAiModel m = live(id);
            if (!Objects.equals(m.getRowVersion(), a.expectedVersion())) throw AiErrors.error("conflict");
            if (delete) {
                if (Objects.equals(id, s.getDefaultModelId())) throw AiErrors.error("referenced");
                if (references(id) != 0) throw AiErrors.error(access.mode().organizations() ? "referenced" : "historical_referenced");
                m.setDeletedAt(System.currentTimeMillis()); m.setEnabled(false); m.setKeyEnvelope(null);
                m.setName("[deleted]"); m.setNameKey("deleted:" + id); m.setBaseUrl("deleted"); m.setModelIdentifier("deleted");
                m.setProcessingNote(null); m.setProcessingLocation("UNKNOWN"); m.setValidatedRevision(null); m.setLastTestTime(null); m.setLastTestStatus("UNTESTED"); m.setLastTestErrorCode(null);
            } else {
                if (a.enabled() == null) throw AiErrors.error("invalid");
                if (a.enabled() && !Objects.equals(m.getConfigRevision(), m.getValidatedRevision())) throw AiErrors.error("needs_test");
                if (Boolean.TRUE.equals(m.getEnabled()) && !a.enabled()) verifyImpact(a.impactToken(), "DISABLE", "" + id, s.getRowVersion() - 1);
                m.setEnabled(a.enabled());
            }
            touch(m); em.flush();
        });
    }
    public OrgPage organizations(String search) {
        AiAccess.Identity identity = access.configurationIdentity();
        var result = organizationPage(search, access.organizations());
        access.verify(identity, true);
        return result;
    }
    private OrgPage organizationPage(String search, java.util.List<io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization> directory) {
        if (directory == null) return new OrgPage(List.of(), 0, false);
        Map<Long, String> names = new HashMap<>(); directory.forEach(o -> names.put(o.id(), o.name()));
        Map<Long, CoreAiOrgPolicy> policyByOrg = new HashMap<>(); policies.findAll().forEach(p -> policyByOrg.put(p.getOrgId(), p));
        Map<Long, CoreAiModel> modelsById = new HashMap<>(); models.findAll((r, q, cb) -> cb.isNull(r.get("deletedAt"))).forEach(m -> modelsById.put(m.getId(), m));
        Long defaultId = setting().getDefaultModelId();
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        List<Org> result = new ArrayList<>();
        for (var node : directory) {
            if (!node.name().toLowerCase(Locale.ROOT).contains(term)) continue;
            CoreAiOrgPolicy p = policyByOrg.get(node.id());
            String mode = p == null ? "INHERIT" : p.getMode(); Long selected = p == null ? null : p.getModelId();
            CoreAiModel model = modelsById.get("SPECIFIED".equals(mode) ? selected : defaultId);
            String reason = "DISABLED".equals(mode) ? "organization_disabled" : model == null || !Boolean.TRUE.equals(model.getEnabled())
                    || !Objects.equals(model.getValidatedRevision(), model.getConfigRevision()) ? "model_unavailable" : null;
            result.add(new Org(node.id(), node.name(), names.getOrDefault(node.parentId(), ""), mode, selected,
                    p == null ? null : p.getRowVersion(), reason == null ? model.getName() : null, reason));
        }
        result.sort(Comparator.comparing(Org::name).thenComparing(Org::id));
        return new OrgPage(result, result.size(), true);
    }
    private String directoryFingerprint(String keyId) {
        return directoryFingerprint(keyId, access.organizations());
    }
    private String directoryFingerprint(String keyId, java.util.List<io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization> directory) {
        if (directory == null) return secrets.fingerprint(keyId, List.of());
        return secrets.fingerprint(keyId, directory.stream().sorted(Comparator.comparing(io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization::id))
                .map(o -> o.id() + ":" + o.parentId() + ":" + o.name()).toList());
    }
    public void batch(OrgBatch batch) {
        access.configuration();
        if (!access.mode().organizations()) throw AiErrors.error("unsupported");
        validate(batch);

        if (batch.organizations().stream().map(ExpectedOrg::id).distinct().count() != batch.organizations().size()) throw AiErrors.error("invalid");
        write(tx -> {
            coordinate();
            Map<Long, Org> allowed = new HashMap<>(); organizations("").items().forEach(o -> allowed.put(o.id(), o));
            if ("SPECIFIED".equals(batch.mode())) available(batch.modelId());
            else if (batch.modelId() != null) throw AiErrors.error("invalid");
            for (ExpectedOrg expected : batch.organizations()) {
                if (!allowed.containsKey(expected.id())) throw AiErrors.error("forbidden");
                CoreAiOrgPolicy p = policies.findById(expected.id()).orElse(null);
                if ((p == null) != (expected.expectedVersion() == null) || (p != null && !Objects.equals(p.getRowVersion(), expected.expectedVersion()))) throw AiErrors.error("conflict");
                boolean create = p == null;
                if (create) { p = new CoreAiOrgPolicy(); p.setOrgId(expected.id()); p.setCreateBy(V3UserUtil.getUid()); }
                p.setMode(batch.mode()); p.setModelId("SPECIFIED".equals(batch.mode()) ? batch.modelId() : null); touch(p);
                if (create) em.persist(p);
            }
            em.flush();
        });
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void beforeDelete(Long organizationId) {
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("AI organization deletion requires the metadata transaction");
        coordinate();
        em.createQuery("delete from CoreAiOrgPolicy p where p.orgId = :id").setParameter("id", organizationId).executeUpdate();
    }
    private void validate(Object value) {
        if (value == null || !validator.validate(value).isEmpty()) throw AiErrors.error("invalid");
    }
    private void requireOwnTransaction() {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) throw AiErrors.error("transaction_active");
    }
    private void write(java.util.function.Consumer<org.springframework.transaction.TransactionStatus> work) {
        requireOwnTransaction();
        AiAccess.Identity identity = access.capture();
        access.verify(identity, true);
        try { transactions.executeWithoutResult(status -> {
            access.verify(identity, true);
            work.accept(status);
            access.verify(identity, true);
        }); }
        catch (org.springframework.dao.OptimisticLockingFailureException | jakarta.persistence.OptimisticLockException conflict) {
            throw AiErrors.error("conflict");
        } catch (org.springframework.dao.DataIntegrityViolationException | org.hibernate.exception.ConstraintViolationException conflict) {
            Throwable cause = conflict;
            while (cause != null) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException constraint && constraint.getConstraintName() != null
                        && constraint.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_ai_model_name")) throw AiErrors.error("name_conflict");
                cause = cause.getCause();
            }
            throw AiErrors.error("conflict");
        }
    }
    private void touch(AiConfigEntity e) { e.setUpdateBy(V3UserUtil.getUid()); e.setUpdateTime(System.currentTimeMillis()); }
}
