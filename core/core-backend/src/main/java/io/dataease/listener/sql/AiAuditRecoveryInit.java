package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import jakarta.persistence.EntityManagerFactory;
import java.sql.ResultSet;
import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Preserve existing terminal facts; old running records use a bounded upgrade-time recovery baseline. */
@Component
public class AiAuditRecoveryInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    public AiAuditRecoveryInit(JdbcTemplate jdbc, EntityManagerFactory factory) { this.jdbc = jdbc; this.factory = factory; }
    public Version getVersion() { return new Version("2.57"); }
    public void execute() {
        var session = factory.unwrap(SessionFactoryImplementor.class);
        var types = session.getTypeConfiguration().getDdlTypeRegistry(); var dialect = session.getJdbcServices().getDialect();
        add("deadline", types.getTypeName(java.sql.Types.BIGINT, dialect));
        add("retention_days", types.getTypeName(java.sql.Types.INTEGER, dialect));
        add("recovered_at", types.getTypeName(java.sql.Types.BIGINT, dialect));
        // The old schema did not retain the original policy. Freeze the configured value at upgrade only.
        Integer days = jdbc.queryForObject("select audit_retention_days from core_ai_setting where id=1", Integer.class);
        jdbc.update("update core_ai_audit_execution set retention_days=? where retention_days is null", days == null ? 30 : days);
        // 300 seconds is the existing maximum execution policy; this cannot shorten an admitted old execution.
        jdbc.update("update core_ai_audit_execution set deadline=coalesce(started_at,?)+300000 where deadline is null and finished_at is null", System.currentTimeMillis());
    }
    private void add(String column, String type) {
        boolean present = Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
            for (String table : List.of("core_ai_audit_execution", "CORE_AI_AUDIT_EXECUTION")) try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), table, null)) {
                while (columns.next()) if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
            }
            return false;
        }));
        if (!present) jdbc.execute("alter table core_ai_audit_execution add " + column + " " + type);
    }
}
