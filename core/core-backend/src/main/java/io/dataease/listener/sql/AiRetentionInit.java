package io.dataease.listener.sql;

import io.dataease.ai.dao.entity.CoreAiAuditRetired;
import io.dataease.initSql.Version;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.sql.ResultSet;
import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/** Additive/retryable. Existing expiries stay fixed; policy changes cannot resurrect ended deliveries. */
@Component
public class AiRetentionInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    private final AiConfigurationInit schema;
    public AiRetentionInit(JdbcTemplate jdbc, EntityManagerFactory factory, EntityManager em, PlatformTransactionManager manager) {
        this.jdbc = jdbc; this.factory = factory; schema = new AiConfigurationInit(jdbc, factory, em, manager);
    }
    public Version getVersion() { return new Version("2.55"); }
    public void execute() {
        var session = factory.unwrap(SessionFactoryImplementor.class);
        var registry = session.getTypeConfiguration().getDdlTypeRegistry(); var dialect = session.getJdbcServices().getDialect();
        add("core_ai_setting", "audit_retention_days", registry.getTypeName(java.sql.Types.INTEGER, dialect), "30");
        add("core_ai_setting", "delivery_retention_days", registry.getTypeName(java.sql.Types.INTEGER, dialect), "7");
        add("core_ai_task_attempt", "delivery_purged_at", registry.getTypeName(java.sql.Types.BIGINT, dialect), null);
        schema.create(CoreAiAuditRetired.class);
        schema.index("core_ai_task_attempt", "idx_ai_delivery_cleanup", "status,delivery_expires_at,delivery_purged_at", false);
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
