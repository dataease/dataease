package io.dataease.listener.sql;

import io.dataease.ai.dao.entity.CoreAiAuditExecution;
import io.dataease.ai.dao.entity.CoreAiAuditEntry;
import io.dataease.initSql.Version;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/** No historical request bodies are invented; new evidence starts at actual execution. */
@Component
public class AiAuditInit implements CoreSqlBlock {
    private final AiConfigurationInit schema;
    public AiAuditInit(JdbcTemplate jdbc, EntityManagerFactory factory, EntityManager em, PlatformTransactionManager manager) {
        schema = new AiConfigurationInit(jdbc, factory, em, manager);
    }
    public Version getVersion() { return new Version("2.54"); }
    public void execute() {
        schema.create(CoreAiAuditExecution.class); schema.create(CoreAiAuditEntry.class);
        schema.index("core_ai_audit_entry", "idx_ai_audit_entry_execution", "execution_id,sequence", false);
        schema.index("core_ai_audit_execution", "idx_ai_audit_retention", "finished_at,retention_expires_at", false);
        schema.index("core_ai_audit_execution", "idx_ai_audit_attempt", "task_id,attempt_id", false);
    }
}
