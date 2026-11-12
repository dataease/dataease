package io.dataease.listener.sql;

import io.dataease.ai.dao.entity.*;
import io.dataease.initSql.Version;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/** Additive and repeatable, including retries after partial DDL success. */
@Component
public class AiTaskInit implements CoreSqlBlock {
    private final AiConfigurationInit schema;
    public AiTaskInit(JdbcTemplate jdbc, EntityManagerFactory factory, EntityManager em, PlatformTransactionManager manager) {
        schema = new AiConfigurationInit(jdbc, factory, em, manager);
    }
    public Version getVersion() { return new Version("2.49"); }
    public void execute() {
        schema.create(CoreAiTask.class); schema.create(CoreAiTaskAttempt.class); schema.create(CoreAiTaskNode.class);
        schema.index("core_ai_task", "uk_ai_task_submission", "submission_hash", true);
        schema.index("core_ai_task", "idx_ai_task_owner", "owner_binding", false);
        schema.index("core_ai_task_attempt", "uk_ai_attempt_retry", "retry_hash", true);
        schema.index("core_ai_task_attempt", "idx_ai_attempt_schedule", "status,created_at", false);
        schema.index("core_ai_task_attempt", "idx_ai_attempt_node", "node_id,status", false);
    }
}
