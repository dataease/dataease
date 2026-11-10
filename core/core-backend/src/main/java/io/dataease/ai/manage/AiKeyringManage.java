package io.dataease.ai.manage;

import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiKeyMaterial;
import io.dataease.ai.support.AiSecrets;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;

/** Owns only the key fields on the existing settings row; never returns them through an HTTP API. */
@Component
public class AiKeyringManage {
    private final EntityManager em;
    private final TransactionTemplate transaction;
    private final String configured;
    private final String activeKey;

    public AiKeyringManage(EntityManager em, PlatformTransactionManager manager,
                           @Value("${dataease.ai.keys:}") String configured,
                           @Value("${dataease.ai.active-key:}") String activeKey) {
        this.em = em;
        this.configured = configured;
        this.activeKey = activeKey;
        transaction = new TransactionTemplate(manager);
        transaction.setTimeout(30);
    }

    private boolean external() { return !configured.isEmpty() || !activeKey.isEmpty(); }

    /** Called after schema migration. No candidate escapes the transaction before commit. */
    public void initialize() {
        requireOwnTransaction();
        transaction.executeWithoutResult(status -> {
            CoreAiSetting state = locked();
            if (empty(state)) {
                AiKeyMaterial material;
                if (external()) {
                    material = AiKeyMaterial.parse(configured, activeKey);
                    validateModels(material);
                } else {
                    if (em.createQuery("select count(m) from CoreAiModel m where m.keyEnvelope is not null", Long.class)
                            .getSingleResult() != 0) throw AiErrors.error("key_unavailable");
                    material = AiKeyMaterial.generate();
                }
                store(state, material, external() ? "EXTERNAL" : "DATABASE", 1L);
                em.flush();
            } else {
                read(state.getKeySource(), state.getKeyRing(), state.getActiveKeyId(), state.getKeyRevision(), state.getKeyFingerprint());
            }
        });
        current();
    }

    /** Scalar query bypasses the first-level entity cache and checks every cryptographic operation. */
    public AiKeyMaterial current() {
        try {
            List<Object[]> rows = em.createQuery("select s.keySource, s.keyRing, s.activeKeyId, s.keyRevision, s.keyFingerprint "
                            + "from CoreAiSetting s where s.id = 1", Object[].class)
                    .setFlushMode(FlushModeType.COMMIT).getResultList();
            if (rows.size() != 1) throw AiErrors.error("key_unavailable");
            Object[] row = rows.getFirst();
            return read((String) row[0], (String) row[1], (String) row[2], (Long) row[3], (String) row[4]);
        } catch (RuntimeException failure) {
            throw AiErrors.error("key_unavailable");
        }
    }

    private AiKeyMaterial read(String source, String ring, String active, Long revision, String fingerprint) {
        if (revision == null || revision < 1 || fingerprint == null) throw AiErrors.error("key_unavailable");
        AiKeyMaterial material;
        if ("EXTERNAL".equals(source) && external() && ring == null) {
            material = AiKeyMaterial.parse(configured, activeKey);
        } else if ("DATABASE".equals(source) && !external()) {
            material = AiKeyMaterial.parse(ring, active);
        } else {
            throw AiErrors.error("key_unavailable");
        }
        if (!Objects.equals(active, material.activeKey()) || !Objects.equals(fingerprint, material.fingerprint())) {
            throw AiErrors.error("key_unavailable");
        }
        return material;
    }

    private CoreAiSetting locked() {
        CoreAiSetting state = em.find(CoreAiSetting.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        if (state == null) throw AiErrors.error("key_unavailable");
        em.refresh(state, LockModeType.PESSIMISTIC_WRITE);
        return state;
    }

    private boolean empty(CoreAiSetting state) {
        return state.getKeySource() == null && state.getKeyRing() == null && state.getActiveKeyId() == null
                && state.getKeyRevision() == null && state.getKeyFingerprint() == null;
    }

    private void store(CoreAiSetting state, AiKeyMaterial material, String source, long revision) {
        if ("DATABASE".equals(source) && material.canonical().length() > 4000) throw AiErrors.error("key_unavailable");
        state.setKeySource(source);
        state.setKeyRing("DATABASE".equals(source) ? material.canonical() : null);
        state.setActiveKeyId(material.activeKey());
        state.setKeyRevision(revision);
        state.setKeyFingerprint(material.fingerprint());
    }

    private void validateModels(AiKeyMaterial material) {
        // Check ciphertext as well as key IDs before accepting a legacy external key ring.
        AiSecrets verifier = new AiSecrets(material.canonical(), material.activeKey(), new com.fasterxml.jackson.databind.ObjectMapper());
        for (Object[] model : em.createQuery("select m.id, m.keyEnvelope from CoreAiModel m where m.keyEnvelope is not null", Object[].class).getResultList()) {
            verifier.decrypt((String) model[1], (Long) model[0]);
        }
    }

    /** Internal maintenance primitive, not an HTTP endpoint. Caller must quiesce AI and back up first. */
    public void replace(long expectedRevision, String ring, String active) {
        requireOwnTransaction();
        AiKeyMaterial replacement = AiKeyMaterial.parse(ring, active);
        transaction.executeWithoutResult(status -> {
            CoreAiSetting state = locked();
            AiKeyMaterial previous = read(state.getKeySource(), state.getKeyRing(), state.getActiveKeyId(), state.getKeyRevision(), state.getKeyFingerprint());
            if (!Objects.equals(expectedRevision, state.getKeyRevision())) throw AiErrors.error("conflict");
            // Keep every old key byte-for-byte, including keys still referenced by short-lived proofs.
            for (String item : previous.canonical().split(",")) {
                String id = item.substring(0, item.indexOf(':'));
                if (!java.security.MessageDigest.isEqual(previous.key(id), replacement.key(id))) throw AiErrors.error("key_unavailable");
            }
            validateModels(replacement);
            store(state, replacement, state.getKeySource(), Math.addExact(expectedRevision, 1));
            em.flush();
        });
    }

    private void requireOwnTransaction() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) throw AiErrors.error("transaction_active");
    }
}
