package io.dataease.listener.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.dao.entity.*;
import io.dataease.initSql.Version;
import jakarta.persistence.*;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.*;
import java.util.*;

/** Additive scheduler upgrade; safe to retry after any DDL or data backfill step. */
@Component
public class AiTaskSchedulingInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    private final EntityManager em;
    private final TransactionTemplate tx;
    public AiTaskSchedulingInit(JdbcTemplate jdbc, EntityManagerFactory factory, EntityManager em, PlatformTransactionManager manager) {
        this.jdbc = jdbc; this.factory = factory; this.em = em; tx = new TransactionTemplate(manager);
    }
    public Version getVersion() { return new Version("2.52"); }
    public void execute() {
        var schema = new AiConfigurationInit(jdbc, factory, em, tx.getTransactionManager());
        schema.create(CoreAiTaskSchedule.class);
        column("core_ai_task", "actor_id", Types.BIGINT);
        column("core_ai_task_schedule", "last_sequence", Types.BIGINT);
        column("core_ai_task_attempt", "queue_sequence", Types.BIGINT);
        column("core_ai_task_attempt", "queue_deadline", Types.BIGINT);
        column("core_ai_setting", "task_max_running", Types.INTEGER);
        column("core_ai_setting", "task_max_queued", Types.INTEGER);
        column("core_ai_setting", "task_max_queued_per_user", Types.INTEGER);
        column("core_ai_setting", "task_max_wait_seconds", Types.INTEGER);
        jdbc.update("update core_ai_setting set task_max_running=2 where task_max_running is null");
        jdbc.update("update core_ai_setting set task_max_queued=100 where task_max_queued is null");
        jdbc.update("update core_ai_setting set task_max_queued_per_user=10 where task_max_queued_per_user is null");
        jdbc.update("update core_ai_setting set task_max_wait_seconds=300 where task_max_wait_seconds is null");
        var json = new ObjectMapper();
        jdbc.query("select id,owner_json from core_ai_task where actor_id is null", row -> {
            try {
                var actor = json.readTree(row.getString("owner_json")).path("actor");
                if (actor.isIntegralNumber() && actor.canConvertToLong() && actor.longValue() > 0) jdbc.update("update core_ai_task set actor_id=? where id=? and actor_id is null", actor.longValue(), row.getLong("id"));
            } catch (com.fasterxml.jackson.core.JsonProcessingException invalidHistoricalIdentity) { /* Historical terminal samples remain untouched. */ }
        });
        // Existing QUEUED attempts inherit the documented default from their original acceptance time.
        // Historical terminal samples have no queue deadline and are never rewritten.
        jdbc.update("update core_ai_task_attempt set queue_deadline=created_at+300000 where status='QUEUED' and queue_deadline is null");
        tx.executeWithoutResult(status -> { if (em.find(CoreAiTaskSchedule.class, 1L) == null) em.persist(new CoreAiTaskSchedule()); em.flush(); });
        jdbc.update("update core_ai_task_schedule set last_sequence=0 where last_sequence is null");
        Long maximum = jdbc.queryForObject("select max(queue_sequence) from core_ai_task_attempt", Long.class);
        long[] sequence = { Math.max(jdbc.queryForObject("select last_sequence from core_ai_task_schedule where id=1", Long.class), maximum == null ? 0 : maximum) };
        jdbc.query("select id from core_ai_task_attempt where status='QUEUED' and queue_sequence is null order by created_at,id", row -> {
            jdbc.update("update core_ai_task_attempt set queue_sequence=? where id=? and queue_sequence is null", ++sequence[0], row.getLong("id"));
        });
        jdbc.update("update core_ai_task_schedule set last_sequence=? where id=1", sequence[0]);
        schema.index("core_ai_task_attempt", "idx_ai_attempt_fifo", "status,queue_sequence", false);
        schema.index("core_ai_task_attempt", "idx_ai_attempt_wait", "status,queue_deadline", false);
        schema.index("core_ai_task", "idx_ai_task_actor", "actor_id", false);
    }
    private void column(String table, String name, int code) {
        boolean exists = Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
            for (String spelling : List.of(table, table.toUpperCase(Locale.ROOT))) {
                try (var columns = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), spelling, null)) {
                    while (columns.next()) if (name.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
                }
            }
            return false;
        }));
        if (!exists) {
            var session = factory.unwrap(SessionFactoryImplementor.class);
            jdbc.execute("alter table " + table + " add " + name + " " + session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(code, session.getJdbcServices().getDialect()));
        }
    }
}
