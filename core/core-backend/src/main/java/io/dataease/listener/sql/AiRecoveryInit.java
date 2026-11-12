package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.sql.ResultSet;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Additive, idempotent retry-policy migration; values only affect new attempts. */
@Component
public class AiRecoveryInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    public AiRecoveryInit(JdbcTemplate jdbc, EntityManagerFactory factory) { this.jdbc = jdbc; this.factory = factory; }
    public Version getVersion() { return new Version("2.51"); }
    public void execute() {
        var session = factory.unwrap(SessionFactoryImplementor.class);
        String type = session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(java.sql.Types.INTEGER, session.getJdbcServices().getDialect());
        add("core_ai_task_attempt", "retry_safe", session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(java.sql.Types.BOOLEAN, session.getJdbcServices().getDialect()),
                session.getJdbcServices().getDialect().toBooleanValueString(false));
        add("core_ai_task_attempt", "execution_id", session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(java.sql.Types.VARCHAR, org.hibernate.engine.jdbc.Size.length(100)), null);
        add("core_ai_task_attempt", "recovery_json", session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(java.sql.Types.VARCHAR, org.hibernate.engine.jdbc.Size.length(2000)), null);
        for (String column : List.of("recovery_step_retries", "recovery_attempt_retries", "recovery_initial_delay_millis")) {
            int value = column.endsWith("delay_millis") ? 1000 : column.contains("step") ? 2 : 3;
            add("core_ai_setting", column, type, Integer.toString(value));
        }
    }
    private void add(String table, String column, String type, String defaultValue) {
        boolean present = Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
            for (String spelling : List.of(table, table.toUpperCase(java.util.Locale.ROOT))) try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), spelling, null)) {
                while (columns.next()) if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
            }
            return false;
        }));
        if (!present) jdbc.execute("alter table " + table + " add " + column + " " + type + (defaultValue == null ? "" : " default " + defaultValue + " not null"));
        if (defaultValue != null) jdbc.update("update " + table + " set " + column + "=" + defaultValue + " where " + column + " is null");
    }
}
