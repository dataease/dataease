package io.dataease.listener.sql;

import io.dataease.ai.dao.entity.CoreAiTaskResource;
import io.dataease.initSql.Version;
import jakarta.persistence.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/** Additive and retryable. Legacy scenes never declared occupancy; do not invent historical locks. */
@Component
public class AiTaskResourceInit implements CoreSqlBlock {
    private final AiConfigurationInit schema;
    public AiTaskResourceInit(JdbcTemplate jdbc, EntityManagerFactory factory, EntityManager em, PlatformTransactionManager manager) {
        schema = new AiConfigurationInit(jdbc, factory, em, manager);
    }
    public Version getVersion() { return new Version("2.53"); }
    public void execute() {
        schema.create(CoreAiTaskResource.class);
        schema.index("core_ai_task_resource", "idx_ai_resource_attempt", "attempt_id", false);
    }
}
