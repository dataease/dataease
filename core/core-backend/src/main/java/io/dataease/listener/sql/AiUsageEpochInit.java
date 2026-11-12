package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.util.List;

/** Additive durable off/on fence. Does not reconstruct historical executions or enable AI. */
@Component
public class AiUsageEpochInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    public AiUsageEpochInit(JdbcTemplate jdbc, EntityManagerFactory factory) { this.jdbc = jdbc; this.factory = factory; }
    public Version getVersion() { return new Version("2.56"); }
    public void execute() {
        var session = factory.unwrap(SessionFactoryImplementor.class);
        String type = session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(java.sql.Types.BIGINT, session.getJdbcServices().getDialect());
        for (String table : List.of("core_ai_setting", "core_ai_org_policy")) {
            boolean present = Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
                for (String spelling : List.of(table, table.toUpperCase(java.util.Locale.ROOT))) try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), spelling, null)) {
                    while (columns.next()) if ("usage_epoch".equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
                }
                return false;
            }));
            if (!present) jdbc.execute("alter table " + table + " add usage_epoch " + type + " default 0 not null");
            jdbc.update("update " + table + " set usage_epoch=0 where usage_epoch is null");
        }
    }
}
