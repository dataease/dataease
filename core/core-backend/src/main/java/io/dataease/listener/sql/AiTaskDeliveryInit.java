package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Additive delivery facts; safe to repeat after partial DDL or backfill failure. */
@Component
public class AiTaskDeliveryInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    public AiTaskDeliveryInit(JdbcTemplate jdbc, EntityManagerFactory factory) { this.jdbc = jdbc; this.factory = factory; }
    public Version getVersion() { return new Version("2.49"); }
    public void execute() {
        Set<String> columns = jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Set<String>>) connection -> {
            Set<String> names = new HashSet<>();
            for (String table : List.of("core_ai_task_attempt", "CORE_AI_TASK_ATTEMPT")) {
                try (ResultSet result = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), table, null)) {
                    while (result.next()) names.add(result.getString("COLUMN_NAME").toLowerCase(java.util.Locale.ROOT));
                }
            }
            return names;
        });
        var session = factory.unwrap(SessionFactoryImplementor.class);
        String bigint = session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(Types.BIGINT, session.getJdbcServices().getDialect());
        for (String column : List.of("delivery_expires_at", "notice_ack_at"))
            if (!columns.contains(column)) jdbc.execute("alter table core_ai_task_attempt add " + column + " " + bigint);
        jdbc.update("update core_ai_task_attempt set notice_ack_at=finished_at, delivery_expires_at=finished_at+? where finished_at is not null and delivery_expires_at is null", io.dataease.ai.manage.AiTaskManage.DELIVERY_MILLIS);
    }
}
