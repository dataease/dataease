package io.dataease.ai.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.support.*;
import io.dataease.api.ai.audit.AiAuditApi.*;
import io.dataease.api.ai.runtime.*;
import io.dataease.utils.IDUtils;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** Bounded audit evidence, isolated from task/result transactions. Failure queues contain metadata only. */
@Component
public class AiAuditManage {
    public static final int MAX_BODY = 4 * 1024 * 1024;
    private static final long RETENTION_MILLIS = TimeUnit.DAYS.toMillis(30);
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AiAuditManage.class);
    private final EntityManager em;
    private final TransactionTemplate tx;
    private final AiAccess access;
    private final ObjectMapper json;
    private final Map<String, CoreAiAuditExecution> gaps = new LinkedHashMap<>();
    private final AtomicLong droppedGapMarkers = new AtomicLong();
    private final ScheduledExecutorService repair = Executors.newSingleThreadScheduledExecutor(r -> { var t = new Thread(r, "de-ai-audit-gaps"); t.setDaemon(true); return t; });
    public AiAuditManage(EntityManager em, PlatformTransactionManager manager, AiAccess access, ObjectMapper json) {
        this.em = em; this.access = access; this.json = json;
        tx = new TransactionTemplate(manager); tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.setTimeout(2);
        repair.scheduleWithFixedDelay(this::repairGaps, 5, 5, TimeUnit.SECONDS);
    }
    public Session session(AiExecutionContext context, AiRuntimeManage.ModelSnapshot model, Long settingsRevision, AiRecovery recovery, String resourceReference) {
        var row = new CoreAiAuditExecution(); row.setId(context.executionId()); row.setFeature(context.feature());
        row.setActorId(context.actorId()); row.setOrganizationId(context.organizationId()); row.setProxyOrganizationId(context.proxyOrganizationId());
        row.setResourceReference(resourceReference); row.setModelId(model.id()); row.setModelRevision(model.revision());
        row.setCredentialRevision(model.credentialRevision()); row.setModelIdentifier(model.identifier()); row.setSettingRevision(settingsRevision);
        row.setDeploymentMode(access.mode().mode().name()); row.setStatus("RUNNING"); row.setIncomplete(false); row.setGapCount(0L); row.setLastSequence(0);
        row.setFeature(bounded(row, row.getFeature(), 100)); row.setModelIdentifier(bounded(row, row.getModelIdentifier(), 256));
        row.setResourceReference(bounded(row, row.getResourceReference(), 1000));
        return new Session(row, recovery);
    }
    private String bounded(CoreAiAuditExecution row, String value, int limit) {
        if (value == null || value.length() <= limit) return value;
        row.setIncomplete(true); return value.substring(0, limit);
    }
    public final class Session {
        private final CoreAiAuditExecution row;
        private final AiRecovery recovery;
        private final Map<String, Long> previous = new HashMap<>();
        private String knownSecret;
        private Session(CoreAiAuditExecution row, AiRecovery recovery) { this.row = row; this.recovery = recovery; }
        public Exchange begin(String kind, String request, String secret) {
            synchronized (this) {
                var link = recovery == null ? null : recovery.link();
                if (link != null) { row.setTaskId(bounded(row, link.taskId(), 100)); row.setAttemptId(bounded(row, link.attemptId(), 100)); row.setSourceAttemptId(bounded(row, link.sourceAttemptId(), 100)); row.setAttemptNumber(link.attemptNumber()); }
                if (row.getStartedAt() == null) row.setStartedAt(System.currentTimeMillis());
                var entry = new CoreAiAuditEntry(); entry.setId(IDUtils.snowID()); entry.setExecutionId(row.getId());
                entry.setSequence(row.getLastSequence() + 1); row.setLastSequence(entry.getSequence());
                var p = recovery == null ? null : recovery.progress();
                String step = p == null || p.stepId() == null ? UUID.randomUUID().toString() : p.stepId();
                entry.setStepId(step); entry.setPreviousEntryId(previous.put(step, entry.getId())); entry.setKind(kind);
                entry.setTechnicalRetries(p == null ? 0 : p.technicalRetries()); entry.setCorrections(p == null ? 0 : p.corrections());
                entry.setModelId(row.getModelId()); entry.setModelRevision(row.getModelRevision()); entry.setCredentialRevision(row.getCredentialRevision());
                entry.setSettingRevision(row.getSettingRevision()); entry.setModelIdentifier(row.getModelIdentifier());
                entry.setStartedAt(System.currentTimeMillis()); entry.setStatus("RUNNING"); entry.setIncomplete(true);
                if (secret != null) knownSecret = secret;
                var exchange = new Exchange(this, entry, knownSecret); entry.setRequestBody(exchange.clean(request));
                save(this, entry); return exchange;
            }
        }
        public <T> T tool(String callId, String name, Map<String, String> arguments, Supplier<T> work) {
            String payload;
            try { payload = json.writeValueAsString(Map.of("callId", callId, "name", name, "arguments", arguments)); }
            catch (IOException invalid) { payload = "{\"capture\":\"unavailable\"}"; }
            var exchange = begin("TOOL", payload, null); boolean success = false; String code = null;
            try { T result = work.get(); exchange.output(String.valueOf(result)); success = true; return result; }
            catch (RuntimeException failure) { code = safeCode(failure); throw failure; }
            finally { exchange.finish(success, code); }
        }
        public synchronized void end(String status) {
            if (row.getFinishedAt() != null) return;
            knownSecret = null;
            row.setStatus(status); row.setFinishedAt(System.currentTimeMillis()); row.setRetentionExpiresAt(row.getFinishedAt() + RETENTION_MILLIS);
            if (!"COMPLETED".equals(status)) row.setIncomplete(true);
            save(this, null);
        }
    }
    public final class Exchange {
        private final Session session;
        private final CoreAiAuditEntry entry;
        private String secret;
        private final ByteArrayOutputStream response = new ByteArrayOutputStream();
        private boolean truncated, redacted, ended;
        private Exchange(Session session, CoreAiAuditEntry entry, String secret) { this.session = session; this.entry = entry; this.secret = secret; }
        private String clean(String text) {
            if (text == null || secret == null || secret.isEmpty()) return text;
            if (text.contains(secret)) { text = text.replace(secret, "[SYSTEM_SECRET_REDACTED]"); redacted = true; }
            var nodes = new ArrayList<com.fasterxml.jackson.databind.JsonNode>();
            var originals = new ArrayList<String>();
            boolean framed = text.stripLeading().startsWith("data:") || text.contains("\ndata:");
            try {
                if (framed) {
                    for (String frame : text.replace("\r\n", "\n").replace('\r', '\n').split("\n\n", -1)) {
                        var data = new StringBuilder();
                        for (String line : frame.split("\n")) if (line.startsWith("data:")) { if (!data.isEmpty()) data.append('\n'); data.append(line.substring(5).stripLeading()); }
                        originals.add(frame);
                        nodes.add(data.isEmpty() || "[DONE]".contentEquals(data) ? null : json.readTree(data.toString()));
                    }
                } else { nodes.add(json.readTree(text)); originals.add(text); }
                var streams = new HashMap<String, List<TextPart>>();
                for (var node : nodes) if (node != null) textParts(node, "", streams, secret, () -> redacted = true);
                for (var parts : streams.values()) {
                    var combined = new StringBuilder(); for (var part : parts) combined.append(part.text());
                    var masked = new java.util.BitSet(combined.length());
                    for (int at = combined.indexOf(secret); at >= 0; at = combined.indexOf(secret, at + secret.length())) masked.set(at, at + secret.length());
                    if (masked.isEmpty()) continue;
                    redacted = true; int offset = 0;
                    for (var part : parts) {
                        var value = new StringBuilder(); boolean replacing = false;
                        for (int index = 0; index < part.text().length(); index++) {
                            boolean hide = masked.get(offset + index);
                            if (hide && !replacing) value.append("[SYSTEM_SECRET_REDACTED]");
                            if (!hide) value.append(part.text().charAt(index));
                            replacing = hide;
                        }
                        part.replace().accept(value.toString()); offset += part.text().length();
                    }
                }
                if (!redacted) return text;
                if (!framed) return nodes.get(0).toString();
                var safe = new StringBuilder();
                for (int index = 0; index < nodes.size(); index++) {
                    safe.append(nodes.get(index) == null ? originals.get(index) : "data: " + nodes.get(index)).append("\n\n");
                    if (safe.length() > MAX_BODY) { truncated = true; return safe.substring(0, MAX_BODY); }
                }
                return safe.toString();
            } catch (IOException | RuntimeException malformed) {
                // Malformed/partial output is retained only when it cannot encode the known credential.
                String decoded = text;
                var matcher = java.util.regex.Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(text);
                var normalized = new StringBuilder();
                while (matcher.find()) matcher.appendReplacement(normalized, java.util.regex.Matcher.quoteReplacement(String.valueOf((char) Integer.parseInt(matcher.group(1), 16))));
                matcher.appendTail(normalized); decoded = normalized.toString();
                if (decoded.contains(secret)) { redacted = true; return "[SYSTEM_SECRET_REDACTED_PARTIAL_PAYLOAD]"; }
                return text;
            }
        }
        private synchronized void bytes(byte[] bytes, int offset, int length) {
            int amount = Math.min(length, MAX_BODY - response.size());
            if (amount > 0) response.write(bytes, offset, amount);
            if (amount < length) truncated = true;
        }
        public InputStream capture(InputStream input) {
            return new FilterInputStream(input) {
                @Override public int read() throws IOException { int value = in.read(); if (value >= 0) bytes(new byte[]{(byte) value}, 0, 1); return value; }
                @Override public int read(byte[] bytes, int offset, int length) throws IOException { int count = in.read(bytes, offset, length); if (count > 0) Exchange.this.bytes(bytes, offset, count); return count; }
            };
        }
        public void output(String text) {
            if (text.length() > MAX_BODY) { text = text.substring(0, MAX_BODY); truncated = true; }
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8); bytes(bytes, 0, bytes.length);
        }
        public void finish(boolean success, String code) {
            synchronized (session) {
                synchronized (this) {
                    if (ended) return; ended = true;
                    String body = clean(response.toString(StandardCharsets.UTF_8)); response.reset(); secret = null;
                    entry.setResponseBody(body); entry.setUsageJson(usage(body)); entry.setFinishedAt(System.currentTimeMillis());
                    entry.setStatus(success ? "SUCCEEDED" : "cancelled".equals(code) ? "CANCELLED" : "FAILED"); entry.setErrorCode(code);
                    entry.setIncomplete(!success || truncated || redacted);
                }
                if (Boolean.TRUE.equals(entry.getIncomplete())) session.row.setIncomplete(true);
                save(session, entry);
                // Captured bodies have a single bounded owner and are discarded after the persistence boundary.
                entry.setRequestBody(null); entry.setResponseBody(null);
            }
        }
    }
    private record TextPart(String text, java.util.function.Consumer<String> replace) { }
    private void textParts(com.fasterxml.jackson.databind.JsonNode node, String path, Map<String, List<TextPart>> streams, String secret, Runnable redaction) {
        if (node.isObject()) {
            var object = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            List.copyOf(object.properties()).forEach(field -> {
                String original = field.getKey();
                String key = original.replace(secret, "[SYSTEM_SECRET_REDACTED]");
                if (!key.equals(original)) { object.remove(original); object.set(key, field.getValue()); redaction.run(); }
                String nested = path + "/" + key.replace("~", "~0").replace("/", "~1");
                if (field.getValue().isTextual()) streams.computeIfAbsent(nested, ignored -> new ArrayList<>()).add(new TextPart(field.getValue().asText(), value -> object.put(key, value)));
                else textParts(field.getValue(), nested, streams, secret, redaction);
            });
        } else if (node.isArray()) {
            var array = (com.fasterxml.jackson.databind.node.ArrayNode) node;
            for (int index = 0; index < array.size(); index++) {
                final int position = index; String nested = path + "/" + index;
                if (array.get(index).isTextual()) streams.computeIfAbsent(nested, ignored -> new ArrayList<>()).add(new TextPart(array.get(index).asText(), value -> array.set(position, com.fasterxml.jackson.databind.node.TextNode.valueOf(value))));
                else textParts(array.get(index), nested, streams, secret, redaction);
            }
        }
    }
    private String usage(String body) {
        if (body == null || body.isEmpty()) return null;
        try {
            var root = json.readTree(body); if (root != null && root.path("usage").isObject()) return root.path("usage").toString();
        } catch (IOException ignored) { }
        String found = null;
        // Parse framed payloads only at the completed request boundary, never in the character hot path.
        var data = new StringBuilder();
        for (String line : body.split("\\r\\n|\\r|\\n")) {
            if (line.startsWith("data:")) { if (!data.isEmpty()) data.append('\n'); data.append(line.substring(5).stripLeading()); }
            else if (line.isEmpty() && !data.isEmpty()) {
                try { var root = json.readTree(data.toString()); if (root != null && root.path("usage").isObject()) found = root.path("usage").toString(); }
                catch (IOException ignored) { }
                data.setLength(0);
            }
        }
        return found != null && found.length() <= 4000 ? found : null;
    }
    private static String safeCode(Throwable failure) {
        return failure instanceof AiErrors.Failure known ? known.safeCode() : "upstream_failed";
    }
    private void save(Session session, CoreAiAuditEntry entry) {
        if (write(session.row, entry)) { synchronized (gaps) { gaps.remove(session.row.getId()); } return; }
        session.row.setIncomplete(true); session.row.setGapCount(session.row.getGapCount() + 1);
        synchronized (gaps) {
            if (gaps.containsKey(session.row.getId()) || gaps.size() < 256) gaps.put(session.row.getId(), copy(session.row));
            else droppedGapMarkers.incrementAndGet();
        }
        LOG.warn("AI audit gap execution={} count={} pending={} dropped={}", session.row.getId(), session.row.getGapCount(), pendingGapCount(), droppedGapMarkers.get());
    }
    private CoreAiAuditExecution copy(CoreAiAuditExecution row) { return json.convertValue(row, CoreAiAuditExecution.class); }
    private boolean write(CoreAiAuditExecution snapshot, CoreAiAuditEntry entry) {
        // Separate transactions prevent an audit outage from poisoning any suspended task/result transaction.
        boolean interrupted = Thread.interrupted();
        try {
            for (int attempt = 0; attempt < 2; attempt++) try {
                tx.executeWithoutResult(status -> {
                    var current = em.find(CoreAiAuditExecution.class, snapshot.getId(), jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
                    var row = copy(snapshot);
                    if (entry != null) {
                        var existing = em.find(CoreAiAuditEntry.class, entry.getId());
                        int sequence = existing != null ? existing.getSequence() : (current == null ? 0 : current.getLastSequence()) + 1;
                        entry.setSequence(sequence); snapshot.setLastSequence(Math.max(snapshot.getLastSequence(), sequence)); row.setLastSequence(snapshot.getLastSequence());
                    }
                    if (current != null) {
                        row.setIncomplete(Boolean.TRUE.equals(current.getIncomplete()) || Boolean.TRUE.equals(row.getIncomplete()));
                        row.setGapCount(Math.max(current.getGapCount(), row.getGapCount()));
                        row.setLastSequence(Math.max(current.getLastSequence(), row.getLastSequence()));
                        if (current.getFinishedAt() != null && (row.getFinishedAt() == null || current.getFinishedAt() > row.getFinishedAt())) {
                            row.setFinishedAt(current.getFinishedAt()); row.setRetentionExpiresAt(current.getRetentionExpiresAt()); row.setStatus(current.getStatus());
                        }
                    }
                    em.merge(row);
                    if (entry != null) em.merge(entry);
                    em.flush();
                });
                return true;
            } catch (RuntimeException unavailable) { /* No body, credential, URL or upstream exception enters ordinary logs. */ }
            return false;
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
    public int pendingGapCount() { synchronized (gaps) { return gaps.size(); } }
    public long droppedGapMarkerCount() { return droppedGapMarkers.get(); }
    private void repairGaps() {
        List<CoreAiAuditExecution> pending; synchronized (gaps) { pending = List.copyOf(gaps.values()); }
        for (var snapshot : pending) if (write(snapshot, null)) synchronized (gaps) { gaps.remove(snapshot.getId(), snapshot); }
    }
    public Detail get(String executionId) {
        var identity = access.capture(); access.verify(identity, false);
        if (executionId == null || executionId.length() > 100) throw AiErrors.error("invalid");
        return tx.execute(status -> {
            var row = em.find(CoreAiAuditExecution.class, executionId);
            // The authorization target is always persisted execution scope, never a frontend parameter.
            access.audit(identity, row == null ? null : row.getOrganizationId());
            if (row == null) throw AiErrors.error("forbidden");
            var entries = em.createQuery("select e from CoreAiAuditEntry e where e.executionId=:execution order by e.sequence", CoreAiAuditEntry.class)
                    .setParameter("execution", executionId).setMaxResults(100).getResultList().stream()
                    .map(e -> new Entry(e.getId(), e.getSequence(), e.getStepId(), e.getPreviousEntryId(), e.getKind(), e.getTechnicalRetries(), e.getCorrections(),
                            e.getRequestBody(), e.getResponseBody(), e.getUsageJson(), e.getStartedAt(), e.getFinishedAt(), e.getStatus(), e.getErrorCode(), e.getIncomplete(), e.getModelId(), e.getModelRevision(), e.getCredentialRevision(), e.getSettingRevision(), e.getModelIdentifier())).toList();
            access.audit(identity, row.getOrganizationId());
            return new Detail(row.getId(), row.getTaskId(), row.getAttemptId(), row.getSourceAttemptId(), row.getAttemptNumber(), row.getActorId(), row.getOrganizationId(),
                    row.getProxyOrganizationId(), row.getFeature(), row.getResourceReference(), row.getDeploymentMode(), row.getModelId(), row.getModelRevision(), row.getCredentialRevision(),
                    row.getSettingRevision(), row.getModelIdentifier(), row.getStartedAt(), row.getFinishedAt(), row.getRetentionExpiresAt(), row.getStatus(), row.getIncomplete(), row.getGapCount(), row.getLastSequence(), entries.size() < row.getLastSequence(), entries);
        });
    }
    @PreDestroy public void close() { repair.shutdownNow(); }
}
